package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;

public class InfoReqMessage extends AbstractMessage {
	
	public InfoReqMessage() {
		super();
	}

	public InfoReqMessage(PeerNode peer) {
		super(peer);
	}

	@Override
	public int getType() {
		return P2PMessageType.INFOREQ.code();
	}
}
