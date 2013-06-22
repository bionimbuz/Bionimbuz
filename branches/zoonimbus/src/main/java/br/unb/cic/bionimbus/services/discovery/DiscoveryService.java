package br.unb.cic.bionimbus.services.discovery;

import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.p2p.messages.InfoRespMessage;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import com.google.common.base.Preconditions;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

@Singleton
public class DiscoveryService extends AbstractBioService implements RemovalListener<Object, Object> {

    private static final int PERIOD_SECS = 10;
//	private final ConcurrentMap<String, PluginInfo> infoMap;
//    private final Cache<String, PluginInfo> infoCache;
    private final ScheduledExecutorService schedExecService;
    private P2PService p2p;
//    private BioNimbusConfig config;
//    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private static final String STATUS = "STATUS";
    private static final String STATUSWAITING = "STATUSWAITING";
//    private static final String FILES = "files";
//    private static final String PREFIX_FILE = "file_";
//    private String peerName;
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

    @Override
    public void run() {    
        System.out.println("running DiscoveryService...");
//        Map<String, PluginInfo> listPlugin = getPeers();
//        if(!listPlugin.isEmpty()){
//            for (PluginInfo myInfo : listPlugin.values()) {
//                try {
//                    
//                    //Apaga znode peer_X apenas quando estiver pronto para ser excluido
//                   
//                    if(zkService.getZNodeExist(myInfo.getPath_zk()+SEPARATOR+STATUS, false)){ 
//                        map.put(myInfo.getId(), myInfo);
//                    }else if(zkService.getZNodeExist(myInfo.getPath_zk()+SEPARATOR+STATUS+WAITING, false)){
//                                map.remove(myInfo.getId());
//                                
//                            }else{
//                                zkService.delete(myInfo.getPath_zk());
//                            }
//                } catch (KeeperException ex) {
//                    Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }

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
     */
     }
//    public String getData() throws IOException {
//        return "id: " + config.getId() + "\n"
//                + "net-address: " + config.getHost().getAddress() + "\n"
//                + "net-port: " + config.getHost().getPort() + "\n"
//                + "cpu-cores: " + Runtime.getRuntime().availableProcessors() + "\n"
//                + "disk-space: " + FileService.getFreeSpace("/") + "\n"
//                + "uptime: " + System.currentTimeMillis();
//    }

//    @Override
    public void start(final P2PService p2p) {
        try {
            Preconditions.checkNotNull(p2p);
            LinuxGetInfo getinfo=new LinuxGetInfo();
            LinuxPlugin linuxPlugin = new LinuxPlugin(p2p);

            PluginInfo infopc= getinfo.call();
            infopc.setId(p2p.getConfig().getId());
            infopc.setHost(p2p.getConfig().getHost());
            infopc.setUptime(p2p.getPeerNode().uptime());
            infopc.setPrivateCloud(p2p.getConfig().getPrivateCloud());

            //definindo myInfo após a leitura dos dados
            linuxPlugin.setMyInfo(infopc);
            //armazenando dados do plugin no zookeeper
            zkService.setData(infopc.getPath_zk(), infopc.toString());
          
            this.p2p = p2p;
            p2p.addListener(this);
            schedExecService.scheduleAtFixedRate(this, 0, PERIOD_SECS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void getStatus() {
    }

    
    /**
     * Trata os watchers enviados da implementação da classe Watcher que recebe uma notificação do zookeeper
     * @param eventType evento recebido do zookeeper
     */
    @Override
    public void event(WatchedEvent eventType) {
        
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
//        96456967  EDSON
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
        info.setLatency(Double.longBitsToDouble(receiver.getLatency()));
        info.setTimestamp(System.currentTimeMillis());
//        infoCache.put(info.getId(), info);
    }

    @Override
    public void onRemoval(RemovalNotification rn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
