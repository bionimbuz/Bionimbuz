package br.unb.cic.bionimbus.p2p.messages;

import java.util.Collection;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class SchedReqMessage extends AbstractMessage {

    private Collection<JobInfo> jobList;

    public SchedReqMessage() {
        super();
    }

    public SchedReqMessage(PeerNode peer, Collection<JobInfo> jobList) {
        super(peer);
        this.jobList = jobList;
    }

    public Collection<JobInfo> values() {
        return jobList;
    }

    @Override
    public byte[] serialize() throws Exception {

        BulkMessage message = encodeBasicMessage();
        message.setJobList(jobList);

        return JsonCodec.encodeMessage(message);
    }

    @Override
    public void deserialize(byte[] buffer) throws Exception {

        BulkMessage message = decodeBasicMessage(buffer);

        jobList = message.getJobList();

    }

    @Override
    public int getType() {
        return P2PMessageType.SCHEDREQ.code();
    }
}
