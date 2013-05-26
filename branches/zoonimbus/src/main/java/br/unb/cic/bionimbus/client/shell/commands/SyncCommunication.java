package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.messaging.Message;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PEventType;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PMessageEvent;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;

public class SyncCommunication implements P2PListener {

    private final P2PService p2p;
    private Message respMsg = null;
    private P2PMessageType respType = null;

    public SyncCommunication(P2PService p2p) {
        this.p2p = p2p;
    }

    @Override
    public void onEvent(P2PEvent event) {
        if (event.getType().equals(P2PEventType.MESSAGE)) {
            P2PMessageEvent msgEvent = (P2PMessageEvent) event;
            Message recvdMsg = msgEvent.getMessage();
            if (recvdMsg.getType() == respType.code())
                putResp(recvdMsg);
        }
    }

    private synchronized void putResp(Message respMsg) {
        p2p.remove(this);
        this.respMsg = respMsg;
        notify();
    }

    public synchronized Message getResp() throws InterruptedException {
        while (respMsg == null)
            wait();
        Message msg = respMsg;
        respMsg = null;
        return msg;
    }

    public void sendReq(Message reqMsg, P2PMessageType respType) {
        this.respType = respType;
        p2p.addListener(this);
        p2p.broadcast(reqMsg);
    }

}
