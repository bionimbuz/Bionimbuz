package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PErrorType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class TaskErrorMessage extends ErrorMessage {
	
	private String pluginId;	
	private String taskId;
	
	public TaskErrorMessage() {
		super();
	}
	
	public TaskErrorMessage(PeerNode peer, String pluginId, String taskId, String error) {
		super(peer, error);
		this.pluginId = pluginId;
		this.taskId = taskId;
	}

	public String getPluginId() {
		return pluginId;
	}
	
	public String getTaskId() {
		return taskId;
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {
		BulkMessage message = decodeBasicMessage(buffer);
		pluginId = message.getPluginId();
		taskId = message.getTaskId();
	}

	@Override
	public byte[] serialize() throws Exception {
		BulkMessage message = encodeBasicMessage();
		message.setPluginId(pluginId);
		message.setTaskId(taskId);		
		return JsonCodec.encodeMessage(message);
	}

	@Override
	public P2PErrorType getErrorType() {
		return P2PErrorType.TASK;
	}
}
