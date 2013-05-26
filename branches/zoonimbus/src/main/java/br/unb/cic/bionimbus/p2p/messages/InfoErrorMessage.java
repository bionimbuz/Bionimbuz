package br.unb.cic.bionimbus.p2p.messages;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbus.p2p.P2PErrorType;
import br.unb.cic.bionimbus.p2p.PeerNode;

public class InfoErrorMessage extends ErrorMessage {

    private String pluginId;

    public InfoErrorMessage() {
        super();
    }

    public InfoErrorMessage(PeerNode peer, String pluginId, String error) {
        super(peer, error);
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }

    @Override
    public void deserialize(byte[] buffer) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(buffer, Map.class);
        setError((String) data.get("error"));
        this.pluginId = (String) data.get("pluginId");
    }

    @Override
    public byte[] serialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("type", getErrorType());
        data.put("error", getError());
        data.put("pluginId", pluginId);

        return mapper.writeValueAsBytes(data);
    }

    @Override
    public P2PErrorType getErrorType() {
        return P2PErrorType.INFO;
    }

}
