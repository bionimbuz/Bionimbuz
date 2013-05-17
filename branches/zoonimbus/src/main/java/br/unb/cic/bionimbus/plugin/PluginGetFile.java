package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.p2p.Host;

public class PluginGetFile implements PluginOps {
	
	private PluginFile pluginFile;
	
	private String taskId;
	
	private Host peer;

	public PluginFile getPluginFile() {
		return pluginFile;
	}

	public void setPluginFile(PluginFile pluginFile) {
		this.pluginFile = pluginFile;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Host getPeer() {
		return peer;
	}

	public void setPeer(Host peer) {
		this.peer = peer;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;

        if (!(object instanceof PluginGetFile)) {

            PluginGetFile other = (PluginGetFile) object;

            return (this.pluginFile.equals(other.pluginFile) && this.taskId.equals(other.taskId));
        } else {
            return false;
        }
	}
	
	@Override
	public int hashCode() {
		return (pluginFile.toString() + taskId).hashCode();
	}
	
	@Override
	public String toString() {
		return pluginFile.toString() + taskId;
	}
}
