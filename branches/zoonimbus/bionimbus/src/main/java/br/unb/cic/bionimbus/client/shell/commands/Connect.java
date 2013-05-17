package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;
import br.unb.cic.bionimbus.p2p.P2PService;

public class Connect implements Command {
	
	public static final String NAME = "connect";
	
	private final SimpleShell shell;
	
	public Connect(SimpleShell shell) {
		this.shell = shell;
	}

	@Override
	public String execute(String... params) throws Exception {
		String configFile = System.getProperty("config.file", "conf/client.json");
		BioNimbusConfig config = BioNimbusConfigLoader.loadHostConfig(configFile);

		P2PService p2p = new P2PService(config);
		p2p.start();
		shell.setP2P(p2p);
		
		while (p2p.getPeers().isEmpty())
			;
		
		shell.setConnected(true);
		return "client is connected.";
	}

	@Override
	public String usage() {
		return NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void setOriginalParamLine(String param) {
	}

}
