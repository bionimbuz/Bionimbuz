package br.unb.cic.bionimbus.services.messaging;

import org.jboss.netty.channel.SimpleChannelHandler;

public class MessageServiceClientHandler extends SimpleChannelHandler {

//    private final Message message;
//
//    private final ChannelGroup group;
//
//    public MessageServiceClientHandler(Message message, ChannelGroup group) {
//        this.message = message;
//        this.group = group;
//    }
//
//    @Override
//    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
//            throws Exception {
//        Channel ch = e.getChannel();
//        group.add(ch);
//
//        ChannelFuture f = ch.write(message);
//        f.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                future.getChannel().close();
//            }
//        });
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
//            throws Exception {
//        e.getCause().printStackTrace();
//        e.getChannel().close();
//    }

}
