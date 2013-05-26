package br.unb.cic.bionimbus.messaging;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;

public class MessageServiceClientPipelineFactory implements
        ChannelPipelineFactory {

    private final Message message;

    private final ChannelGroup group;

    public MessageServiceClientPipelineFactory(Message message, ChannelGroup group) {
        this.message = message;
        this.group = group;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("encoder", new MessageEncoder());
        pipeline.addLast("handler", new MessageServiceClientHandler(message, group));

        return pipeline;
    }

}
