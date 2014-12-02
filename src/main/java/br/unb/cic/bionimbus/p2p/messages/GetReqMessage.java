package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class GetReqMessage extends AbstractMessage {

    private String fileId;

    private String taskId;

    public GetReqMessage() {
        super();
    }

    public GetReqMessage(PeerNode peer, String fileId, String taskId) {
        super(peer);
        this.fileId = fileId;
        this.taskId = taskId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public byte[] serialize() throws Exception {
        BulkMessage message = encodeBasicMessage();
        message.setFileId(fileId);
        if (taskId.length() > 0)
            message.setTaskId(taskId);
        return JsonCodec.encodeMessage(message);
    }

    @Override
    public void deserialize(byte[] buffer) throws Exception {
        BulkMessage message = decodeBasicMessage(buffer);
        fileId = message.getFileId();
        taskId = message.getTaskId();
    }

    @Override
    public int getType() {
        return P2PMessageType.GETREQ.code();
    }

}
