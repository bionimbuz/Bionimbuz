package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class StoreReqMessage extends AbstractMessage {
	
	private FileInfo fileInfo;
	
	private String taskId = "";
	
	public StoreReqMessage() {
		super();
	}
	
	public StoreReqMessage(PeerNode peer, FileInfo fileInfo, String taskId) {
		super(peer);
		this.fileInfo = fileInfo;
		this.taskId = taskId;
	}
	
	public FileInfo getFileInfo() {
		return fileInfo;
	}
	
	public String getTaskId() {
		return taskId;
	}

	@Override
	public int getType() {
		return P2PMessageType.STOREREQ.code();
	}

	@Override
	public byte[] serialize() throws Exception {
		BulkMessage message = encodeBasicMessage();
		message.setFileInfo(fileInfo);
		message.setTaskId(taskId);
		return JsonCodec.encodeMessage(message);
	}

	@Override
	public void deserialize(byte[] buffer) throws Exception {
		BulkMessage message = decodeBasicMessage(buffer);
		this.fileInfo = message.getFileInfo();
		this.taskId = message.getTaskId();
	}

}
