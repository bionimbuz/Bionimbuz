package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class JobCancelRespMessage extends AbstractMessage {

	private String jobId;

	public JobCancelRespMessage() {
		super();
	}

	public JobCancelRespMessage(PeerNode peer, String jobId) {
		super(peer);
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {

		BulkMessage message = decodeBasicMessage(buffer);
		jobId = message.getJobId();
	}

	@Override
	public byte[] serialize() throws Exception {

		BulkMessage message = encodeBasicMessage();
		message.setJobId(jobId);

		return JsonCodec.encodeMessage(message);
	}

	@Override
	public int getType() {
		return P2PMessageType.JOBCANCELRESP.code();
	}

}
