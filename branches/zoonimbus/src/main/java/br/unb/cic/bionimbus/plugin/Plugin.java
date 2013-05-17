package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.p2p.P2PService;

public interface Plugin {

	public void start();
	
	public void shutdown();

	public void setP2P(P2PService p2p);
}
