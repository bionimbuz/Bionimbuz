package br.unb.cic.bionimbus.services.storage;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.BioProtoImpl;
import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.PeerNode;
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
import org.codehaus.jackson.type.TypeReference;

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
        private static final String PENDING_SAVE="/pending_save";


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
       // System.out.println("Cloudmap vazia?:"+cloudMap.values().isEmpty());
        System.out.println("Executando loop.");

        //  System.out.println(" \n Hosts: " + p2p.getConfig().getHost());

//                        Message msg = new CloudReqMessage(p2p.getPeerNode());
//                        p2p.broadcast(msg); // TODO isso e' broadcast?                        

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
        checkFiles();
        
        
        File file = new File("data-folder/persistent-storage.json");
        if (file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, PluginFile> map = mapper.readValue(new File("data-folder/persistent-storage.json"), new TypeReference<Map<String, PluginFile>>() {
                });
                if (filesChanged(map.values())) {
                    map = mapper.readValue(new File("data-folder/persistent-storage.json"), new TypeReference<Map<String, PluginFile>>() {
                    });
                }
                savedFiles = new ConcurrentHashMap<String, PluginFile>(map);
                for(PluginFile filePlugin: savedFiles.values()){
                    zkService.createEphemeralZNode("/peers/peer_"+filePlugin.getPluginId()+"/files/file_"+filePlugin.getId(), filePlugin.toString());  
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
//        
//        
//        if (!event.getType().equals(P2PEventType.MESSAGE)) {
//            return;
//        }
//
//        P2PMessageEvent msgEvent = (P2PMessageEvent) event;
//        Message msg = msgEvent.getMessage();
//        if (msg == null) {
//            return;
//        }
//
//        PeerNode receiver = null;
//        if (msg instanceof AbstractMessage) {
//            receiver = ((AbstractMessage) msg).getPeer();
//        }
//
//        switch (P2PMessageType.of(msg.getType())) {
//            case CLOUDRESP:
//                CloudRespMessage cloudMsg = (CloudRespMessage) msg;
//                /*   DiscoveryService data=new DiscoveryService(zkService);
//                 ConcurrentMap<String,PluginInfo> cloudData= data.getPeers();
//                 for (PluginInfo info : cloudData.values()) {
//                 cloudMap.put(info.getId(), info);
//                 }*/
//                break;
//            case STOREREQ:
//                StoreReqMessage storeMsg = (StoreReqMessage) msg;
//                sendStoreResp(storeMsg.getFileInfo(), storeMsg.getTaskId(), receiver);
//                break;
//            case STOREACK:
//                StoreAckMessage ackMsg = (StoreAckMessage) msg;
//                savedFiles.put(ackMsg.getPluginFile().getId(), ackMsg.getPluginFile());
//                try {
//                    ObjectMapper mapper = new ObjectMapper();
//                    mapper.writeValue(new File("persistent-storage.json"), savedFiles);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case LISTREQ:
//                p2p.sendMessage(receiver.getHost(), new ListRespMessage(p2p.getPeerNode(), savedFiles.values()));
//                break;
//            case GETREQ:
//                GetReqMessage getMsg = (GetReqMessage) msg;
//                PluginFile file = savedFiles.get(getMsg.getFileId());
//                // TODO Tem que verificar se o Id do file existe, ou melhor, caso o file seja null deve
//                // exibir uma mensagem de erro avisando que o id do arquivo informado não existe
//                for (PluginInfo plugin : cloudMap.values()) {
//                    if (plugin.getId().equals(file.getPluginId())) {
//                        p2p.sendMessage(receiver.getHost(), new GetRespMessage(p2p.getPeerNode(), plugin, file, getMsg.getTaskId()));
//                        return;
//                    }
//                }
//
//                //TODO mensagem de erro?
//                break;
//        }
    }

    public void sendStoreResp(FileInfo info, String taskId, PeerNode dest) {
        for (PluginInfo plugin : cloudMap.values()) {
            /*if (info.getSize() < plugin.getFsFreeSize()) {
             StoreRespMessage msg = new StoreRespMessage(p2p.getPeerNode(), plugin, info, taskId);
             p2p.sendMessage(dest.getHost(), msg);
             return;
             }*/
        }
    }

    /**  
     * Verifica se os arquivos listados no persistent storage existem, caso não
     * existam é gerado um novo persistent-storage.json
     *
     * @param files
     * @return
     * @throws IOException 
     */
     
    public boolean filesChanged(Collection<PluginFile> files) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Collection<PluginFile> savedFilesOld = files;
        for (PluginFile archive : files) {
            //    System.out.println("nome:" + archive.getPath());
            File arq = new File(archive.getPath());
            if (arq.exists() && checkFiles(archive, savedFilesOld)) {
                savedFiles.put(archive.getId(), archive);
            }
        }
        if (!savedFiles.isEmpty() && !savedFiles.equals(savedFilesOld)) {
            mapper.writeValue(new File("data-folder/persistent-storage.json"), savedFiles);
            return true;
        }
        return false;

    }
    public void checkFiles(){
        try {
            if (!dataFolder.exists()) {
                System.out.println("dataFolder " + dataFolder + " doesn't exists, creating...");
                dataFolder.mkdirs();
            }

            zkService.getChildren(zkService.getPath().FILES.getFullPath(p2p.getConfig().getId(),"",""), new UpdatePeerData(zkService, this));

            for(File file : dataFolder.listFiles()){
                PluginFile pluginFile =  new PluginFile();
                pluginFile.setId(file.getName());
                pluginFile.setName(file.getName());
                pluginFile.setPath(file.getPath());
                pluginFile.setPluginId(p2p.getConfig().getPlugin());
                pluginFile.setSize(file.getUsableSpace());
                
                zkService.createPersistentZNode(zkService.getPath().PREFIX_FILE.getFullPath(p2p.getConfig().getId(),pluginFile.getId(),""),pluginFile.toString());
                
            }
        } catch (KeeperException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean checkFiles(PluginFile arc, Collection<PluginFile> old) throws IOException {
        for (PluginFile archive_check : old) {
            if (arc.getPath().equalsIgnoreCase(archive_check.getPath())) {
                return true;
            }
        }

        return false;
    }

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
    
    public String getFilesIP(String file){
        List<String> listFiles ;
        Map<String,List<String>> mapFiles = new HashMap<String, List<String>>();
        try {
            for (Iterator<PluginInfo> it = getPeers().values().iterator(); it.hasNext();) {
                PluginInfo plugin = it.next();
                mapFiles.put(plugin.getHost().getAddress(),zkService.getChildren(plugin.getPath_zk()+zkService.getPath().FILES.toString(), new UpdatePeerData(zkService, this)));
                listFiles = zkService.getChildren(plugin.getPath_zk()+zkService.getPath().FILES.toString(), new UpdatePeerData(zkService, this));
                for(String checkfile : listFiles){
                    if(file.equals(checkfile)){
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
    
    
//    public File getDataFolder() {
//        return dataFolder;
//    }
    

    public void storeFileRec(PluginFile fileC){
        PluginFile file=fileC;
        //file.setPath("data-folder/"+file.getName());
        savedFiles.put(file.getId(),file);
        zkService.createPersistentZNode("/peers/peer_"+file.getPluginId()+"/files/file_"+file.getId(), file.toString());
        try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(new File("data-folder/persistent-storage.json"), savedFiles);
        } catch (Exception e) {
                    e.printStackTrace();
        }
    }
    

    public List<NodeInfo> bestNode(){
        
        List<NodeInfo> plugin;
        cloudMap = discoveryService.getPeers();
        StoragePolicy policy = new StoragePolicy();
        plugin = policy.calcBestCost(zkService,cloudMap.values());

        return plugin;
    }
    
    public void fileUploaded(PluginFile fileuploaded) throws KeeperException, InterruptedException{
        if (zkService.getZNodeExist(PENDING_SAVE+"/file_"+fileuploaded.getId(), false))
        {    
           File fileServer =new File(fileuploaded.getPath());
           if(fileServer.exists()){
               storeFileRec(fileuploaded);
              // zkService.createEphemeralZNode("/peers/peer_"+fileuploaded.getPluginId()+"/files/file_"+fileuploaded.getId(), fileuploaded.toString());            
               zkService.delete(PENDING_SAVE+"/file_"+fileuploaded.getId());
//               zkService.getChildren(PENDING_SAVE+"/file_"+fileuploaded.getId(), new UpdatePeerData(zkService,this));
           }else
               System.out.println("Arquivo não encontrado!");
        }
        else
            System.out.println("Arquivo não encontrado nas pendências !");
        
    }
    
    public void transferFiles(List<NodeInfo> plugins,NodeInfo nodedest,br.unb.cic.bionimbus.avro.gen.FileInfo file, String path, int copies) throws AvroRemoteException{
        
        String dest = null;
        int aux=1;
        
        for (Iterator<NodeInfo> it = plugins.iterator(); it.hasNext();) {
                 NodeInfo node = it.next();
                  
                    Put conexao = new Put(node.getAddress(),path);
                    
                    try {
                        if(conexao.startSession()){
                            dest = node.getPeerId();
                            aux++;
                            if(aux == copies){
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

    @Override
    public void event(WatchedEvent eventType) {
            switch(eventType.getType()){

                case NodeChildrenChanged:

                    break;
                case NodeDataChanged:
                break;

                case NodeDeleted:

                break;
            }
    }
    
    
    
}
