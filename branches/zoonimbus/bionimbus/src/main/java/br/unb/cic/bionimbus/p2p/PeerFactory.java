package br.unb.cic.bionimbus.p2p;

public final class PeerFactory {
	
	public static synchronized PeerNode createPeer(boolean random) {
		ID peerID = IDFactory.newRandomID();
		PeerNode node = new PeerNode(peerID);
		return node;
	}

}
