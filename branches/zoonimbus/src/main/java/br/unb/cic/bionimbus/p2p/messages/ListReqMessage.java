package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;

public class ListReqMessage extends AbstractMessage {
	
	public ListReqMessage() {
		super();
	}
	
	public ListReqMessage(PeerNode peer) {
		super(peer);
	}

	@Override
	public int getType() {
		return P2PMessageType.LISTREQ.code();
	}

}
