package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.toSort.Listeners;
import java.util.List;
import org.apache.zookeeper.WatchedEvent;

public interface Service {

    public void start(BioNimbusConfig config, List<Listeners> listeners);

    public void shutdown();

    public void getStatus();

    /**
     * Método para tratar os watchers disparados pelo zookeeper
     */
    public void verifyPlugins();

    public void event(WatchedEvent eventType);

}
