package br.unb.cic.bionimbus.discovery;

import br.unb.cic.bionimbus.Service;
import br.unb.cic.bionimbus.messaging.Message;
import br.unb.cic.bionimbus.p2p.*;
import br.unb.cic.bionimbus.p2p.messages.*;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Singleton;

import java.util.concurrent.*;

@Singleton
public class DiscoveryService implements Service, P2PListener, Runnable, RemovalListener<Object, Object> {
	
	private static final int PERIOD_SECS = 5;
//	private final ConcurrentMap<String, PluginInfo> infoMap;
    private final Cache<String, PluginInfo> infoCache;
	private final ScheduledExecutorService schedExecService;

	private P2PService p2p;

    public DiscoveryService() {

        infoCache = CacheBuilder.newBuilder()
                                .initialCapacity(1000)
                                .weakKeys()
                                .expireAfterWrite(3*PERIOD_SECS, TimeUnit.SECONDS)
                                .removalListener(this)
                                .build();

        schedExecService = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                                                                    .setDaemon(true)
                                                                    .setNameFormat("DiscoveryService-%d")
                                                                    .build());
    }

    @Override
	public void run() {
		System.out.println("running DiscoveryService...");
        broadcastDiscoveryMessage();
//        removeStaleEntriesFromInfoMap();
	}

    private void broadcastDiscoveryMessage() {
        Preconditions.checkNotNull(p2p);
        Message msg = new InfoReqMessage(p2p.getPeerNode());
        p2p.broadcast(msg);
    }

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

    @Override
	public void start(final P2PService p2p) {
        Preconditions.checkNotNull(p2p);
        this.p2p = p2p;
        p2p.addListener(this);
        schedExecService.scheduleAtFixedRate(this, 0, PERIOD_SECS, TimeUnit.SECONDS);
    }

	@Override
	public void shutdown() {
		p2p.remove(this);
		schedExecService.shutdownNow();
	}

    /**
     * TODO: qual a razão de existir este método?
     */
	@Override
	public void getStatus() {}

	@Override
	public void onEvent(final P2PEvent event) {
		if (!event.getType().equals(P2PEventType.MESSAGE))
			return;

		P2PMessageEvent msgEvent = (P2PMessageEvent) event;
		Message msg = msgEvent.getMessage();
		
		if (msg == null)
			return;
		
		PeerNode sender = p2p.getPeerNode();
		PeerNode receiver = null;
		if (msg instanceof AbstractMessage) {
			receiver = ((AbstractMessage) msg).getPeer();
		}

		switch (P2PMessageType.of(msg.getType())) {
		case INFORESP:
			InfoRespMessage infoMsg = (InfoRespMessage) msg;
            insertResponseIntoInfoMap(receiver, infoMsg);
			break;
		case CLOUDREQ:
            sendRequestMessage(sender, receiver);
			break;
		case ERROR:
			ErrorMessage errMsg = (ErrorMessage) msg;
			System.out.println("ERROR: type="
					+ errMsg.getErrorType().toString() + ";msg="
					+ errMsg.getError());
			break;
		}
	}

    private void sendRequestMessage(final PeerNode sender, final PeerNode receiver) {
        CloudRespMessage cloudMsg = new CloudRespMessage(sender, infoCache.asMap().values());
        if (receiver != null)
            p2p.sendMessage(receiver.getHost(), cloudMsg);
    }

    private void insertResponseIntoInfoMap(PeerNode receiver, InfoRespMessage infoMsg) {
        PluginInfo info = infoMsg.getPluginInfo();
        info.setUptime(receiver.uptime());
        info.setLatency(receiver.getLatency());
        info.setTimestamp(System.currentTimeMillis());
        infoCache.put(info.getId(), info);
    }

    @Override
    public void onRemoval(RemovalNotification<Object, Object> removalNotification) {
        System.out.println("Removendo entrada do infoCache");
    }
}
