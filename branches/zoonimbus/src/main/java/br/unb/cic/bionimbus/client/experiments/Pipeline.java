package br.unb.cic.bionimbus.client.experiments;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginFile;

public interface Pipeline {

	String getCurrentOutput();
	
	JobInfo firstJob();

	JobInfo nextJob(PluginFile pluginFile);

	String getInput();
}
