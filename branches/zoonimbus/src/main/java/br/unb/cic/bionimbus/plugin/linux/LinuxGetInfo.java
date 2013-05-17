package br.unb.cic.bionimbus.plugin.linux;

import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class LinuxGetInfo implements Callable<PluginInfo> {

	private static final String SERVICE_DIR = "services";
	
	public static final String PATH = "files";

	private final PluginInfo pluginInfo = new PluginInfo();

	@Override
	public PluginInfo call() throws Exception {
		getCpuInfo();
		getDiskInfo();
		getServices();

		return pluginInfo;
	}

	private void getCpuInfo() {
		pluginInfo.setNumCores(Runtime.getRuntime().availableProcessors());
		pluginInfo.setNumNodes(1);
		pluginInfo.setNumOccupied(0);
	}

	private void getDiskInfo() {
		File path = new File(PATH);
		for (File root : File.listRoots()) {
			if (path.getAbsolutePath().contains(root.getAbsolutePath())) {
				pluginInfo.setFsFreeSize((float)root.getFreeSpace());
				pluginInfo.setFsSize((float)root.getTotalSpace());
				return;
			}
		}
	}

	private void getServices() throws Exception {
		final List<PluginService> list = new CopyOnWriteArrayList<PluginService>();
		File dir = new File(SERVICE_DIR);

		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile() && file.canRead() && file.getName().endsWith(".json")) {
					ObjectMapper mapper = new ObjectMapper();
					PluginService service = mapper.readValue(file, PluginService.class);
					list.add(service);
				}
			}
		}
			
		pluginInfo.setServices(list);
	}

}
