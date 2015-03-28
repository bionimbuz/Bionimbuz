package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import java.io.IOException;

public class PluginFactory {

    private static Plugin REF;

    private PluginFactory() {
    }

    public static synchronized Plugin getPlugin(final String pluginType, final BioNimbusConfig config) throws IOException {
        if (REF == null) {
//            if (pluginType.equals("hadoop"))
//                REF = new HadoopPlugin();
//            else 
            if (pluginType.equals("linux"))
                REF = new LinuxPlugin(config);
//            else if (pluginType.equals("sge")) {
//                REF = new SGEPlugin(config);
//            }
        }
        return REF;
    }

}
