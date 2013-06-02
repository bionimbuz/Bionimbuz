package br.unb.cic.bionimbus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.Plugin;
import br.unb.cic.bionimbus.services.ServiceManager;
import br.unb.cic.bionimbus.services.ServiceModule;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Objects;
import com.google.inject.Injector;

import static br.unb.cic.bionimbus.config.BioNimbusConfigLoader.*;
import static br.unb.cic.bionimbus.plugin.PluginFactory.getPlugin;

import static com.google.inject.Guice.createInjector;

public class BioNimbus {

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("shutdown hook");
            }

        });
    }

    public BioNimbus(BioNimbusConfig config) throws IOException, InterruptedException {

        final P2PService p2p = new P2PService(config);
//        p2p.start();

        if (!config.isClient()) {
            final Plugin plugin = getPlugin(config.getInfra(), p2p);
            plugin.start();
            plugin.setP2P(p2p);
        }

        final Injector injector = createInjector(new ServiceModule());

        if (p2p.isMaster()) {
            ServiceManager manager = injector.getInstance(ServiceManager.class);
            manager.startAll(p2p);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        final String configFile = System.getProperty("config.file", "conf/node.yaml");
        BioNimbusConfig config = loadHostConfig(configFile);

        System.out.println("config = " + config);

        new BioNimbus(config);
    }

}
