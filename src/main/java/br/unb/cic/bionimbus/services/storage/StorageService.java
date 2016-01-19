package br.unb.cic.bionimbus.services.storage;

import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.security.Hash;
import br.unb.cic.bionimbus.security.Integrity;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.toSort.Listeners;
import br.unb.cic.bionimbus.utils.Nmap;
import br.unb.cic.bionimbus.utils.Put;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
import org.apache.avro.AvroRemoteException;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StorageService extends AbstractBioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    @Inject
    private final MetricRegistry metricRegistry;
    private final ScheduledExecutorService executorService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("StorageService-%d").build());
    private Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<>();
    private final Map<String, PluginFile> savedFiles = new ConcurrentHashMap<>();
//    private Set<String> pendingSaveFiles = new HashSet<String>();
    private final File dataFolder = new File("data-folder"); //TODO: remover hard-coded e colocar em node.yaml e injetar em StorageService
    private final Double MAXCAPACITY = 0.9;
    private final int PORT = 8080;
    private final int REPLICATIONFACTOR = 2;
    private final List<String> listFile = new ArrayList<>();

    @Inject
    public StorageService(final CloudMessageService cms, MetricRegistry metricRegistry) {

        Preconditions.checkNotNull(cms);
        this.cms = cms;

        this.metricRegistry = metricRegistry;
    }

    @Override
    public void run() {
    }

    /**
     * Método que inicia a storage
     *
     * @param config
     * @param listeners
     */
    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {
        this.config = config;
        this.listeners = listeners;
        if (listeners != null) {
            listeners.add(this);
        }
        //Criando pastas zookeeper para o módulo de armazenamento
        if (!cms.getZNodeExist(Path.PENDING_SAVE.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.PENDING_SAVE.getFullPath(), null);
        }
        if (!cms.getZNodeExist(Path.FILES.getFullPath(config.getId()), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.FILES.getFullPath(config.getId()), "");
        }

        //watcher para verificar se um pending_save foi lançado
        cms.getChildren(Path.PENDING_SAVE.getFullPath(), new UpdatePeerData(cms, this));
        cms.getChildren(Path.PEERS.getFullPath(), new UpdatePeerData(cms, this));

        //NECESSARIO atualizar a lista de arquivo local , a lista do zookeeper com os arquivos locais.
        checkFiles();
        checkPeers();
        try {
            if (getPeers().size() != 1) {
                checkReplicationFiles();
            }
        } catch (Exception ex) {
            LOGGER.error("[Exception] - " + ex.getMessage());
        }
        executorService.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        listeners.remove(this);
        executorService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub
    }

    /**
     * Verifica os peers(plugins) existentes e adiciona um observador(watcher)
     * no zNode STATUS de cada plugin.
     */
    public void checkPeers() {
        for (PluginInfo plugin : getPeers().values()) {
            if (cms.getZNodeExist(Path.STATUS.getFullPath(plugin.getId()), null)) {
                cms.getData(Path.STATUS.getFullPath(plugin.getId()), new UpdatePeerData(cms, this));
            }
        }
    }

    /**
     * Verifica os arquivos que existem no recurso. Alterado para synchronized
     * para evitar condição de corrida.
     */
    public synchronized void checkFiles() {
        try {
            if (!dataFolder.exists()) {
//                System.out.println(" (CheckFiles) dataFolder " + dataFolder + " doesn't exists, creating...");
                dataFolder.mkdirs();
            }
            cms.getChildren(Path.FILES.getFullPath(config.getId()), new UpdatePeerData(cms, this));
            for (File file : dataFolder.listFiles()) {
                if (!savedFiles.containsKey(file.getName())) {
                    PluginFile pluginFile = new PluginFile();
                    pluginFile.setId(file.getName());
                    pluginFile.setName(file.getName());
                    pluginFile.setPath(file.getPath());

                    List<String> listIds = new ArrayList<>();
                    listIds.add(config.getId());

                    pluginFile.setPluginId(listIds);
                    pluginFile.setSize(file.length());
                    pluginFile.setHash(Hash.calculateSha3(file.getPath()));
                    //cria um novo znode para o arquivo e adiciona o watcher
                    cms.createZNode(CreateMode.PERSISTENT, Path.NODE_FILE.getFullPath(config.getId(), pluginFile.getId()), pluginFile.toString());
                    cms.getData(Path.NODE_FILE.getFullPath(config.getId(), pluginFile.getId()), new UpdatePeerData(cms, this));

                    savedFiles.put(pluginFile.getName(), pluginFile);
                }

            }
        } catch (Exception ex) {
            LOGGER.error("[Exception] - " + ex.getMessage());
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
            for (String fileNamePlugin : collection) {
                if (!existReplication(fileNamePlugin)) {
                    /*
                     * Caso não exista um número de cópias igual a REPLICATIONFACTOR inicia as cópias,
                     * enviando uma RPC para o peer que possui o arquivo, para que ele replique.
                     */
                    String ipPluginFile = getIpContainsFile(fileNamePlugin);
                    if (!ipPluginFile.isEmpty() && !ipPluginFile.equals(config.getAddress())) {
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
        for (Collection<String> collection : getFiles().values()) {
            for (String fileNamePlugin : collection) {
                if (fileName.equals(fileNamePlugin)) {
                    cont++;
                }
            }
        }
        return cont >= REPLICATIONFACTOR;
    }

    /**
     * Cria map com endereço dos peers(plugins) e seus respectivos arquivos
     * baseado nos dados do zookeeper.
     *
     * @return map de endereço e lista de arquivos.
     * @throws java.io.IOException
     */
    public Map<String, List<String>> getFiles() throws IOException {
        Map<String, List<String>> mapFiles = new HashMap<>();
        List<String> listFiles;
        checkFiles();

        for (PluginInfo plugin : getPeers().values()) {
            listFiles = new ArrayList<String>();
            for (String file : cms.getChildren(Path.FILES.getFullPath(plugin.getId()), new UpdatePeerData(cms, this))) {
                listFiles.add(file);
            }
            mapFiles.put(plugin.getHost().getAddress(), listFiles);
        }

        return mapFiles;

    }

    /**
     * Metodo para pegar o Ip de cada peer na federação e verificar em qual peer
     * o arquivo está, se o arquivo for encontrado retorna o Ip do peer, caso
     * contrário retorna null.
     *
     * @param file
     * @return Ip que possui o arquivo ou null
     * @throws java.io.IOException
     */
    public String getIpContainsFile(String file) throws IOException {
        List<String> listFiles;
        //NECESSARIO atualizar a lista de arquivo local , a lista do zookeeper com os arquivos locais. Não é feito em nenhum momento
        //caso não seja chamado a checkFiles();
        checkFiles();

        for (Iterator<PluginInfo> it = getPeers().values().iterator(); it.hasNext();) {
            PluginInfo plugin = it.next();
            listFiles = cms.getChildren(Path.FILES.getFullPath(plugin.getId()), null);
            for (String checkfile : listFiles) {
                if (file.equals(checkfile)) {
                    return plugin.getHost().getAddress();
                }
            }
        }
        return "";
    }

    /**
     * Retorna o tamanho do arquivo, dado o nome do mesmo. NOTE: listFiles never
     * used. Revise this code.
     *
     * @param file O nome do arquivo
     * @return O tamanho do arquivo
     */
    public long getFileSize(String file) {

        try {
            List<String> listFiles;
            for (Iterator<PluginInfo> it = getPeers().values().iterator(); it.hasNext();) {
                PluginInfo plugin = it.next();
                listFiles = cms.getChildren(Path.FILES.getFullPath(plugin.getId()), null);

                PluginFile files = new ObjectMapper().readValue(cms.getData(Path.NODE_FILE.getFullPath(plugin.getId(), file), null), PluginFile.class);
                return files.getSize();
            }
        } catch (IOException ex) {
            LOGGER.error("[IOException] - " + ex.getMessage());
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
            cloudMap.get(node.getPeerId()).setLatency(node.getLatency());
            cloudMap.get(node.getPeerId()).setFsFreeSize(node.getFreesize());
        }
        StoragePolicy policy = new StoragePolicy();
        /*
         * Dentro da Storage Policy é feito o ordenamento da list de acordo com o custo de armazenamento
         */
        plugins = policy.calcBestCost(cms, cloudMap.values());

        return plugins;
    }

    /**
     * Verifica se um arquivo existe em um peer e seta o seu Znode no Zookeeper
     *
     * @param file - Arquivo a ser verifcado
     * @return true caso o arquivo exista e tenha sido setado
     */
    public boolean checkFilePeer(PluginFile file) {
        LOGGER.info("Verifying if file (filename=" + file.toString() + ") exists on peer");

        String pathHome = System.getProperty("user.dir");
        String path = (pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/");
        File localFile = new File(path + file.getName());

        if (localFile.exists()) {
            cms.createZNode(CreateMode.PERSISTENT, Path.NODE_FILE.getFullPath(config.getId(), file.getId()), file.toString());
            cms.getData(Path.NODE_FILE.getFullPath(config.getId(), file.getId()), new UpdatePeerData(cms, this));
            return true;
        }

        LOGGER.info("File not found on Peer");
        return false;
    }

    /**
     * Método que manda o comando dizendo que o arquivo foi upado com o intuito
     * de replicar esse arquivo pelos nós.
     *
     * @param fileUploaded
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     * @throws java.security.NoSuchAlgorithmException
     * @throws com.jcraft.jsch.SftpException
     */
    public synchronized String fileUploaded(PluginFile fileUploaded) throws KeeperException, InterruptedException, IOException, NoSuchAlgorithmException, SftpException {
        LOGGER.info("Checking if there is request on PENDING_SAVE " + fileUploaded.toString());

        Boolean successUpload = false;
        if (cms.getZNodeExist(Path.NODE_PENDING_FILE.getFullPath(fileUploaded.getId()), null)) {
            String ipPluginFile;
            ipPluginFile = getIpContainsFile(fileUploaded.getName());
            FileInfo file = new FileInfo();
            file.setId(fileUploaded.getId());
            file.setName(fileUploaded.getName());
            file.setSize(fileUploaded.getSize());
            file.setHash(fileUploaded.getHash());
            String idPluginFile = null;

            for (String idPlugin : fileUploaded.getPluginId()) {
                idPluginFile = idPlugin;
                break;
            }

            LOGGER.info("IdPluginFile: " + idPluginFile);

            //Verifica se a máquina que recebeu essa requisição não é a que está armazenando o arquivo
            if (!fileUploaded.getPluginId().contains(config.getId())) {
                for (PluginInfo plugin : getPeers().values()) {
                    if (plugin.getId().equals(fileUploaded.getPluginId().get(0))) {
                        ipPluginFile = plugin.getHost().getAddress();
                    }
                }
                RpcClient rpcClient = new AvroClient("http", ipPluginFile, PORT);
                String filePeerHash = rpcClient.getProxy().getFileHash(fileUploaded.getName());

                //Verifica se o arquivo foi corretamente transferido ao nó.
                if (Integrity.verifyHashes(filePeerHash, fileUploaded.getHash())) {
                    successUpload = true;
                    try {
                        if (rpcClient.getProxy().verifyFile(file, fileUploaded.getPluginId()) && cms.getZNodeExist(Path.NODE_FILE.getFullPath(idPluginFile, fileUploaded.getId()), null)) {
                            //Remova o arquivo do PENDING FILE já que ele foi upado
                            cms.delete(Path.NODE_PENDING_FILE.getFullPath(fileUploaded.getId()));
                        }
                        rpcClient.close();
                    } catch (Exception ex) {
                        LOGGER.error("[Exception] - " + ex.getMessage());
                    }
                }
            } else {
                if (checkFilePeer(fileUploaded)) {
                    String pathHome = System.getProperty("user.dir");
                    String path = (pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/");
                    String filePeerHash = Hash.calculateSha3(path + fileUploaded.getName());

                    //Verifica se o arquivo foi corretamente transferido ao peer.
                    if (Integrity.verifyHashes(filePeerHash, fileUploaded.getHash())) {
                        successUpload = true;
                        if (cms.getZNodeExist(Path.NODE_FILE.getFullPath(idPluginFile, fileUploaded.getId()), null) && !existReplication(file.getName())) {
                            try {
                                replication(file.getName(), config.getAddress());
                                if (existReplication(file.getName())) {
                                    //Remova o arquivo do PENDING FILE já que ele foi upado
                                    cms.delete(Path.NODE_PENDING_FILE.getFullPath(fileUploaded.getId()));
                                } else {
                                    LOGGER.info("Replication failed! File wasn't replicated...");
                                }
                            } catch (JSchException ex) {
                                LOGGER.error("[JSchException] - " + ex.getMessage());
                            }
                        }
                    }
                }
            }
        } else {
            LOGGER.info("File not found on pending files");
        }

        if (successUpload) {
            LOGGER.info("File integrity verified: File uploaded correctly");

            return "File integrity verified: File uploaded correctly.";
        } else {
            LOGGER.info("File integrity verified: Error on file uploading.");

            return "File integrity verified: Error on file uploading.";
        }
    }

    /**
     * Metodo que checa os znodes filhos da pending_save, para replica-lós
     *
     * @throws java.io.IOException
     * @throws java.security.NoSuchAlgorithmException
     */
    public void checkingPendingSave() throws IOException, NoSuchAlgorithmException {

        ObjectMapper mapper = new ObjectMapper();
        Boolean validFile = true;
        Integrity integrity = new Integrity();
        int cont = 0;
        List<String> pendingSave = cms.getChildren(Path.PENDING_SAVE.getFullPath(), null);
//            pendingSaveFiles.addAll(pendingSave);
        for (String files : pendingSave) {
            try {
                String data = cms.getData(Path.NODE_PENDING_FILE.getFullPath(files.substring(13, files.length())), null);
                //verifica se arquivo existe
                if (data == null || data.trim().isEmpty()) {
                    LOGGER.info("========> There is no data for Path: " + Path.PENDING_SAVE.getFullPath());

                    continue;
                }
                PluginFile fileplugin = mapper.readValue(data, PluginFile.class);

                //Verifica se é um arquivo de saída de uma execução e se o arquivo foi gerado nesse recurso
                if (fileplugin.getService() != null && fileplugin.getService().equals(SchedService.class.getSimpleName()) && fileplugin.getPluginId().get(0).equals(config.getId())) {
                    //Adiciona o arquivo a lista do zookeeper
                    checkFiles();
                }
                while (cont < 6) {
                    if (fileplugin.getPluginId().size() == REPLICATIONFACTOR) {
                        cms.delete(Path.PENDING_SAVE.getFullPath(fileplugin.getId()));
                        break;
                    }
                    String address = getIpContainsFile(fileplugin.getName());
                    if (!address.isEmpty() && !address.equals(config.getAddress())) {
                        RpcClient rpcClient = new AvroClient("http", address, PORT);
                        rpcClient.getProxy().notifyReply(fileplugin.getName(), address);
                        try {
                            rpcClient.close();
                            if (existReplication(fileplugin.getName())) {
                                cms.delete(Path.NODE_PENDING_FILE.getFullPath(fileplugin.getId()));
                                break;
                            } else {
                                cont++;
                            }
                        } catch (Exception ex) {
                            LOGGER.error("[Exception] - " + ex.getMessage());
                        }
                    } else {
                        try {
                            replication(fileplugin.getName(), address);
                        } catch (JSchException ex) {
                            LOGGER.error("[JSchException] - " + ex.getMessage());
                        } catch (SftpException ex) {
                            LOGGER.error("[SftpException] - " + ex.getMessage());
                        }
                        if (existReplication(fileplugin.getName())) {
                            cms.delete(Path.NODE_PENDING_FILE.getFullPath(fileplugin.getId()));
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                LOGGER.error("[IOException] - " + ex.getMessage());
            }
            //verifica se exite replicação quando houver mais de um peer
            if (getPeers().size() != 1) {
                existReplication(files);
            }
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
     * @throws java.io.FileNotFoundException
     * @throws java.security.NoSuchAlgorithmException
     */
    public synchronized void replication(String filename, String address) throws IOException, JSchException, SftpException, FileNotFoundException, NoSuchAlgorithmException {
        LOGGER.info("Replicating file (filename=" + filename + ") from peer (peer=" + address + ")");

        List<NodeInfo> pluginList = new ArrayList<>();
        List<String> idsPluginsFile = new ArrayList<>();
        String pathHome = System.getProperty("user.dir");
        String path = (pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/");
        File file = new File(path + filename);

        int filesreplicated = 1;

        //Verifica se o arquivo existe no peer        
        if (file.exists()) {
            FileInfo info = new FileInfo();
            info.setId(file.getName());
            info.setName(file.getName());
            info.setSize(file.length());
            info.setHash(Hash.calculateSha3(file.getAbsolutePath()));

            PluginFile pluginFile = new PluginFile(info);
            /*
             * PLuginList ira receber a lista dos Peers disponiveis na federação
             * e que possuem espaço em disco para receber o arquivo a ser replicado
             */
            pluginFile.setPath("data-folder/" + info.getName());
            NodeInfo no = null;

            /*
             * While para que o peer pegue o próprio endereço e ele seja removido da lista de peers,
             * isso é feito para evitar que ele tente replicar
             * o arquivo para ele mesmo.
             */
            for (NodeInfo node : getNodeDisp(info.getSize())) {
                if (node.getAddress().equals(address)) {
                    no = node;
                    break;
                }
            }
            if (no != null) {
                pluginList.remove(no);
                idsPluginsFile.add(config.getId());
                pluginList = new ArrayList<>(bestNode(pluginList));
                for (NodeInfo curr : pluginList) {
                    if (no.getAddress().equals(curr.getAddress())) {
                        no = curr;
                        break;
                    }
                }

                pluginList.remove(no);
            }
            pluginList = new ArrayList<>(bestNode(pluginList));
            pluginList.remove(no);
            Iterator<NodeInfo> bt = pluginList.iterator();
            while (bt.hasNext() && filesreplicated != REPLICATIONFACTOR) {
                NodeInfo node = (NodeInfo) bt.next();
                if (!(node.getAddress().equals(address))) {
                    //Descoberto um peer disponivel, tenta enviar o arquivo                    
                    Put conexao = new Put(node.getAddress(), dataFolder + "/" + info.getName());
                    if (conexao.startSession()) {
                        idsPluginsFile.add(node.getPeerId());

                        pluginFile.setPluginId(idsPluginsFile);

                        RpcClient rpcClient = new AvroClient("http", node.getAddress(), PORT);
                        String fileReplicatedHash = rpcClient.getProxy().getFileHash(pluginFile.getName());
                        //rpcClient.close();

                        //Verifica se o arquivo foi corretamente transferido ao nó.
                        if (Integrity.verifyHashes(pluginFile.getHash(), fileReplicatedHash)) {
                            //Com o arquivo enviado, seta os seus dados no Zookeeper                        
                            for (String idPlugin : idsPluginsFile) {
                                if (cms.getZNodeExist(Path.NODE_FILE.getFullPath(idPlugin, filename), null)) {
                                    cms.setData(Path.NODE_FILE.getFullPath(idPlugin, filename), pluginFile.toString());
                                } else {
                                    cms.createZNode(CreateMode.PERSISTENT, Path.NODE_FILE.getFullPath(idPlugin, filename), pluginFile.toString());
                                }
                                cms.getData(Path.NODE_FILE.getFullPath(idPlugin, filename), new UpdatePeerData(cms, this));
                            }
                        } else {
                            LOGGER.info("Error replicating the file to the peer (peer=" + node.getAddress() + ")");
                        }
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
        List<NodeInfo> nodesdisp = new ArrayList<>();
        Collection<PluginInfo> cloudPlugin = getPeers().values();
        nodesdisp.clear();
        for (PluginInfo plugin : cloudPlugin) {
            try {
                NodeInfo node = new NodeInfo();

                if ((long) (plugin.getFsFreeSize() * MAXCAPACITY) > lengthFile && plugin.getId().equals(config.getId())) {
                    node.setLatency(Ping.calculo(plugin.getHost().getAddress()));
                    if (node.getLatency().equals(Double.MAX_VALUE)) {
                        node.setLatency(Nmap.nmap(plugin.getHost().getAddress()));
                    }
                    node.setAddress(plugin.getHost().getAddress());
                    node.setFreesize(plugin.getFsFreeSize());
                    node.setPeerId(plugin.getId());
                    nodesdisp.add(node);
                }
            } catch (IOException ex) {
                LOGGER.error("[IOException] - " + ex.getMessage());
            } catch (InterruptedException ex) {
                LOGGER.error("[InterruptedException] - " + ex.getMessage());
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
        cms.createZNode(CreateMode.PERSISTENT, Path.NODE_PENDING_FILE.getFullPath(file.getId()), file.toString());
    }

    /**
     * Cria uma Map com o ID de um peer e seus respectivos arquivos
     *
     * @param pluginId id do plugin para pegar os arquivos do plugin
     * @return Map com os plugins e seus arquivos
     */
    public List<PluginFile> getFilesPeer(String pluginId) {
        List<String> children;
        List<PluginFile> filesPeerSelected = new ArrayList<>();
        //NECESSARIO atualizar a lista de arquivo local , a lista do zookeeper com os arquivos locais. Não é feito em nenhum momento
        //caso não seja chamado a checkFiles();
        checkFiles();
        try {
            children = cms.getChildren(Path.FILES.getFullPath(pluginId), null);
            for (String fileId : children) {
                String fileName = fileId.substring(5, fileId.length());
                ObjectMapper mapper = new ObjectMapper();
                PluginFile file = mapper.readValue(cms.getData(Path.NODE_FILE.getFullPath(pluginId, fileName), null), PluginFile.class);
                filesPeerSelected.add(file);
            }
        } catch (IOException ex) {
            LOGGER.error("[IOException] - " + ex.getMessage());
        }

        return filesPeerSelected;
    }

    @Override
    public void verifyPlugins() {
        Collection<PluginInfo> temp = getPeers().values();
        temp.removeAll(cloudMap.values());
        for (PluginInfo plugin : temp) {
            if (cms.getZNodeExist(Path.STATUS.getFullPath(plugin.getId(), null, null), null)) {
                cms.getData(Path.STATUS.getFullPath(plugin.getId()), new UpdatePeerData(cms, this));
            }
        }
    }

    /**
     * Método que recebe um evento do zookeeper caso os znodes setados nessa
     * classe sofra alguma alteração, criado, deletado, modificado, trata os
     * eventos de acordo com o tipo do mesmo
     *
     * @param eventType
     */
    @Override
    public void event(WatchedEvent eventType) {
        String path = eventType.getPath();
        switch (eventType.getType()) {

            case NodeChildrenChanged:
                if (eventType.getPath().equals(Path.PEERS.toString())) {

                    if (cloudMap.size() < getPeers().size()) {
                        verifyPlugins();
                    }
                } else if (eventType.getPath().equals(Path.PENDING_SAVE.toString())) {
                    //chamada para checar a pending_save apenas quando uma alerta para ela for lançado
//                       try{
//                            checkingPendingSave();
//                        }
//                        catch (IOException ex) {
//                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                }
                break;
            case NodeDeleted:
                if (eventType.getPath().contains(Path.STATUS.toString())) {
                    LOGGER.info("Erased ZNode Status");
                    
                    String peerId = path.substring(12, path.indexOf("/STATUS"));
                    if (getPeers().values().size() != 1) {
                        try {
                            if (!cms.getZNodeExist(Path.STATUSWAITING.getFullPath(peerId), null)) {
                                cms.createZNode(CreateMode.PERSISTENT, Path.STATUSWAITING.getFullPath(peerId), "");
                            }

                            StringBuilder info = new StringBuilder(cms.getData(Path.STATUSWAITING.getFullPath(peerId), null));
                            //verifica se recurso já foi recuperado ou está sendo recuperado por outro recurso
                            if (!info.toString().contains("S") /*&& !info.toString().contains("L")*/) {

                                //bloqueio para recuperar tarefas sem que outros recursos realizem a mesma operação
                                // cms.setData(Path.STATUSWAITING.getFullPath(peerId), info.append("L").toString());
                                //Verificar pluginid para gravar
                                for (PluginFile fileExcluded : getFilesPeer(peerId)) {
                                    String idPluginExcluded = null;
                                    for (String idPlugin : fileExcluded.getPluginId()) {
                                        if (peerId.equals(idPlugin) && !idPlugin.equals(config.getId())) {
                                            idPluginExcluded = idPlugin;
                                            break;
                                        }
                                    }

                                    if (fileExcluded.getPluginId().size() > 1) {
                                        fileExcluded.getPluginId().remove(idPluginExcluded);
                                    }

                                    setPendingFile(fileExcluded);
                                    fileExcluded.setService("storagePeerDown");
                                    fileUploaded(fileExcluded);
                                }

                                //retira bloqueio de uso e adiciona marcação de recuperação
                                //    info.deleteCharAt(info.indexOf("L"));
                                info.append("S");
                                cms.setData(Path.STATUSWAITING.getFullPath(peerId), info.toString());

                                //nao é necessário chamar esse método aqui, ele será chamado se for necessário ao receber um alerta de watcher
                                //                            checkingPendingSave();
                            }

                        } catch (AvroRemoteException ex) {
                            LOGGER.error("[AvroRemoteException] - " + ex.getMessage());
                        } catch (KeeperException ex) {
                            LOGGER.error("[KeeperException] - " + ex.getMessage());
                        } catch (InterruptedException ex) {
                            LOGGER.error("[InterruptedException] - " + ex.getMessage());
                        } catch (IOException ex) {
                            LOGGER.error("[IOException] - " + ex.getMessage());
                        } catch (NoSuchAlgorithmException ex) {
                            LOGGER.error("[NoSuchAlgorithmException] - " + ex.getMessage());
                        } catch (SftpException ex) {
                            LOGGER.error("[SftpException] - " + ex.getMessage());
                        }
                    }
                    break;
                }
        }
    }
}
