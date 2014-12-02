package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class StatusReqMessage extends AbstractMessage {

    private String taskId;

    public StatusReqMessage() {
        super();
    }

    public StatusReqMessage(PeerNode peer, String taskId) {
        super(peer);
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public void deserialize(byte[] buffer) throws Exception {

        BulkMessage message = decodeBasicMessage(buffer);
        taskId = message.getTaskId();
    }

    @Override
    public byte[] serialize() throws Exception {

        BulkMessage message = encodeBasicMessage();
        message.setTaskId(taskId);

        return JsonCodec.encodeMessage(message);
    }

    @Override
    public int getType() {
        return P2PMessageType.STATUSREQ.code();
    }

}
