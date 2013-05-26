package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class StoreRespMessage extends AbstractMessage {

    private PluginInfo pluginInfo;

    private FileInfo fileInfo;

    private String taskId = "";

    public StoreRespMessage() {
        super();
    }

    public StoreRespMessage(PeerNode peer, PluginInfo pluginInfo, FileInfo fileInfo, String taskId) {
        super(peer);
        this.pluginInfo = pluginInfo;
        this.fileInfo = fileInfo;
        this.taskId = taskId;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public int getType() {
        return P2PMessageType.STORERESP.code();
    }

    @Override
    public byte[] serialize() throws Exception {
        BulkMessage message = encodeBasicMessage();
        message.setPluginInfo(pluginInfo);
        message.setFileInfo(fileInfo);
        message.setTaskId(taskId);
        return JsonCodec.encodeMessage(message);
    }

    @Override
    public void deserialize(byte[] buffer) throws Exception {
        BulkMessage message = decodeBasicMessage(buffer);
        this.pluginInfo = message.getPluginInfo();
        this.fileInfo = message.getFileInfo();
        this.taskId = message.getTaskId();
    }

}
