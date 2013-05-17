package br.unb.cic.bionimbus.p2p.messages;

import java.util.Collection;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class ListRespMessage extends AbstractMessage {
	
	private Collection<PluginFile> values;
	
	public ListRespMessage() {
		super();
	}
	
	public ListRespMessage(PeerNode peer, Collection<PluginFile> values) {
		super(peer);
		this.values = values;
	}
	
	public Collection<PluginFile> values() {
		return values;
	}

	@Override
	public byte[] serialize() throws Exception {
		BulkMessage message = encodeBasicMessage();
		message.setFileList(values);				
		return JsonCodec.encodeMessage(message);
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {
		BulkMessage message = decodeBasicMessage(buffer);		
		values = message.getFileList();
	}

	@Override
	public int getType() {
		return P2PMessageType.LISTRESP.code();
	}

}
