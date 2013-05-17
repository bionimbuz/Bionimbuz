package br.unb.cic.bionimbus.plugin.linux;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginGetFile;

public class LinuxGetFile implements Callable<PluginGetFile> {

	private final PluginGetFile getFile;

	private final String serverPath;

	public LinuxGetFile(PluginFile pluginFile, String taskId, Host receiver, String serverPath) {
		getFile = new PluginGetFile();
		getFile.setPeer(receiver);
		getFile.setPluginFile(pluginFile);
		getFile.setTaskId(taskId);
		this.serverPath = serverPath;
	}

	public PluginGetFile call() throws Exception {
		String absolutePath = new File(LinuxGetInfo.PATH).getAbsolutePath();
		FileUtils.copyFile(new File(absolutePath + File.separator + getFile.getPluginFile().getPath()),
				new File(serverPath + File.separator + getFile.getPluginFile().getPath()));
		return getFile;
	}

}
