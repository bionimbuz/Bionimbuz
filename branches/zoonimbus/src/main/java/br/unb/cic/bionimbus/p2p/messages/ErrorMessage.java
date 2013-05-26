package br.unb.cic.bionimbus.p2p.messages;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import br.unb.cic.bionimbus.p2p.P2PErrorType;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.PeerNode;

public abstract class ErrorMessage extends AbstractMessage {

    private String error;

    public ErrorMessage() {
        super();
    }

    public ErrorMessage(PeerNode peer, String error) {
        super(peer);
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    static public P2PErrorType deserializeErrorType(byte[] buffer) throws Exception {
        P2PErrorType type = null;
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createJsonParser(buffer);

        p.nextToken();
        while (p.nextToken() != JsonToken.END_OBJECT) {
            p.nextToken();
            if (p.getCurrentName().equals("type")) {
                type = P2PErrorType.valueOf(p.getText());
                break;
            }
        }
        p.close();

        return type;
    }

    public abstract P2PErrorType getErrorType();

    @Override
    public abstract void deserialize(byte[] buffer) throws Exception;

    @Override
    public abstract byte[] serialize() throws Exception;

    @Override
    public int getType() {
        return P2PMessageType.ERROR.code();
    }

}
