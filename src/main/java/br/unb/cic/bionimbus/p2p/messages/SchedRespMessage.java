package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class SchedRespMessage extends AbstractMessage {

    private String jobId;
    private PluginInfo pluginInfo;

    public SchedRespMessage() {
        super();
    }

    public SchedRespMessage(PeerNode peer) {
        super(peer);
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Override
    public byte[] serialize() throws Exception {

        BulkMessage message = encodeBasicMessage();
        message.setJobId(jobId);
        message.setPluginInfo(pluginInfo);

        return JsonCodec.encodeMessage(message);

    }

    @Override
    public void deserialize(byte[] buffer) throws Exception {

        BulkMessage message = decodeBasicMessage(buffer);

        jobId = message.getJobId();
        pluginInfo = message.getPluginInfo();
    }

    @Override
    public int getType() {
        return P2PMessageType.SCHEDRESP.code();
    }

}
