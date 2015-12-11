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
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbus.toSort.Listeners;

import static com.google.inject.Guice.createInjector;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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
        List<Listeners> listeners = new CopyOnWriteArrayList<Listeners>();
        
        if (!config.isClient()) {
            LinuxGetInfo getinfo = new LinuxGetInfo();
            PluginInfo infopc = getinfo.call();

            infopc.setId(config.getId());

            infopc.setHost(config.getHost());
            infopc.setPrivateCloud(config.getPrivateCloud());
                
                
            final Plugin plugin = getPlugin(config.getInfra(), config);
            if (plugin instanceof LinuxPlugin){
                ((LinuxPlugin)plugin).setMyInfo(infopc);
            }
            plugin.start();

        }

        final Injector injector = createInjector(new ServiceModule());

            ServiceManager manager = injector.getInstance(ServiceManager.class);
            manager.startAll(config, listeners);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        final String configFile = System.getProperty("config.file", "conf/node.yaml");
        BioNimbusConfig config = loadHostConfig(configFile);

        LOGGER.debug("config = " + config);

        new BioNimbus(config);
    }

}
