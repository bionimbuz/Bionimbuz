package br.unb.cic.bionimbus.p2p.udp;


import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class JsonUdpServerPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		
		ChannelPipeline p = Channels.pipeline();
	
		p.addLast("decoder", new JsonDecoder());
		p.addLast("encoder", new JsonEncoder());
		p.addLast("handler", new UdpServerHandler());
		return p;
	}
}
