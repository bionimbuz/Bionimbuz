package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class StartRespMessage extends AbstractMessage {
	
	private String jobId;
	
	private PluginTask task;
	
	public StartRespMessage() {
		super();
	}
	
	public StartRespMessage(PeerNode peer, String jobId, PluginTask task) {
		super(peer);
		this.jobId = jobId;
		this.task = task;
	}
	
	public String getJobId() {
		return jobId;
	}
	
	public PluginTask getPluginTask() {
		return task;
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {
		
		BulkMessage message = decodeBasicMessage(buffer);
		
		this.jobId = message.getJobId();		
		this.task = message.getTask();

//		this.task = new PluginTask();
//		Map<String, Object> taskMap = (Map<String, Object>) data.get("task");
//		this.task.setId((String) taskMap.get("id"));
//		this.task.setState(PluginTaskState.valueOf((String) taskMap.get("state")));
	}
	
	@Override
	public byte[] serialize() throws Exception {
		
		BulkMessage message = encodeBasicMessage();
		message.setJobId(jobId);
		message.setTask(getPluginTask());
		
		return JsonCodec.encodeMessage(message);
		
//		Map<String, Object> data = new HashMap<String, Object>();
//		data.put("jobId", getJobId());
//		data.put("task", getPluginTask());
//
//		ObjectMapper mapper = new ObjectMapper();
//		return mapper.writeValueAsBytes(data);
	}


	@Override
	public int getType() {
		return P2PMessageType.STARTRESP.code();
	}

}
