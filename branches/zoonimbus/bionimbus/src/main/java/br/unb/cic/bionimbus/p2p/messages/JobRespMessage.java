package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class JobRespMessage extends AbstractMessage {
	
	private JobInfo jobInfo;
	
	public JobRespMessage() {
		super();
	}
		
	public JobRespMessage(PeerNode peer, JobInfo jobInfo) {
		super(peer);
		this.jobInfo = jobInfo;
	}

	public JobInfo getJobInfo() {
		return jobInfo;
	}

	@Override
	public byte[] serialize() throws Exception {
		BulkMessage message = encodeBasicMessage();
		message.setJobInfo(jobInfo);
		return JsonCodec.encodeMessage(message);
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {
		BulkMessage message = decodeBasicMessage(buffer);
		jobInfo = message.getJobInfo();
	}

	@Override
	public int getType() {
		return P2PMessageType.JOBRESP.code();
	}
}
