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
import br.unb.cic.bionimbus.controller.ControllerManager;
import br.unb.cic.bionimbus.controller.ControllerModule;
import static br.unb.cic.bionimbus.plugin.PluginFactory.getPlugin;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbus.toSort.Listeners;

import static com.google.inject.Guice.createInjector;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BioNimbus {

    private static final Logger LOGGER = LoggerFactory.getLogger(BioNimbus.class);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Shutdown hook");
            }

        });
    }

    public BioNimbus(BioNimbusConfig config) throws IOException, InterruptedException {

        config.setId(UUID.randomUUID().toString());
        List<Listeners> listeners = new CopyOnWriteArrayList<>();

        if (!config.isClient()) {
            LinuxGetInfo getinfo = new LinuxGetInfo();
            PluginInfo infopc = getinfo.call();

            infopc.setId(config.getId());

            infopc.setHost(config.getHost());
            infopc.setPrivateCloud(config.getPrivateCloud());

            final Plugin plugin = getPlugin(config.getInfra(), config);
            if (plugin instanceof LinuxPlugin) {
                ((LinuxPlugin) plugin).setMyInfo(infopc);
            }
            plugin.start();

        }

        // Declares Guice Injectors
        final Injector serviceInjector = createInjector(new ServiceModule());
        final Injector controllerInjector = createInjector(new ControllerModule());

        // Intantiates ServiceManager and Controller
        ServiceManager serviceManager = serviceInjector.getInstance(ServiceManager.class);
        ControllerManager controllerManager = controllerInjector.getInstance(ControllerManager.class);

        serviceManager.startAll(config, listeners);

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        final String configFile = System.getProperty("config.file", "conf/node.yaml");
        BioNimbusConfig config = loadHostConfig(configFile);

        // !!! MEDIDA PALEATIVA !!! Para nao ter que trocar o node.yaml toda vez
        config.setZkConnString(InetAddress.getLocalHost().getHostAddress() + ":2181");
        config.setAddress(InetAddress.getLocalHost().getHostAddress());
        // !!! Fim MEDIDA PALEATIVA !!!
        
        LOGGER.debug("config = " + config);

        new BioNimbus(config);
    }

}
