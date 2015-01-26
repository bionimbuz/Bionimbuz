package br.unb.cic.bionimbus.services.messaging;

public class MessageServiceFileClientPipelineFactory {// implements ChannelPipelineFactory {

//    private final String fileName;
//
//    private final Map<String, String> parms;
//
//    private final boolean isGet;
//
//    private final MessageServiceClient client;
//
//    public MessageServiceFileClientPipelineFactory(String fileName) {
//        this.fileName = fileName;
//        this.parms = null;
//        this.isGet = false;
//        this.client = null;
//    }
//
//    public MessageServiceFileClientPipelineFactory(String fileName,
//                                                   Map<String, String> parms, boolean isGet,
//                                                   MessageServiceClient client) {
//        this.fileName = fileName;
//        this.parms = parms;
//        this.isGet = isGet;
//        this.client = client;
//    }

//    @Override
//    public ChannelPipeline getPipeline() throws Exception {
//        ChannelPipeline pipeline = Channels.pipeline();
//
//        pipeline.addLast("codec", new HttpClientCodec());
//        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
//        pipeline.addLast("handler", new MessageServiceFileClientHandler(
//                fileName, parms, isGet, client));
//
//        return pipeline;
//    }

}
