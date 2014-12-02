package br.unb.cic.bionimbus.p2p.messages;

import br.unb.cic.bionimbus.services.messaging.Message;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.utils.JsonCodec;

public class InfoRespMessage extends AbstractMessage implements Message {

    private PluginInfo pluginInfo;

    public InfoRespMessage() {
        super();
    }

    public InfoRespMessage(PeerNode peer, PluginInfo pluginInfo) {
        super(peer);
        this.pluginInfo = pluginInfo;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    @Override
    public void deserialize(byte[] buffer) throws Exception {

        BulkMessage message = decodeBasicMessage(buffer);

        pluginInfo = message.getPluginInfo();
    }

    @Override
    public byte[] serialize() throws Exception {

        BulkMessage message = encodeBasicMessage();

        message.setPluginInfo(pluginInfo);

        return JsonCodec.encodeMessage(message);

    }

    @Override
    public int getType() {
        return P2PMessageType.INFORESP.code();
    }

}
