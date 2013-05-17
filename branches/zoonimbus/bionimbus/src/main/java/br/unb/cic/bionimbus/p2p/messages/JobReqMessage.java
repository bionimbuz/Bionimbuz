package br.unb.cic.bionimbus.p2p.messages;

import java.util.Collection;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class JobReqMessage extends AbstractMessage {
	
	private Collection<JobInfo> values;
	
	public JobReqMessage() {
		super();
	}
		
	public JobReqMessage(PeerNode peer, Collection<JobInfo> values) {
		super(peer);
		this.values = values;
	}
	
	public Collection<JobInfo> values() {
		return values;
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {

		BulkMessage message = decodeBasicMessage(buffer);
		values = message.getJobList();		
	}
	
	@Override
	public byte[] serialize() throws Exception {
		
		BulkMessage message = encodeBasicMessage();
		
		message.setJobList(values);
		
		return JsonCodec.encodeMessage(message);
		
	}

	@Override
	public int getType() {
		return P2PMessageType.JOBREQ.code();
	}

}
