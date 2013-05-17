package br.unb.cic.bionimbus.p2p;

import br.unb.cic.bionimbus.messaging.Message;

public class P2PMessageEvent implements P2PEvent {
	
	private Message message;
	
	public P2PMessageEvent(Message message) {
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
	
	public void setMessage(Message message) {
		this.message = message;
	}

	@Override
	public P2PEventType getType() {
		return P2PEventType.MESSAGE;
	}

}
