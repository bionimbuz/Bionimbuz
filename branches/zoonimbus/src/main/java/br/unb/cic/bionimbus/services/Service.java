package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.p2p.P2PService;
import org.apache.zookeeper.WatchedEvent;

public interface Service {

    public void start(P2PService p2p);

    public void shutdown();

    public void getStatus();
    
    /**
     * Método para tratar os watchers disparados pelo zookeeper
     */
    public void event(WatchedEvent eventType);
    

}
