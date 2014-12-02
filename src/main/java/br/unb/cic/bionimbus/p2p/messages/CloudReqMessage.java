package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;

public class CloudReqMessage extends AbstractMessage {

    public CloudReqMessage() {
        super();
    }

    public CloudReqMessage(PeerNode peer) {
        super(peer);
    }

    @Override
    public int getType() {
        return P2PMessageType.CLOUDREQ.code();
    }
}
