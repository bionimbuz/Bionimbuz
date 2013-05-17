package br.biofoco.p2p.dht.core;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import br.unb.cic.bionimbus.p2p.dht.Endpoint;
import br.unb.cic.bionimbus.p2p.dht.IDFactory;
import br.unb.cic.bionimbus.p2p.dht.PeerConfig;
import br.unb.cic.bionimbus.p2p.dht.PeerNode;
import br.unb.cic.bionimbus.p2p.dht.PeerServer;

public class PeerPlatformTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		PeerConfig config = new PeerConfig();
		config.setPeerID(IDFactory.newRandomID());
		config.setPort(9191);
		
		config.addSeed(new Endpoint("localhost", 9191));
		config.addSeed(new Endpoint("localhost", 9292));
		config.addSeed(new Endpoint("localhost", 9393));
		
		PeerServer p1 = PeerServer.newPeerServer(config);
		p1.startServices();

		config.setPeerID(IDFactory.newRandomID());
		config.setPort(9292);
		
		PeerServer p2 = PeerServer.newPeerServer(config);
		p2.startServices();
		
		config.setPeerID(IDFactory.newRandomID());
		config.setPort(9393);
		
		PeerServer p3 = PeerServer.newPeerServer(config);
		p3.startServices();
		
		TimeUnit.MINUTES.sleep(1);
				
		Set<PeerNode> peers = p2.getPeerView();
		System.out.println(peers);
		
//		p2.sendMessage("SERVICE");
	}
}
