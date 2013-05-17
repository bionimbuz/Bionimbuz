package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.hadoop.HadoopPlugin;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbus.plugin.sge.SGEPlugin;

public class PluginFactory {

	private static Plugin REF;

    private PluginFactory() {}

	public static synchronized Plugin getPlugin(final String pluginType, final P2PService p2p) {
		if (REF == null) {
			if (pluginType.equals("hadoop"))
				REF = new HadoopPlugin();
			else if (pluginType.equals("linux"))
				REF = new LinuxPlugin(p2p);
			else if (pluginType.equals("sge")) {
				REF = new SGEPlugin(p2p);
			}
		}
		return REF;
	}

}
