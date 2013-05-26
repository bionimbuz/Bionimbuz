package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class StartReqMessage extends AbstractMessage {

    private JobInfo jobInfo;

    public StartReqMessage() {
        super();
    }

    public StartReqMessage(PeerNode peer, JobInfo jobInfo) {
        super(peer);
        this.jobInfo = jobInfo;
    }

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    @Override
    public void deserialize(byte[] buffer) throws Exception {

        BulkMessage message = decodeBasicMessage(buffer);
        jobInfo = message.getJobInfo();
    }

    @Override
    public byte[] serialize() throws Exception {

        BulkMessage message = encodeBasicMessage();
        message.setJobInfo(jobInfo);

        return JsonCodec.encodeMessage(message);
    }

    @Override
    public int getType() {
        return P2PMessageType.STARTREQ.code();
    }

}
