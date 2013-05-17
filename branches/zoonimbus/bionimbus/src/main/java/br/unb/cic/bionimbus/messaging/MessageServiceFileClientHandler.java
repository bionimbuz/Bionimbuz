package br.unb.cic.bionimbus.messaging;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.stream.ChunkedFile;

public class MessageServiceFileClientHandler extends
		SimpleChannelUpstreamHandler {

	private final String fileName;

	private final Map<String, String> parms;

	private final boolean isGet;

	private final MessageServiceClient client;

	private final String pathDir;

	private boolean readingChunks = false;

	private FileOutputStream fs;

	public MessageServiceFileClientHandler(String fileName,
			Map<String, String> parms, boolean isGet,
			MessageServiceClient client) {
		this.fileName = fileName;
		this.parms = parms;
		this.isGet = isGet;
		this.client = client;

		if (client != null)
			this.pathDir = client.getMessageService().getConfig()
					.getServerPath();
		else
			this.pathDir = "";
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if (!isGet) {
			sendFile(e.getChannel());
		} else {
			getFile(e.getChannel());
		}
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
		if (!readingChunks) {
			HttpResponse resp = (HttpResponse) e.getMessage();

			if (resp.getStatus().equals(HttpResponseStatus.CREATED)) {
				System.out.println("File sent succesfully");

				if (resp.isChunked())
					readingChunks = true;
				else
					e.getChannel().close();
				return;
			}

			fs = new FileOutputStream(pathDir + "/" + fileName);

			if (resp.isChunked()) {
				readingChunks = true;
			} else {
				ChannelBuffer content = resp.getContent();
				int length = content.readableBytes();
				if (content.readable()) {
					content.readBytes(fs, length);
				}
				fs.close();
				e.getChannel().close();
				client.getMessageService().recvFile(new File(pathDir + "/" + fileName), parms);
			}
		} else {
			HttpChunk chunk = (HttpChunk) e.getMessage();
			if (chunk.isLast()) {
				readingChunks = false;
				e.getChannel().close();
				if (isGet) {
					fs.close();
					client.getMessageService().recvFile(new File(pathDir + "/" + fileName), parms);
				}
			} else {
				ChannelBuffer content = chunk.getContent();
				int length = content.readableBytes();
				content.readBytes(fs, length);
			}
		}
	}

	private void getFile(Channel ch) {
		File file = new File(fileName);
		InetSocketAddress addr = (InetSocketAddress) ch.getRemoteAddress();

		HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.GET, "http://" + addr.getHostName() + ":"
						+ addr.getPort() + "/" + file.getName());
		req.setHeader(HttpHeaders.Names.HOST, addr.getHostName());
		req.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);

		ch.write(req);
	}

	private void sendFile(Channel ch) throws Exception {
		File file = new File(fileName);
		long length = file.length();

		InetSocketAddress addr = (InetSocketAddress) ch.getRemoteAddress();

		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.PUT, "http://" + addr.getHostName() + ":"
						+ addr.getPort() + "/" + file.getName());
		request.setHeader(HttpHeaders.Names.HOST, addr.getHostName());
		request.setHeader(HttpHeaders.Names.CONNECTION,
				HttpHeaders.Values.CLOSE);
		HttpHeaders.setContentLength(request, length);

		ch.write(request);
		ch.write(new ChunkedFile(file));
	}

}
