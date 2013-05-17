package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class CancelRespMessage extends AbstractMessage {
	
	private PluginTask pluginTask;
	
	public CancelRespMessage() {
		super();
	}
	
	public CancelRespMessage(PeerNode peer, PluginTask pluginTask) {
		super(peer);
		this.pluginTask = pluginTask;
	}
	
	public PluginTask getPluginTask() {
		return pluginTask;
	}
	
	@Override
	public void deserialize(byte[] buffer) throws Exception {

		BulkMessage message = decodeBasicMessage(buffer);
		pluginTask = message.getTask();
	}

	@Override
	public byte[] serialize() throws Exception {
		
		BulkMessage message = encodeBasicMessage();
		message.setTask(pluginTask);
		
		return JsonCodec.encodeMessage(message);
	}

	@Override
	public int getType() {
		return P2PMessageType.CANCELRESP.code();
	}

}
