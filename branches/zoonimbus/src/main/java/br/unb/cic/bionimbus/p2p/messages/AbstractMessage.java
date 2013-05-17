package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.messaging.Message;
import br.unb.cic.bionimbus.p2p.IDFactory;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public abstract class AbstractMessage implements Message {
	
	protected PeerNode peer;

	public AbstractMessage(PeerNode peer) {
		this.peer = peer;
	}
	
	public AbstractMessage() {}

	public void setHost(PeerNode peer) {
		this.peer = peer;
	}
	
	public PeerNode getPeer() {
		return peer;
	}

	@Override
	public byte[] serialize() throws Exception {
		BulkMessage message = encodeBasicMessage();		
		return JsonCodec.encodeMessage(message);		
	}
	
	public BulkMessage encodeBasicMessage() {
		BulkMessage message = new BulkMessage();
		message.setPeerID(peer.getId().toString());
		message.setHost(peer.getHost());
		return message;
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {		
		decodeBasicMessage(buffer);		
	}

	public BulkMessage decodeBasicMessage(byte[] buffer) throws Exception {
		
		BulkMessage message = JsonCodec.decodeMessage(buffer);
		
		String id = message.getPeerID();
		peer = new PeerNode(IDFactory.fromString(id));
		peer.setHost(message.getHost());		
		
		return message;
	}

	public PeerNode getPeerNode() {
		return peer;
	}
}
