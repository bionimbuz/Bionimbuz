package br.unb.cic.bionimbus.p2p.udp;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.util.CharsetUtil;


public class JsonEncoder extends OneToOneEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

        if (!(msg instanceof WireMessage)) {
            return msg;
        }

        System.out.println("msg to encode:" + msg);

        String json = serialize((WireMessage) msg);

        byte[] data = json.getBytes(CharsetUtil.UTF_8);
        int dataLength = data.length;

        System.out.println("message: " + json);
        System.out.println("message length: " + dataLength);

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
//		buf.writeByte((byte) 'J'); //magic number
        buf.writeInt(dataLength);
        buf.writeBytes(data);
        return buf;
    }

    private String serialize(WireMessage msg) throws CorruptedFrameException {
        ObjectMapper mapper = new ObjectMapper();

        Throwable t;
        try {
            return mapper.writeValueAsString(msg);
        } catch (JsonGenerationException e) {
            t = e;
        } catch (JsonMappingException e) {
            t = e;
        } catch (IOException e) {
            t = e;
        }

        throw new CorruptedFrameException("Error while serializing message: " + t.getMessage());
    }
}
