package br.unb.cic.bionimbus.services.storage;

import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.monitor.MonitoringService;
import br.unb.cic.bionimbus.utils.Put;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.AvroRemoteException;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class StorageService extends AbstractBioService {

    @Inject
    private MetricRegistry metricRegistry;
    private final ScheduledExecutorService executorService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
            .namingPattern("StorageService-%d").build());
    private Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    private Map<String, PluginFile> savedFiles = new ConcurrentHashMap<String, PluginFile>();
    private P2PService p2p = null;
    private File dataFolder = new File("data-folder"); //TODO: remover hard-coded e colocar em node.yaml e injetar em StorageService
    private Double MAXCAPACITY = 0.9;
    private int PORT = 9999;
    private int REPLICATIONFACTOR = 2;
    List<String> listFile = new ArrayList<String>();

    @Inject
    public StorageService(final ZooKeeperService service, MetricRegistry metricRegistry) {

        Preconditions.checkNotNull(service);
        this.zkService = service;

        this.metricRegistry = metricRegistry;
        // teste
        Counter c = metricRegistry.counter("teste");
        c.inc();
    }

    @Override
    public void run() {
        
        System.out.println("checando pending save...");
        try{
            if (getPeers().size() != 1) 
                checkingPendingSave(); 
        } 
        catch (IOException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Método que inicia a storage
     * @param p2p
     */
    @Override
    public void start(P2PService p2p) {
        this.p2p = p2p;
        if (p2p != null) {
            p2p.addListener(this);
        }
        //Criando pastas zookeeper para o módulo de armazenamento
        zkService.createPersistentZNode(zkService.getPath().PENDING_SAVE.toString(), null);
        zkService.createPersistentZNode(zkService.getPath().FILES.getFullPath(p2p.getConfig().getId(), "", ""), "");
        try {
            zkService.getChildren(zkService.getPath().PENDING_SAVE.getFullPath("", "", ""), new UpdatePeerData(zkService, this));
            zkService.getChildren(zkService.getPath().PEERS.getFullPath("", "", ""), new UpdatePeerData(zkService, this));

        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }

        checkFiles();
        checkPeers();
        try {
            if (getPeers().size() != 1) {
                checkReplicationFiles();
            }
        } catch (Exception ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        executorService.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        p2p.remove(this);
        executorService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onEvent(P2PEvent event) {
    }

    /**
     * Verifica os peers(plugins) existentes e adiciona um observador(watcher)
     * no zNode STATUS de cada plugin.
     */
    public void checkPeers() {
        for (PluginInfo plugin : getPeers().values()) {
            try {
                zkService.getData(zkService.getPath().STATUS.getFullPath(plugin.getId(), null, null), new UpdatePeerData(zkService, this));
            } catch (KeeperException ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * Verifica os arquivos que existem no recurso e se o fator de replicação
     * está sendo respeitado na federação. Roda apenas quando o plugin está
     * sendo iniciado.
     */
    public void checkFiles() {
        try {
            if (!dataFolder.exists()) {
                System.out.println(" (CheckFiles) dataFolder " + dataFolder + " doesn't exists, creating...");
                dataFolder.mkdirs();
            }
            zkService.getChildren(zkService.getPath().FILES.getFullPath(p2p.getConfig().getId(), "", ""), new UpdatePeerData(zkService, this));
            for (File file : dataFolder.listFiles()) {
                if (!savedFiles.containsKey(file.getName())) {

                    PluginFile pluginFile = new PluginFile();
                    pluginFile.setId(file.getName());
                    pluginFile.setName(file.getName());
                    pluginFile.setPath(file.getPath());

                    /*
                     * listIds - ID do Plugin que contem os arquivos
                     *  Verifica onde esse arquivo está caso já exista
                     */
                    List<String> listIds = new ArrayList<String>();
                    listIds.add(p2p.getConfig().getId());

                    pluginFile.setPluginId(listIds);
                    pluginFile.setSize(file.length());
                    String path =zkService.createPersistentZNode(zkService.getPath().PREFIX_FILE.getFullPath(p2p.getConfig().getId(), pluginFile.getId(), ""), pluginFile.toString());
                    zkService.getData(zkService.getPath().PREFIX_FILE.getFullPath(p2p.getConfig().getId(), pluginFile.getId(), ""), new UpdatePeerData(zkService, this));

                    savedFiles.put(pluginFile.getName(), pluginFile);
                }

            }
        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checa quantas cópias existem de um arquivo, caso existam menos cópias do
     * que REPLICATIONFACTOR inicia a replicação deste arquivo; Este método
     * checa todos os arquivos da federação.
     *
     * @throws Exception
     */
    public void checkReplicationFiles() throws Exception {
        for (Collection<String> collection : getFiles().values()) {
            /*
             * Percorre cada arquivo e o IP que possui ele
             */
            for (Iterator<String> it = collection.iterator(); it.hasNext();) {
                String fileNamePlugin = it.next();
                
                if (!existReplication(fileNamePlugin)) {
                    /*
                     * Caso não exista um número de cópias igual a REPLICATIONFACTOR inicia as cópias,
                     * enviando uma RPC para o peer que possui o arquivo, para que ele replique.
                     */
                    String ipPluginFile = getFilesIP(fileNamePlugin);
                    if (!ipPluginFile.equals(p2p.getConfig().getAddress())) {
                        RpcClient rpcClient = new AvroClient("http", ipPluginFile, PORT);
                        rpcClient.getProxy().notifyReply(fileNamePlugin, ipPluginFile);
                        rpcClient.close();
                    } else {
                        replication(fileNamePlugin, ipPluginFile);
                    }
                }
            }
        }
    }

    /**
     * Verifica a existência da replicação do arquivo na federação. Se a
     * replicação estiver feita retona true; Fator de replicação igual a 2;
     * Retorna true se existir replicação.
     */
    private boolean existReplication(String fileName) throws IOException {
        int cont = 0;
        System.out.println("(existReplication)Verificando se o arquivo: "+fileName+" está replicado!");
        for (Collection<String> collection : getFiles().values()) {
            for (Iterator<String> it = collection.iterator(); it.hasNext();) {
                String fileNamePlugin = it.next();
                if (fileName.equals(fileNamePlugin)) {
                    cont++; 
                }
               
            }
        }
        System.out.println("(existReplication) Arquivo: "+fileName+" contém: "+ cont+" replicas!");
        if (cont < REPLICATIONFACTOR) {
            return false;
        }
        
        return true;
    }

    /**
     * Cria map com endereço dos peers(plugins) e seus respectivos arquivos
     * baseado nos dados do zookeeper.
     *
     * @return map de endereço e lista de arquivos.
     */
    public Map<String, List<String>> getFiles() throws IOException {
        Map<String, List<String>> mapFiles = new HashMap<String, List<String>>();
        List<String> listFiles;
        checkFiles();
        try {
            for (PluginInfo plugin : getPeers().values()) {
                listFiles = new ArrayList<String>();
                for (String file : zkService.getChildren(plugin.getPath_zk() + zkService.getPath().FILES.toString(), new UpdatePeerData(zkService, this))) {
                    listFiles.add(file.substring(5, file.length()));
                }
                mapFiles.put(plugin.getHost().getAddress(), listFiles);
            }
        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return mapFiles;

    }

    /**
     * Metodo para pegar o Ip de cada peer na federação e verificar se um
     * arquivo está com este peer, se o arquivo for encontrado retorna o Ip do
     * peer, caso contrário retorna null.
     *
     * @param file
     * @return Ip que possui o arquivo ou null
     */
    public String getFilesIP(String file) throws IOException {
        List<String> listFiles;
        try {
            for (Iterator<PluginInfo> it = getPeers().values().iterator(); it.hasNext();) {
                PluginInfo plugin = it.next();
                listFiles = zkService.getChildren(plugin.getPath_zk() + zkService.getPath().FILES.toString(), null);
                for (String checkfile : listFiles) {

                    String idfile = checkfile.substring(checkfile.indexOf(zkService.getPath().UNDERSCORE.toString()) + 1);
                    if (file.equals(idfile)) {
                        return plugin.getHost().getAddress();
                    }
                }
            }
            return "";
        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;

    }

    /**
     * Retorna o tamanho do arquivo, dado o nome do mesmo.
     * @param file O nome do arquivo
     * @return O tamanho do arquivo
     */
    public long getFileSize(String file) {
       
        try { 
            List<String> listFiles;
            for (Iterator<PluginInfo> it = getPeers().values().iterator(); it.hasNext();) {
                PluginInfo plugin = it.next();
                listFiles = zkService.getChildren(plugin.getPath_zk() + zkService.getPath().FILES.toString(), null);
                PluginFile files = new ObjectMapper().readValue(zkService.getData(zkService.getPath().PREFIX_FILE.getFullPath(plugin.getId(), file, ""), null), PluginFile.class);
                return files.getSize();
            }

        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Recebe uma list com todos os peers da federação e seta o custo de
     * armazenamento em cada plugin
     *
     * @param list - Lista com todos os plugins da federação
     * @return - Lista com todos os plugins com seus custos de armazenamento
     * inseridos
     */
    public List<NodeInfo> bestNode(List<NodeInfo> list) {

        List<NodeInfo> plugins;
        cloudMap = getPeers();
        for (NodeInfo node : list) {
            System.out.println("" + cloudMap.get(node.getPeerId()));
            cloudMap.get(node.getPeerId()).setLatency(node.getLatency());
            cloudMap.get(node.getPeerId()).setFsFreeSize(node.getFreesize());
        }
        StoragePolicy policy = new StoragePolicy();
        /*
         * Dentro da Storage Policy é feito o ordenamento da list de acordo com o custo de armazenamento
         */
        plugins = policy.calcBestCost(zkService, cloudMap.values());

        return plugins;
    }

    /**
     * Verifica se um arquivo existe em um peer e seta o seu Znode no Zookeeper
     *
     * @param file - Arquivo a ser verifcado
     * @return true caso o arquivo exista e tenha sido setado
     */
    public boolean checkFilePeer(PluginFile file) {
        System.out.println("(checkFilePeer)vericando se o arquivo "+file.toString()+" existe no peer");
        String pathHome = System.getProperty("user.dir");
        String path =  (pathHome.substring(pathHome.length()).equals("/") ? pathHome+"data-folder/" : pathHome+"/data-folder/");
        File localFile = new File(path + file.getName());
        
        if (localFile.exists()) {
            zkService.createPersistentZNode(zkService.getPath().PREFIX_FILE.getFullPath(p2p.getConfig().getId(), file.getId(), ""), file.toString());
            try {
                zkService.getData(zkService.getPath().PREFIX_FILE.getFullPath(p2p.getConfig().getId(), file.getId(), ""), new UpdatePeerData(zkService, this));
            } catch (KeeperException ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        return false;
    }

    /**
     * Método que manda o comando dizendo que o arquivo foi upado para copiar um arquivo de um peer para outro
     *
     * @param fileuploaded
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public synchronized void fileUploaded(PluginFile fileuploaded) throws KeeperException, InterruptedException, IOException {
        System.out.println("(fileUploaded) Checando se existe a requisição no pending saving"+fileuploaded.toString());
        if (zkService.getZNodeExist(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", fileuploaded.getId(), ""), false)) {
            
            String ipPluginFile = getPeers().get(fileuploaded.getPluginId().iterator().next()).getHost().getAddress();
            FileInfo file = new FileInfo();
            file.setFileId(fileuploaded.getId());
            file.setName(fileuploaded.getName());
            file.setSize(fileuploaded.getSize());
            if (!p2p.getConfig().getAddress().equals(ipPluginFile)){
       
                RpcClient rpcClient = new AvroClient("http", ipPluginFile, PORT);
                if (rpcClient.getProxy().verifyFile(file, fileuploaded.getPluginId())&&zkService.getZNodeExist(zkService.getPath().PREFIX_FILE.getFullPath(fileuploaded.getPluginId().iterator().next(), fileuploaded.getId(), ""), false)) {
                        zkService.delete(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", fileuploaded.getId(), ""));   
                }else {
                    try {
                        rpcClient.getProxy().notifyReply(fileuploaded.getName(), ipPluginFile);
                        rpcClient.close();
                    } catch (Exception ex) {
                        Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }else {
                if (checkFilePeer(fileuploaded)) {
                    if (zkService.getZNodeExist(zkService.getPath().PREFIX_FILE.getFullPath(fileuploaded.getPluginId().iterator().next(), fileuploaded.getId(), ""), true)&&!existReplication(file.getName())){
                        try {
                                 replication(file.getName(), p2p.getConfig().getAddress());
                                 if(existReplication(file.getName())){
                                 zkService.delete(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", fileuploaded.getId(),""));
                                 }
                                 else{
                                     System.out.println("\n\n Erro na replicacao, arquivo nao foi replicado!!");
                                 }
                        } catch (JSchException ex) {
                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SftpException ex) {
                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                       
                    }
               }
            }
        }
        else {
                    System.out.println("Arquivo não encontrado nas pendências !");   
        }
 }
    
    /**
     * Metodo que checa os znodes filhos da pending_save, para replica-lós
     */
    public void checkingPendingSave() throws IOException{
        try {
            ObjectMapper mapper = new ObjectMapper();
            int cont = 0;
            List<String> pendingsave = zkService.getChildren(zkService.getPath().PENDING_SAVE.toString(), null);
            for(String files: pendingsave){
                try {
                    String data = zkService.getData(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", files.substring(13, files.length()), ""), null);
                    if (data == null || data.trim().isEmpty()){
                        System.out.println(">>>>>>>>>> NÃO EXISTEM DADOS PARA PATH " + zkService.getPath().PENDING_SAVE.getFullPath("", "", ""));
                        continue;
                    }
                    PluginFile fileplugin = mapper.readValue(data, PluginFile.class);
                    while(cont < 6){
                        if(fileplugin.getPluginId().size() == REPLICATIONFACTOR){
                            zkService.delete(zkService.getPath().PENDING_SAVE.getFullPath("", fileplugin.getId(), ""));
                            break;
                        }
                        String address = getFilesIP(fileplugin.getName());
                        if(!address.equals(p2p.getConfig().getAddress())){
                            RpcClient rpcClient = new AvroClient("http", address, PORT);
                            rpcClient.getProxy().notifyReply(fileplugin.getName(), address);
                            try {
                                rpcClient.close();
                                cont++;
                            } catch (Exception ex) {
                                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }           
                                    
                        else{
                            try{
                                replication(fileplugin.getName(),address);
                            } catch (JSchException ex) {
                                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SftpException ex) {
                                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        cont++;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                }
                existReplication(files);
                
                
            }
        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Realiza a replicação de arquivos, sejam eles enviados pelo cliente ou
     * apenas gerados na própria federação
     *
     * @param filename - nome do arquivo
     * @param address - endereço do peer que possui o arquivo
     * @throws IOException
     * @throws JSchException
     * @throws SftpException
     */
    public synchronized void replication(String filename, String address) throws IOException, JSchException, SftpException {

        System.out.println("(replication) Replicando o arquivo de nome: "+filename+" do peer: "+address);
        List<NodeInfo> pluginList = new ArrayList<NodeInfo>();
        List<String> idsPluginsFile = new ArrayList<String>();
        File file = new File(System.getProperty("user.dir")+"/data-folder/" + filename);
        

        int filesreplicated = 1;

        /*
         * Verifica se o arquivo existe no peer
         */
        if (file.exists()) {
            FileInfo info = new FileInfo();
            info.setFileId(file.getName());
            info.setName(file.getName());
            info.setSize(file.length());

            PluginFile pluginFile = new PluginFile(info);
            /*
             * PLuginList ira receber a lista dos Peers disponiveis na federação
             * e que possuem espaço em disco para receber o arquivo a ser replicado
             */

            NodeInfo no = null;

            /*
             * While para que o peer pegue o próprio endereço e ele seja removido da lista de peers, 
             * isso é feito para evitar que ele tente replicar
             * o arquivo para ele mesmo.
             */
            for (NodeInfo node: getNodeDisp(info.getSize())){
                if(node.getAddress().equals(address)){
                    no=node;
                    break;
                }
            }
            if(no!=null){
                pluginList.remove(no);
                idsPluginsFile.add(p2p.getConfig().getId());
                pluginList = new ArrayList<NodeInfo>(bestNode(pluginList));
                for (NodeInfo curr : pluginList){
                    if (no.getAddress().equals(curr.getAddress())){
                        no = curr;
                        break;
                    }
                }
                
                pluginList.remove(no);
            }
            pluginList = new ArrayList<NodeInfo>(bestNode(pluginList));
            pluginList.remove(no);
            Iterator<NodeInfo> bt = pluginList.iterator();
            while (bt.hasNext() && filesreplicated != REPLICATIONFACTOR) {
                NodeInfo node = (NodeInfo) bt.next();
                if (!(node.getAddress().equals(address))) {
                    /*
                     * Descoberto um peer disponivel, tenta enviar o arquivo
                     */
                    Put conexao = new Put(node.getAddress(), dataFolder + "/" + info.getName());
                    if (conexao.startSession()) {
                        idsPluginsFile.add(node.getPeerId());
                        
                        pluginFile.setPluginId(idsPluginsFile);
                        /*
                         * Com o arquivo enviado, seta os seus dados no Zookeeper
                         */
                        for (String idPlugin : idsPluginsFile) {
                            try {
                                if (zkService.getZNodeExist(zkService.getPath().PREFIX_FILE.getFullPath(idPlugin, filename, ""), true)) {
                                    zkService.setData(zkService.getPath().PREFIX_FILE.getFullPath(idPlugin, filename, ""), pluginFile.toString());
                                } else {
                                    zkService.createPersistentZNode(zkService.getPath().PREFIX_FILE.getFullPath(idPlugin, filename, ""), pluginFile.toString());
                                }
                                zkService.getData(zkService.getPath().PREFIX_FILE.getFullPath(idPlugin, filename, ""), new UpdatePeerData(zkService, this));
                            } catch (KeeperException ex) {
                                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        filesreplicated++;
                        if(filesreplicated==REPLICATIONFACTOR)
                            break;
                    }
                  
                }
            }
        }
    }


    /**
     * Pega uma lista com todos os peers da federação e separa eles de acordo
     * com o tamanho do arquivo, criando uma lista somente com os peers que
     * possuem condições de receber o arquivo
     *
     * @param lengthFile
     * @return - Lista com peers que podem receber o arquivo
     */
    public List<NodeInfo> getNodeDisp(long lengthFile) {
        List<NodeInfo> nodesdisp = new ArrayList<NodeInfo>();
        Collection<PluginInfo> cloudPlugin = getPeers().values();
        nodesdisp.clear();
        for (PluginInfo plugin : cloudPlugin) {
            try {
                NodeInfo node = new NodeInfo();

                if ((long) (plugin.getFsFreeSize() * MAXCAPACITY) > lengthFile && plugin.getId().equals(p2p.getConfig().getId())) {
                    node.setLatency(Ping.calculo(plugin.getHost().getAddress()));
                    node.setAddress(plugin.getHost().getAddress());
                    node.setFreesize(plugin.getFsFreeSize());
                    node.setPeerId(plugin.getId());
                    nodesdisp.add(node);
                }
            } catch (IOException ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return nodesdisp;
    }

    /**
     * Seta no Zookeeper os dados de um arquivo que foi requisitado por um
     * cliente para ser submetido na federação
     *
     * @param file - Arquivo a ser submetido
     */
    public void setPendingFile(PluginFile file) {
        zkService.createPersistentZNode(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", file.getId(), ""), file.toString());
    }

    /**
     * Cria uma Map com o ID de um peer e seus respectivos arquivos
     *
     * @param pluginId id do plugin para pegar os arquivos do plugin
     * @return Map com os plugins e seus arquivos
     */
    public List<PluginFile> getFilesPeer(String pluginId) {
        List<String> children;
        List<PluginFile> filesPeerSelected = new ArrayList<PluginFile>();
        checkFiles();
        try {
            children = zkService.getChildren(zkService.getPath().FILES.getFullPath(pluginId, "", ""), null);
            for (String fileId : children) {
                String fileName = fileId.substring(5, fileId.length());
                if (savedFiles.containsKey(fileName)) {
                    filesPeerSelected.add(savedFiles.get(fileName));
                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return filesPeerSelected;
    }

    /**
     * 
     */
    @Override
    public void verifyPlugins() {
        Collection<PluginInfo> temp = getPeers().values();
        temp.removeAll(cloudMap.values());
        for (PluginInfo plugin : temp) {
            try {
                zkService.getData(zkService.getPath().STATUS.getFullPath(plugin.getId(), "", ""), new UpdatePeerData(zkService, this));
            } catch (KeeperException ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Método que recebe um evento do zookeeper caso os znodes setados nessa classe sofra alguma alteração, criado, deletado, modificado, trata os eventos de acordo com o tipo do mesmo
     * @param eventType 
     */
    @Override
    public void event(WatchedEvent eventType) {
        String path = eventType.getPath(); 
        switch (eventType.getType()) {

            case NodeChildrenChanged:
                if (eventType.getPath().equals(zkService.getPath().PEERS.toString())) {
                    if (cloudMap.size() < getPeers().size()) {
                        verifyPlugins();
                    }
                }
            case NodeDeleted:
                if (eventType.getPath().contains(zkService.getPath().STATUS.toString())) {
                    System.out.println("StoringService status apagada");
                    String peerId = path.substring(12, path.indexOf("/STATUS"));
                    try {
                        if (!zkService.getZNodeExist(zkService.getPath().STATUSWAITING.getFullPath(peerId, "", ""), false)) {
                            zkService.createPersistentZNode(zkService.getPath().STATUSWAITING.getFullPath(peerId, "", ""), "");              
                        }
                        if (!zkService.getData(zkService.getPath().STATUSWAITING.getFullPath(peerId, "", ""), null).contains("S")) {
                            //Verificar pluginid para gravar
                            for (PluginFile fileExcluded : getFilesPeer(peerId)) {
                                String idPluginExcluded = null;
                                for (String idPlugin : fileExcluded.getPluginId()) {
                                    if (peerId.equals(idPlugin)) {fileExcluded.getPluginId().remove(idPluginExcluded);
                                        idPluginExcluded = idPlugin;
                                        break;
                                    }
                                }
                                fileExcluded.getPluginId().remove(idPluginExcluded);
                                
                                setPendingFile(fileExcluded);
                                fileExcluded.setService("storagePeerDown");
                                fileUploaded(fileExcluded);
                            }
                            StringBuilder info = new StringBuilder(zkService.getData(zkService.getPath().STATUSWAITING.getFullPath(peerId, "", ""), null));
                            info.append("S");
                            zkService.setData(zkService.getPath().STATUSWAITING.getFullPath(peerId, "", ""), info.toString());
                            checkingPendingSave();
                        }

                    } catch (AvroRemoteException ex) {
                        Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (KeeperException ex) {
                        Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
        }
    }
}
