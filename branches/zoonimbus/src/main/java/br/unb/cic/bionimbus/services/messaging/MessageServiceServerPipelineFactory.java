package br.unb.cic.bionimbus.services.messaging;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class MessageServiceServerPipelineFactory implements
        ChannelPipelineFactory {

    private final MessageServiceServer server;

    private final MessageFactory factory;

    public MessageServiceServerPipelineFactory(MessageServiceServer server, MessageFactory factory) {
        this.server = server;
        this.factory = factory;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("first", new MessageServiceServerDecoder(factory, server));

        return pipeline;
    }

}
