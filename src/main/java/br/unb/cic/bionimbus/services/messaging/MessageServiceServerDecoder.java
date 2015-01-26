package br.unb.cic.bionimbus.services.messaging;

public class MessageServiceServerDecoder {

//    private final MessageFactory factory;
//
//    private final MessageServiceServer server;
//
//    public MessageServiceServerDecoder(MessageFactory factory, MessageServiceServer server) {
//        this.factory = factory;
//        this.server = server;
//    }

//    @Override
//    protected Object decode(ChannelHandlerContext ctx, Channel ch,
//                            ChannelBuffer buf) throws Exception {
//        if (buf.readableBytes() < 1)
//            return null;
//
//        ChannelPipeline pipeline = ctx.getPipeline();
//        final char magic = (char) buf.getByte(buf.readerIndex());
//
//        if (magic == 'X') {
//            pipeline.addLast("decoder", new MessageDecoder(factory));
//            pipeline.addLast("handler", new MessageServiceServerHandler(server));
//            pipeline.remove(this);
//        } else {
//            pipeline.addLast("codec", new HttpServerCodec());
//            pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
//            pipeline.addLast("handler", new MessageServiceServerHttpHandler(server));
//            pipeline.remove(this);
//        }
//        return buf.readBytes(buf.readableBytes());
//    }

}
