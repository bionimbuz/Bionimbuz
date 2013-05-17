package br.unb.cic.bionimbus;

import br.unb.cic.bionimbus.p2p.P2PService;

public interface Service {
	
	public void start(P2PService p2p);
	
	public void shutdown();
	
	public void getStatus();

}
