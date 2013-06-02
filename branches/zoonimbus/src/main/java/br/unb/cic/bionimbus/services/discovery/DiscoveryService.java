package br.unb.cic.bionimbus.services.discovery;

import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.p2p.*;
import br.unb.cic.bionimbus.p2p.messages.*;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbus.services.storage.file.FileService;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import com.google.common.base.Preconditions;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.zookeeper.KeeperException;
import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class DiscoveryService extends AbstractBioService implements RemovalListener<Object, Object> {

    private static final int PERIOD_SECS = 10;
    //	private final ConcurrentMap<String, PluginInfo> infoMap;
//    private final Cache<String, PluginInfo> infoCache;
    private final ScheduledExecutorService schedExecService;
    private P2PService p2p;
    private BioNimbusConfig config;
    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private static final String PREFIX_PEER = "peer_";
    private static final String STATUS = "STATUS";
    private String peerName;
    private List<String> children;
    private ConcurrentMap<String, PluginInfo> map = Maps.newConcurrentMap();

    @Inject
    public DiscoveryService(final ZooKeeperService service) {

        Preconditions.checkNotNull(service);
        this.zkService = service;

//        infoCache = CacheBuilder.newBuilder()
//                                .initialCapacity(1000)
//                                .weakKeys()
//                                .expireAfterWrite(3*PERIOD_SECS, TimeUnit.SECONDS)
//                                .removalListener(this)
//                                .build();

        schedExecService = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("DiscoveryService-%d")
                .build());
    }

    //    @Override
    public void run() {
        System.out.println("running DiscoveryService...");

        System.out.println(peerName);
        try {
            // ATUALIZANDO DADOS LOCAIS
//                File infoFile = new File("plugininfo.json");
//                String infoStr = "";
//                if (infoFile.exists()) {
//                    List<String> lines = Files.readLines(infoFile, Charsets.UTF_8);
//                    infoStr = Joiner.on("").join(lines);
//                    zkService.setData(peerName, infoStr);
//                }

            children = zkService.getChildren(ROOT_PEER, null);

            System.out.println(children);

            map.clear();
            for (String child : children) {
                // if (!peerName.contains(child)){
                try {

//                        System.out.println("peer: " + peerName);
                    String childStr = zkService.getData(ROOT_PEER + SEPARATOR + child, null);
                    System.out.println("childStr:" + childStr);
                    ObjectMapper mapper = new ObjectMapper();
                    PluginInfo myInfo = mapper.readValue(childStr, PluginInfo.class);
                    
                    //modificar setter
                    myInfo.setPath_zk(ROOT_PEER + SEPARATOR +child);
                    
                    
//                        System.out.println("id:" + myInfo.getId());
//                        System.out.println("cores:" + myInfo.getNumCores());
//                        System.out.println("disk:" + myInfo.getFsFreeSize());
                    
                    //verifica se o peer ainda está on-line e através da existência do zNode STATUS, 
                    //se não estiver apaga o zNode persistente
                    if(zkService.getZNodeExist(myInfo.getPath_zk()+SEPARATOR+STATUS, false)){
                         map.put(myInfo.getId(), myInfo);
                    }else{
                        zkService.delete(myInfo.getPath_zk());
                    }
//                    if(zkService.getZNodeExist(peerName+SEPARATOR+STATUS, false)){
//                        map.put(myInfo.getId(), myInfo);
//                    }else{
//                        zkService.delete(myInfo.getPath_zk());
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("tamanho do values" + map.values().size());
            for (PluginInfo a : map.values()) {
                System.out.println("no: " + a.getHost().getAddress());
                System.out.println("espaço: " + a.getFsSize());
                System.out.println("uptime: "+ a.getUptime());
            }

//                System.out.println(map.values().toString());


            ////        removeStaleEntriesFromInfoMap();
            ////        removeStaleEntriesFromInfoMap();
        } catch (KeeperException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ConcurrentMap<String, PluginInfo> getPeers() {

        System.out.println(peerName);
        try {

            children = zkService.getChildren(ROOT_PEER, null);

            map.clear();
            for (String child : children) {
                // if (!peerName.contains(child)){
                try {
                    String childStr = zkService.getData(ROOT_PEER + SEPARATOR + child, null);
                    System.out.println(childStr);
                    ObjectMapper mapper = new ObjectMapper();
                    PluginInfo myInfo = mapper.readValue(childStr, PluginInfo.class);
                    map.put(myInfo.getId(), myInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }
//
//    private void broadcastDiscoveryMessage() {
//        Preconditions.checkNotNull(p2p);
//        Message msg = new InfoReqMessage(p2p.getPeerNode());
//        p2p.broadcast(msg);
//    }

    /**
     * TODO: substituir por Guava Cache com expiração
     */
    /*
     private void removeStaleEntriesFromInfoMap() {
     long now = System.currentTimeMillis();
     for (PluginInfo plugin : infoMap.values()) {
     if (now - plugin.getTimestamp() > 3*PERIOD_SECS*1000) {
     infoMap.remove(plugin.getId());
     }
     }
     }
     */
    public String getData() throws IOException {
        return "id: " + config.getId() + "\n"
                + "net-address: " + config.getHost().getAddress() + "\n"
                + "net-port: " + config.getHost().getPort() + "\n"
                + "cpu-cores: " + Runtime.getRuntime().availableProcessors() + "\n"
                + "disk-space: " + FileService.getFreeSpace("/");
    }

    //    @Override
    public void start(final P2PService p2p) {
        try {
            Preconditions.checkNotNull(p2p);

            zkService.createPersistentZNode("/peers", null);

//                config = p2p.getConfig();
//                String data = getData();
          LinuxGetInfo getinfo=new LinuxGetInfo();
          PluginInfo infopc= getinfo.call();
          infopc.setId(UUID.randomUUID().toString());
          infopc.setHost(p2p.getConfig().getHost());
          infopc.setUptime(p2p.getPeerNode().uptime());
          String infoStr = infopc.toString();
            System.out.println(">>>>"+infoStr);
//            File infoFile = new File("plugininfo.json");
//            String infoStr = "";
//            if (infoFile.exists()) {
//                List<String> lines = Files.readLines(infoFile, Charsets.UTF_8);
//                infoStr = Joiner.on("").join(lines)+"uptime: "+p2p.getPeerNode().uptime();
//                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + infoStr);
//
//            }
            //criando zNode persistente para cada novo peer
            peerName = zkService.createPersistentSequentialZNode(ROOT_PEER + SEPARATOR + PREFIX_PEER, infoStr);
            
            //criando status efemera para verificar se o servidor esta rodando
            zkService.createEphemeralZNode(peerName+SEPARATOR+STATUS, null);
                
            System.out.println("Criado e registrado peer com id " + peerName);

            this.p2p = p2p;
            p2p.addListener(this);
            schedExecService.scheduleAtFixedRate(this, 0, PERIOD_SECS, TimeUnit.SECONDS);

        } catch (IOException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//	@Override
//	public void shutdown() {
//		p2p.remove(this);
//		schedExecService.shutdownNow();
//	}
    /**
     * TODO: qual a razão de existir este método?
     */
//	@Override
    public void getStatus() {
    }

    @Override
    public void onEvent(final P2PEvent event) {
//		if (!event.getType().equals(P2PEventType.MESSAGE))
//			return;
//
//		P2PMessageEvent msgEvent = (P2PMessageEvent) event;
//		Message msg = msgEvent.getMessage();
//		
//		if (msg == null)
//			return;
//		
//		PeerNode sender = p2p.getPeerNode();
//		PeerNode receiver = null;
//		if (msg instanceof AbstractMessage) {
//			receiver = ((AbstractMessage) msg).getPeer();
//		}
//
//		switch (P2PMessageType.of(msg.getType())) {
//		case INFORESP:
//			InfoRespMessage infoMsg = (InfoRespMessage) msg;
//            insertResponseIntoInfoMap(receiver, infoMsg);
//			break;
//		case CLOUDREQ:
//            sendRequestMessage(sender, receiver);
//			break;
//		case ERROR:
//			ErrorMessage errMsg = (ErrorMessage) msg;
//			System.out.println("ERROR: type="
//					+ errMsg.getErrorType().toString() + ";msg="
//					+ errMsg.getError());
//			break;
//		}
    }

//    private void sendRequestMessage(final PeerNode sender, final PeerNode receiver) {
//        CloudRespMessage cloudMsg = new CloudRespMessage(sender, infoCache.asMap().values());
//        if (receiver != null)
//            p2p.sendMessage(receiver.getHost(), cloudMsg);
//    }
    private void insertResponseIntoInfoMap(PeerNode receiver, InfoRespMessage infoMsg) {
        PluginInfo info = infoMsg.getPluginInfo();
        info.setUptime(receiver.uptime());
        info.setLatency(receiver.getLatency());
        info.setTimestamp(System.currentTimeMillis());
//        infoCache.put(info.getId(), info);
    }
//
////    @Override
//    public void onRemoval(RemovalNotification<Object, Object> removalNotification) {
//        System.out.println("Removendo entrada do infoCache");
//    }
//
////    @Override
//    public void onRemoval(RemovalNotification rn) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    @Override
    public void onRemoval(RemovalNotification rn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
