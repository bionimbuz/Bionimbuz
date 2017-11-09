package br.unb.cic.bionimbuz.services.sched.model;

import br.unb.cic.bionimbuz.plugin.PluginInfo;
import java.util.List;
import java.util.ArrayList;


public class ScheduledMachines {
	public List<PluginInfo> cpu;
	public List<PluginInfo> gpu;
	public ScheduledMachines() {
		cpu= new ArrayList<PluginInfo>();
		gpu= new ArrayList<PluginInfo>();
	}
}
