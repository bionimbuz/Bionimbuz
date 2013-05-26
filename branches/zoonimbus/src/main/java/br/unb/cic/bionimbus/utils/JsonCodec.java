package br.unb.cic.bionimbus.utils;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Charsets;

import br.unb.cic.bionimbus.p2p.messages.BulkMessage;

public final class JsonCodec {

    private JsonCodec() {
    }

    public static byte[] encodeMessage(BulkMessage message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String raw = mapper.writeValueAsString(message);
        return raw.getBytes(Charsets.UTF_8);
    }

    public static BulkMessage decodeMessage(byte[] buf) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BulkMessage message = mapper.readValue(buf, BulkMessage.class);
        return message;
    }
}
