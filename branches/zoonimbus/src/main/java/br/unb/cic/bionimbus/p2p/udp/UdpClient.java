package br.unb.cic.bionimbus.p2p.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

public final class UdpClient {
		
	private ChannelFactory factory;
	private ConnectionlessBootstrap bootstrap;
	private DatagramChannel channel;
	
	public UdpClient() {
		factory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
		bootstrap = new ConnectionlessBootstrap(factory);
		
		bootstrap.setPipelineFactory(new JsonUdpClientPipelineFactory());
		
		bootstrap.setOption("reuseAddress", true);
		
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("broadcast", "false");
		bootstrap.setOption("sendBufferSize", 65536);
		bootstrap.setOption("receiveBufferSize", 65536);
		
		channel = (DatagramChannel) bootstrap.bind(new InetSocketAddress(0));
	}
	
	public void sendAsyncMessage(String address, int port, WireMessage message) throws IOException {
		
		ChannelFuture future = channel.write(message, new InetSocketAddress(address, port));
		
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture cf) throws Exception {
				System.out.println("Message sent!");
//				cf.getChannel().close();
//				factory.releaseExternalResources();
			}			
		});	
	}
	
	public void sendAsyncMessage(String address, int port, WireMessage message, UdpEventListener listener, long timeout) {
		ChannelFuture future = channel.write(message, new InetSocketAddress(address, port));
		
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture cf) throws Exception {
				// start clock
				
//				cf.getChannel().close();
//				factory.releaseExternalResources();
			}			
		});			
	}
	
	public void stop() {
		channel.close();
		factory.releaseExternalResources();
	}

}
