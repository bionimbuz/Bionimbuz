package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class StatusRespMessage extends AbstractMessage {
	
	private PluginTask task;
	
	public StatusRespMessage() {
		super();
	}
	
	public StatusRespMessage(PeerNode peer, PluginTask task) {
		super(peer);
		this.task = task;
	}
	
	public PluginTask getPluginTask() {
		return task;
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {
		
		BulkMessage message = decodeBasicMessage(buffer);		
		task = message.getTask();
		
	}

	@Override
	public byte[] serialize() throws Exception {
		
		BulkMessage message = encodeBasicMessage();
		message.setTask(task);
		
		return JsonCodec.encodeMessage(message);
	}

	@Override
	public int getType() {
		return P2PMessageType.STATUSRESP.code();
	}

}
