package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class PingReqMessage extends AbstractMessage {
	
	private long timestamp = 0;
	
	public PingReqMessage() {
		super();
	}
	
	public PingReqMessage(PeerNode peer, long timestamp) {
		super(peer);
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int getType() {
		return P2PMessageType.PINGREQ.code();
	}

	@Override
	public byte[] serialize() throws Exception {
		BulkMessage message = encodeBasicMessage();
		message.setTimestamp(timestamp);
		return JsonCodec.encodeMessage(message);
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {
		BulkMessage message = decodeBasicMessage(buffer);
		timestamp = message.getTimestamp();
	}

}
