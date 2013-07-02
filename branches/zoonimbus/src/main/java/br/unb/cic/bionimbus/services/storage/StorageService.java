package br.unb.cic.bionimbus.services.storage;

import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.BioProtoImpl;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
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
    private DiscoveryService discoveryService;
//        private static final String PENDING_SAVE="/pending_save";
    private Double MAXCAPACITY = 0.9;
    private int PORT =9999;
    private int copies = 2;
    @Inject
    public StorageService(final ZooKeeperService service, MetricRegistry metricRegistry, DiscoveryService disc) {

        Preconditions.checkNotNull(service);
        this.zkService = service;
        this.discoveryService = disc;

        this.metricRegistry = metricRegistry;
        // teste
        Counter c = metricRegistry.counter("teste");
        c.inc();
    }

    @Override
    public void run() {
        
        System.out.println("Running StorageService...");
        System.out.println("Executando loop.");

    }

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
            zkService.getChildren(zkService.getPath().PENDING_SAVE.getFullPath("","",""),new UpdatePeerData(zkService, this));
        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }

        checkFiles();
        checkPeers();
        try {
            checkReplicationFiles();
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
     * Verifica os peers(plugins) existentes e adiciona um observador(watcher) no zNode STATUS de cada plugin.
     */
    public void checkPeers(){
        for(PluginInfo plugin : getPeers().values()){
            try {
                zkService.getData(zkService.getPath().STATUS.getFullPath(plugin.getId(), null, null), new UpdatePeerData(zkService,this));
            } catch (KeeperException ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    /**
     * Verifica os arquivos que existem no recurso e se o fator de replicação está sendo respeitado na federação.
     * Roda apenas quando o plugin está sendo iniciado.
     */
    public void checkFiles(){
        try {
            if (!dataFolder.exists()) {
                System.out.println("dataFolder " + dataFolder + " doesn't exists, creating...");
                dataFolder.mkdirs();
            }

            zkService.getChildren(zkService.getPath().FILES.getFullPath(p2p.getConfig().getId(),"",""), new UpdatePeerData(zkService, this));

            for(File file : dataFolder.listFiles()){
              if(existReplication(file.getName())){
                  if(file.delete())
                      System.out.println("Arquivo Apagado!");
              }else{
                    PluginFile pluginFile =  new PluginFile();
                    pluginFile.setId(file.getName());
                    pluginFile.setName(file.getName());
                    pluginFile.setPath(file.getPath());
                    //list ids Verificar onde esse arquivo está caso já exista
                    List<String> listIds = new ArrayList<String>();
                    listIds.add(p2p.getConfig().getPlugin());

                    pluginFile.setPluginId(listIds);
                    pluginFile.setSize(file.length());
                    //checkFilesReplication();
                    zkService.createPersistentZNode(zkService.getPath().PREFIX_FILE.getFullPath(p2p.getConfig().getId(),pluginFile.getId(),""),pluginFile.toString());
                    zkService.getData(zkService.getPath().PREFIX_FILE.getFullPath(p2p.getConfig().getId(),pluginFile.getId(),""), new UpdatePeerData(zkService, this));
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
     * 
     */
    public void checkReplicationFiles() throws Exception{    
       for(Collection<String> collection : getFiles().values()){
            for (Iterator<String> it = collection.iterator(); it.hasNext();) {
                String fileNamePlugin = it.next();
                if(!existReplication(fileNamePlugin)){
                   String ipPluginFile =getFilesIP(fileNamePlugin);
                   RpcClient rpcClient = new AvroClient("http", ipPluginFile, PORT);
                   rpcClient.getProxy().notifyReply(fileNamePlugin,ipPluginFile);
                   rpcClient.close();
                }
            }
       }
    }
    
    /**
     * Verifica a existência da replicação do arquivo na federação. Se a replicação estiver feita retona true.
     * Fator de replicação igual a 2.
     * return true se existir replicação.
     */
    private boolean existReplication(String fileName){
        int cont=0;
        for(Collection<String> collection : getFiles().values()){
            for (Iterator<String> it = collection.iterator(); it.hasNext();) {
                String fileNamePlugin = it.next();
                if(fileName.equals(fileNamePlugin))
                    cont++;
            }
        }
        if(cont<=2)
            return false;
        
        return true;
    }
    
    /**
     * Cria map com endereço dos peers(plugins) e seus respectivos arquivos baseado nos dados do 
     * zookeeper.
     * @return map de endereço e lista de arquivos.
     */
    public Map<String,List<String>> getFiles(){
        Map<String,List<String>> mapFiles = new HashMap<String, List<String>>();
        List<String> listFiles ;
        try {
            for(PluginInfo plugin : getPeers().values()){
                listFiles = new ArrayList<String>();
                for(String file : zkService.getChildren(plugin.getPath_zk()+zkService.getPath().FILES.toString(), new UpdatePeerData(zkService, this))){
                    listFiles.add(file.substring(5,file.length()));
                }
                mapFiles.put(plugin.getHost().getAddress(),listFiles);
            }
        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return mapFiles;
        
    }
    
    /**
     * Metodo para pegar o Ip de cada peer na federação e verificar se um arquivo está com este peer,
     * se o arquivo for encontrado retorna o Ip do peer, caso contrário retorna null.
     * @param file
     * @return
     */
    public String getFilesIP(String file){
        List<String> listFiles ;
       // Map<String,List<String>> mapFiles = new HashMap<String, List<String>>();
        try {
            for (Iterator<PluginInfo> it = getPeers().values().iterator(); it.hasNext();) {
                PluginInfo plugin = it.next();
                listFiles = zkService.getChildren(plugin.getPath_zk()+zkService.getPath().FILES.toString(),null);
                for(String checkfile : listFiles){
                  
                    String idfile=checkfile.substring(checkfile.indexOf(zkService.getPath().UNDERSCORE.toString())+1);
                    if(file.equals(idfile)){
                        return plugin.getHost().getAddress();
                    }
                }
            }
            return "Arquivo nao encontrado";
        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
        
    }
    
    /**
     * Método que recebe uma list com todos os peers da federação e seta o custo de armazenamento em casa plugin
     * @param list - Lista com todos os plugins da federação
     * @return - Lista com todos os plugins com seus custos de armazenamento inseridos
     */
    public List<NodeInfo> bestNode(List<NodeInfo> list){
        
        List<NodeInfo> plugins;
        cloudMap = getPeers();
            for(NodeInfo node : list){
               cloudMap.get(node.getPeerId()).setLatency(node.getLatency());
            }
        StoragePolicy policy = new StoragePolicy();
        plugins = policy.calcBestCost(zkService,cloudMap.values());

        return plugins;
    }
    
    public void fileUploaded(PluginFile fileuploaded) throws KeeperException, InterruptedException{
        if (zkService.getZNodeExist(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", fileuploaded.getId(), ""), false)){    
           String ipPluginFile =getFilesIP(fileuploaded.getName());
           RpcClient rpcClient = new AvroClient("http",ipPluginFile, PORT);
            try {
                rpcClient.getProxy().verifyFile(fileuploaded.getName(),ipPluginFile);
            } catch (IOException ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                rpcClient.close();
            } catch (Exception ex) {
                Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
            }
           if(zkService.getZNodeExist(zkService.getPath().PREFIX_FILE.getFullPath(fileuploaded.getPluginId().iterator().next(),fileuploaded.getId(), ""), true));
              zkService.delete(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("",fileuploaded.getId(), ""));
        }else
            System.out.println("Arquivo não encontrado nas pendências !");
        
    }
    
    
    /**
     * Realiza a transferencia de arquivos de um servidor Bionimbus para os outros peers,
     * usada para a replicação de arquivos.
     * @param plugins - Lista de plugins com espaço livre disponivel para armazenamento
     * @param path - Caminho do arquivo que será copiado
     * @param copies - Número de cópias que se deseja na replicação.
     * @param idPluginCopy
     * @throws AvroRemoteException
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void transferFiles(List<NodeInfo> plugins, String fileName, int copies,List<String> idsPluginCopy) throws AvroRemoteException, KeeperException, InterruptedException{
        
        int aux=1;
        int flag=1;
        
        for (Iterator<NodeInfo> it = plugins.iterator(); it.hasNext();) {
                 NodeInfo node = it.next();         
                    Put conexao = new Put(node.getAddress(),fileName,flag);   
                    try {
                        if(conexao.startSession()){
                            aux++;
                            if(aux == copies){
                                //Começar daqui aamanha
                                for(String idPluginCopy : idsPluginCopy){
                                  ObjectMapper mapper = new ObjectMapper();
                                    try {
                                        PluginFile file = mapper.readValue(zkService.getData(
                                                                           zkService.getPath().PREFIX_FILE.getFullPath(
                                                                           idPluginCopy,fileName,""), null),PluginFile.class);
                                        file.setPluginId(idsPluginCopy);
                                        if(zkService.getZNodeExist(zkService.getPath().PREFIX_FILE.getFullPath(idPluginCopy, fileName,""), true))
                                            zkService.setData(zkService.getPath().PREFIX_FILE.getFullPath(idPluginCopy, fileName,""), file.toString());
                                        else 
                                            System.out.println("Não existe o nó!");
                                    } catch (IOException ex) {
                                        Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                System.out.println("\n Replication Completed !! No de destino : "+node.getAddress());
                                break;
                            }
                        }
                    } catch (SftpException ex) {
                    Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JSchException ex) {
                         Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 
        }
        
    }
    
    public void replication (String filename,String address) throws IOException, JSchException, SftpException{
        
        List<NodeInfo> nodesdisp = new ArrayList<NodeInfo>();
        List<NodeInfo> pluginList = new ArrayList<NodeInfo>();
        File file = new File(filename);
        int flag =1;
        int filesreplicated = 1;
        
        if(file.exists()){
            FileInfo info = new FileInfo();            
            info.setFileId(file.getName());
            info.setName(file.getName());
            info.setSize(file.length());           
            
            pluginList = getNodeDisp(info.size);
            
            System.out.println("\n Calculando Latencia.....");
            for (Iterator<NodeInfo> it = pluginList.iterator(); it.hasNext();) {
                NodeInfo plugin = it.next();
                   plugin.setLatency(Ping.calculo(plugin.getAddress()));
                   nodesdisp.add(plugin);
                }    
            nodesdisp = new ArrayList<NodeInfo>(bestNode(nodesdisp));
            NodeInfo no=null;
            Iterator<NodeInfo> it = nodesdisp.iterator();
            while (it.hasNext() && filesreplicated != copies) {
                 NodeInfo node = (NodeInfo)it.next();
                 if(node.getAddress().equals(address)){
                    Put conexao = new Put(node.getAddress(),info.getName(),flag);                
                    if(conexao.startSession()){
                       filesreplicated++;
                       System.out.println("\n File Replicated !!");
                    }
                 }
            }
            
         }     
    } 
    
    public List<NodeInfo> getNodeDisp(long lengthFile){
        List<NodeInfo> nodesdisp = new ArrayList<NodeInfo>();
        Collection<PluginInfo> cloudPlugin=getPeers().values();
        nodesdisp.clear();
        for(PluginInfo plugin: cloudPlugin){
                        try {
                              NodeInfo node = new NodeInfo();
                              if ((long)(plugin.getFsFreeSize()*MAXCAPACITY)>lengthFile){
                                node.setLatency(Ping.calculo(plugin.getHost().getAddress()));
                                node.setAddress(plugin.getHost().getAddress());
                                node.setFreesize(plugin.getFsFreeSize());
                                node.setPeerId(p2p.getConfig().getPlugin());
                                nodesdisp.add(node);
                              }    
                              plugin.setLatency(Ping.calculo(plugin.getHost().getAddress()));
                        } catch (IOException ex) {
                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
        return nodesdisp;
    }
    
//    public void failOverStorage(String id) throws AvroRemoteException, KeeperException, InterruptedException{
//        Map<String,PluginFile>filesPeerDown= getFilesPeer(id);
//        //modificar pois está estático
//        int fatoreplicacao =1;
//    
//        for (PluginFile file : filesPeerDown.values())
//        {
//            File localFile= new File(dataFolder+"/"+file.getPath());
//            for (Iterator<String> it = file.getPluginId().iterator(); it.hasNext();) {
//                String pluginId;
//                pluginId = it.next();
//                if (localFile.exists() && pluginId.equals(p2p.getConfig().getId())){
//                    getNodeDisp(localFile.length());
//                    if(!file.getPluginId().remove(pluginId))
//                        System.out.println("Plugin não encontrado!!");
//                    transferFiles(bestNode(nodesdisp), localFile.getPath(),fatoreplicacao,file.getPluginId());
//                    StringBuilder info = new StringBuilder(zkService.getData(zkService.getPath().STATUSWAITING.getFullPath(pluginId, "", ""), null));
//                    info.append("S");
//                    zkService.setData(zkService.getPath().STATUSWAITING.getFullPath(pluginId, "", ""), info.toString());
//                }
//            }
//        }
//    }
    /**
     * 
     */
    public void setPendingFile(PluginFile file){
        file.setPath(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("",file.getId(),""));
        zkService.createPersistentZNode(file.getPath(), file.toString());
             String ipPluginFile =getFilesIP(file.getName());
    }
    /**
     * 
     * @param pluginId id do plugin para pegar os arquivos do plugin
     * @return 
     */
    public Map<String, PluginFile> getFilesPeer(String pluginId){
        List<String> children;
        Map<String,PluginFile>filesPeerSelected=new ConcurrentHashMap<String, PluginFile>(); 
                 filesPeerSelected.clear();
        try {
            children = zkService.getChildren(zkService.getPath().FILES.getFullPath(pluginId,"",""), null);
            for (String fileId : children) {
                ObjectMapper mapper = new ObjectMapper();
                PluginFile file = mapper.readValue(zkService.getData(zkService.getPath().PREFIX_FILE.getFullPath(pluginId, fileId, ""), null), PluginFile.class);
                    
                if(zkService.getZNodeExist(zkService.getPath().PREFIX_FILE.getFullPath(pluginId, fileId, ""), false)){ 
                    filesPeerSelected.put(file.getId(), file);
                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return filesPeerSelected;
    }
    
    @Override
    public void event(WatchedEvent eventType) {
               String path = eventType.getPath(); 
            switch(eventType.getType()){

                case NodeChildrenChanged:
                    System.out.println("\n\n Event get path"+path);
                    //como pegar o znode que foi mudado ......
//                    if(path.contains(zkService.getPath().PENDING_SAVE.toString())){
//                       String fileId =  path.substring(path.indexOf(zkService.getPath().PREFIX_PENDING_FILE.toString())+13);
//                       ObjectMapper mapper = new ObjectMapper();
//                        try {
//                            PluginFile file = mapper.readValue(zkService.getData(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", fileId, ""), null), PluginFile.class);
//                            for(String pluginId : file.getPluginId()){
//                                 if(p2p.getConfig().getId().equals(pluginId)){
//                                    System.out.println("\n\n Eu sou o peer que recebeu o arquivo");
//                                }
//                            }
//                           
//                       
//                        } catch (IOException ex) {
//                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
//   
//                        } catch (KeeperException ex) {
//                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
//                        }    
//                        
//                    }
                    System.out.print(path + "= NodeChildrenChanged");
                    break;
                case NodeDeleted:
                    if(eventType.getPath().contains(zkService.getPath().STATUS.toString())){
//                        String peerId =  path.substring(path.indexOf(zkService.getPath().PREFIX_FILE.toString())+5, path.indexOf("/STATUS"));
//                        try {
//                            if(!zkService.getZNodeExist(zkService.getPath().STATUSWAITING.getFullPath(peerId, "", ""), false)){
//                                zkService.createPersistentZNode(zkService.getPath().STATUSWAITING.getFullPath(peerId, "", ""), "");
//                            }
//                            if(!zkService.getData(zkService.getPath().STATUSWAITING.getFullPath(peerId, "", ""), null).contains("S")){
//                                failOverStorage(peerId);
//                            }
//                        } catch (AvroRemoteException ex) {
//                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (KeeperException ex) {
//                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                    }
                    break;
            }
    }

   
    
    
}
