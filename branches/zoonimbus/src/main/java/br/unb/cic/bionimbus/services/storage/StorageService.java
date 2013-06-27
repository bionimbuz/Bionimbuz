package br.unb.cic.bionimbus.services.storage;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.BioProtoImpl;
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
        checkFiles();


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

    //Talves esse metodo seja apagado, pois se esse servidor cair, 
    //o arquivo que foi duplicado em outra máquina sera duplicado para outra máquina, e assim que esse servidor 
    //suba os arquivos serão deletados, a não ser que esse metodo verifique se existe somente 1 arquivo ou o numero 
    //do fator de replicação para poder subir com estes arquivos par nuvem
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
                //list ids Verificar onde esse arquivo está caso já exista
                List<String> listIds = new ArrayList<String>();
                listIds.add(p2p.getConfig().getPlugin());
                
                pluginFile.setPluginId(listIds);
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
           File fileServer =new File(fileuploaded.getPath());
           if(fileServer.exists()){
                zkService.createPersistentZNode( zkService.getPath().PREFIX_FILE.getFullPath(fileuploaded.getPluginId().iterator().next(), 
                                        fileuploaded.getId(), ""), fileuploaded.toString());
               zkService.delete(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", fileuploaded.getId(), ""));
           }else
               System.out.println("Arquivo não encontrado!");
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
    public void transferFiles(List<NodeInfo> plugins, String path, int copies,String idPluginCopy) throws AvroRemoteException, KeeperException, InterruptedException{
        
        int aux=0;
        
        for (Iterator<NodeInfo> it = plugins.iterator(); it.hasNext();) {
                 NodeInfo node = it.next();
                  
                    Put conexao = new Put(node.getAddress(),path);   
                    try {
                        if(conexao.startSession()){
                            aux++;
                            if(aux == copies){
                                //Começar daqui aamanha
                                zkService.getData(zkService.getPath().PREFIX_FILE.getFullPath(idPluginCopy, path, path), null);
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
    
    public void failOverStorage(String id) throws AvroRemoteException, KeeperException, InterruptedException{
        Map<String,PluginFile>filesPeerDown= getFilesPeer(id);
        List<NodeInfo> nodesdisp = new ArrayList<NodeInfo>();
        nodesdisp.clear();
        Collection<PluginInfo> cloudPlugin=getPeers().values();
        for (PluginFile file : filesPeerDown.values())
        {
            File localFile= new File(file.getPath());
            for (Iterator<String> it = file.getPluginId().iterator(); it.hasNext();) {
                String pluginId;
                pluginId = it.next();
                if (!pluginId.equals(id) && localFile.exists())
                    
                    for(PluginInfo plugin: cloudPlugin){
                        try {
                              NodeInfo node = new NodeInfo();
                              if ((long)(plugin.getFsFreeSize()*MAXCAPACITY)>localFile.length()){
                                node.setLatency(Ping.calculo(plugin.getHost().getAddress()));
                                node.setAddress(plugin.getHost().getAddress());
                                node.setFreesize(plugin.getFsFreeSize());
                                node.setPeerId(pluginId);
                                nodesdisp.add(node);
                              }    
                              plugin.setLatency(Ping.calculo(plugin.getHost().getAddress()));
                        } catch (IOException ex) {
                            Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //modificar o último valor para fator de replicação-1(MODIFICAR)
                    transferFiles(bestNode(nodesdisp), localFile.getPath(),1,pluginId);
            }
        }
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
                        System.out.print(path + "= NodeChildrenChanged");
                    break;
                case NodeDeleted:
                    if(eventType.getPath().contains(zkService.getPath().STATUS.toString())){
                        String peerPath =  path.subSequence(path.indexOf(zkService.getPath().PREFIX_PEER.toString()), path.indexOf("/STATUS")).toString();
                   try {
                       failOverStorage(peerPath);
                   } catch (AvroRemoteException ex) {
                       Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                   } catch (KeeperException ex) {
                       Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                   } catch (InterruptedException ex) {
                       Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                   }
                    }
                    break;
            }
    }
    
    
    
}
