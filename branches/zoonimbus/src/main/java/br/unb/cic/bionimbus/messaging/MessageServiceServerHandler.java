package br.unb.cic.bionimbus.messaging;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import br.unb.cic.bionimbus.p2p.messages.AbstractMessage;

public class MessageServiceServerHandler extends SimpleChannelHandler {
	
	private final MessageServiceServer server;
	
	public MessageServiceServerHandler(MessageServiceServer server) {
		this.server = server;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		server.getChannelGroup().add(e.getChannel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		InetSocketAddress addr = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
		AbstractMessage msg = (AbstractMessage) e.getMessage();
		if (msg.getPeer() != null)
			msg.getPeer().getHost().setAddress(addr.getAddress().getHostAddress());
		server.getMessageService().recvMessage(msg);
	}

}
