package br.unb.cic.bionimbus.messaging;

import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class MessageServiceServer {

    private final ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

    private final ChannelGroup channelGroup = new DefaultChannelGroup("msg-server");

    private MessageService service;

    public void start(MessageService service, MessageFactory messageFactory) {
        this.service = service;
        ServerBootstrap server = new ServerBootstrap(factory);
        server.setPipelineFactory(new MessageServiceServerPipelineFactory(this, messageFactory));
        server.bind(service.getSocket());
    }

    public void shutdown() {
        ChannelGroupFuture f = channelGroup.close();
        f.awaitUninterruptibly();
        factory.releaseExternalResources();
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public MessageService getMessageService() {
        return service;
    }

    public String getPathDir() {
        return service.getConfig().getServerPath();
    }

}
