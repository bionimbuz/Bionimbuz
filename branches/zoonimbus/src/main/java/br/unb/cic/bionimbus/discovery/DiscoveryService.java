package br.unb.cic.bionimbus.discovery;

import br.unb.cic.bionimbus.AbstractBioService;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.messaging.Message;
import br.unb.cic.bionimbus.network.utils.NetUtils;
import br.unb.cic.bionimbus.p2p.*;
import br.unb.cic.bionimbus.p2p.messages.*;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.file.FileService;
import br.unb.cic.bionimbus.zookeeper.ZooKeeperService;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
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
                
                for (String child : children) {
                   // if (!peerName.contains(child)){
                   try {
                        System.out.println("peer: " + peerName);
                        String childStr = zkService.getData(ROOT_PEER + SEPARATOR + child, null);
                        ObjectMapper mapper = new ObjectMapper();
                        PluginInfo myInfo = mapper.readValue(childStr, PluginInfo.class); 
                        System.out.println("id:" + myInfo.getId());
                        System.out.println("cores:" + myInfo.getNumCores());
                        System.out.println("disk:" + myInfo.getFsFreeSize());
                        
                        map.put(myInfo.getId(), myInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                                
                System.out.println(map.values().toString());
                                
                
    ////        removeStaleEntriesFromInfoMap();
    ////        removeStaleEntriesFromInfoMap();
            } catch (KeeperException ex) {
                Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
            }
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

                this.connectZK(p2p.getConfig().getZkHosts());
                
                zkService.createPersistentZNode("/peers", null);
                
//                config = p2p.getConfig();
//                String data = getData();
                
                File infoFile = new File("plugininfo.json");
                String infoStr = "";
                if (infoFile.exists()) {
                    List<String> lines = Files.readLines(infoFile, Charsets.UTF_8);
                    infoStr = Joiner.on("").join(lines);
//                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + infoStr);
                    
                }
               
                peerName = zkService.createEphemeralSequentialZNode(ROOT_PEER + SEPARATOR + PREFIX_PEER, infoStr);
                System.out.println("Criado e registrado peer com id " + peerName);
                                
                this.p2p = p2p;
                p2p.addListener(this);
                schedExecService.scheduleAtFixedRate(this, 0, PERIOD_SECS, TimeUnit.SECONDS);
                
            } catch (IOException ex) {
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
	public void getStatus() {}

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
