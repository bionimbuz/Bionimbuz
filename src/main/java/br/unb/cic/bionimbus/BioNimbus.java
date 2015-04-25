package br.unb.cic.bionimbus;

import java.io.IOException;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.Plugin;
import br.unb.cic.bionimbus.services.ServiceManager;
import br.unb.cic.bionimbus.services.ServiceModule;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static br.unb.cic.bionimbus.config.BioNimbusConfigLoader.*;
import static br.unb.cic.bionimbus.plugin.PluginFactory.getPlugin;
import br.unb.cic.bionimbus.toSort.Listeners;

import static com.google.inject.Guice.createInjector;
import java.util.List;
import java.util.UUID;

public class BioNimbus {

    private static final Logger LOGGER = LoggerFactory.getLogger(BioNimbus.class);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("shutdown hook");
            }

        });
    }

    public BioNimbus(BioNimbusConfig config) throws IOException, InterruptedException {

        config.setId(UUID.randomUUID().toString());
        //final P2PService p2p = new P2PService(config);
        List<Listeners> listeners = null;
//        p2p.start();

        if (!config.isClient()) {
            final Plugin plugin = getPlugin(config.getInfra(), config);
            plugin.start();
//            plugin.setP2P(p2p);
        }

        final Injector injector = createInjector(new ServiceModule());

//        if (p2p.isMaster()) {
            ServiceManager manager = injector.getInstance(ServiceManager.class);
            manager.startAll(config, listeners);
//        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        final String configFile = System.getProperty("config.file", "conf/node.yaml");
        BioNimbusConfig config = loadHostConfig(configFile);

        LOGGER.debug("config = " + config);

        new BioNimbus(config);
    }

}
