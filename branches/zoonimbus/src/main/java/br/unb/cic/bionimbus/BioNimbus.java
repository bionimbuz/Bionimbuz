package br.unb.cic.bionimbus;

import java.io.IOException;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.Plugin;
import com.google.inject.Injector;

import static br.unb.cic.bionimbus.config.BioNimbusConfigLoader.*;
import static br.unb.cic.bionimbus.plugin.PluginFactory.getPlugin;
import br.unb.cic.bionimbus.zookeeper.ZooKeeperService;
import static com.google.inject.Guice.createInjector;

public class BioNimbus {
    
    private ZooKeeperService zkService;

    public BioNimbus(BioNimbusConfig config) throws IOException, InterruptedException {

        final P2PService p2p = new P2PService(config);
		p2p.start();

		if (!config.isClient()) {
            final Plugin plugin = getPlugin(config.getInfra(), p2p);
			plugin.start();
			plugin.setP2P(p2p);
                        zkService = new ZooKeeperService();
                        zkService.connect(config.getZkHosts());
		}

        final Injector injector = createInjector(new ServiceModule());

        if (p2p.isMaster()) {
			ServiceManager manager = injector.getInstance(ServiceManager.class);
			manager.startAll(p2p);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		
		final String configFile = System.getProperty("config.file", "conf/server.json");
		BioNimbusConfig config = loadHostConfig(configFile);

		new BioNimbus(config);
	}
}
