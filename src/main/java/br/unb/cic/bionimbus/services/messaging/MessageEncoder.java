package br.unb.cic.bionimbus.services.messaging;

import org.jboss.netty.channel.SimpleChannelHandler;

public class MessageEncoder extends SimpleChannelHandler {

//    @Override
//    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
//        Message message = (Message) e.getMessage();
//
//        byte[] encoded = message.serialize();
//        int length = 0;
//        if (encoded != null)
//            length = encoded.length;
//
//        ChannelBuffer buffer = ChannelBuffers.buffer(9 + length);
//        buffer.writeByte('X');
//        buffer.writeInt(length);
//        buffer.writeInt(message.getType());
//
//        if (length > 0)
//            buffer.writeBytes(encoded);
//
//        Channels.write(ctx, e.getFuture(), buffer);
//    }

}
