package br.unb.cic.bionimbus.services.messaging;

public class MessageServiceClient {

//    private final ChannelFactory factory = new NioClientSocketChannelFactory(
//            Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
//
////    private MessageService service;
//
//    private final ChannelGroup channelGroup = new DefaultChannelGroup(
//            "msg-client");

//    public void setService(MessageService service) {
//        this.service = service;
//    }
//
//    public MessageService getMessageService() {
//        return service;
//    }

//    public void shutdown() {
//        ChannelGroupFuture f = channelGroup.close();
//        f.awaitUninterruptibly();
//        factory.releaseExternalResources();
//    }
//
//    public void sendMessage(InetSocketAddress addr, Message message) {
//        ClientBootstrap client = new ClientBootstrap(factory);
//        client.setPipelineFactory(new MessageServiceClientPipelineFactory(message, channelGroup));
//        client.connect(addr);
//    }
//
//    public void sendFile(InetSocketAddress addr, String fileName) {
//        ClientBootstrap client = new ClientBootstrap(factory);
//        client.setPipelineFactory(new MessageServiceFileClientPipelineFactory(fileName));
//        client.connect(addr);
//    }
//
//    public void getFile(InetSocketAddress addr, String fileName, Map<String, String> parms) {
//        ClientBootstrap client = new ClientBootstrap(factory);
//        client.setPipelineFactory(new MessageServiceFileClientPipelineFactory(fileName, parms, true, this));
//        client.connect(addr);
//    }

}
