package br.unb.cic.bionimbus.services.messaging;

public class MessageServiceServer {

//    private final ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
//
//    private final ChannelGroup channelGroup = new DefaultChannelGroup("msg-server");
//
//    //private MessageService service;
//
//    public void start(MessageService service, MessageFactory messageFactory) {
//        //this.service = service;
//        ServerBootstrap server = new ServerBootstrap(factory);
//        server.setPipelineFactory(new MessageServiceServerPipelineFactory(this, messageFactory));
//        //server.bind(service.getSocket());
//    }
//
//    public void shutdown() {
//        ChannelGroupFuture f = channelGroup.close();
//        f.awaitUninterruptibly();
//        factory.releaseExternalResources();
//    }
//
//    public ChannelGroup getChannelGroup() {
//        return channelGroup;
//    }
//
//    //public MessageService getMessageService() {
//    //    return service;
//    //}
//
//    public String getPathDir() {
//        //return service.getConfig().getServerPath();
//        return null;
//    }

}
