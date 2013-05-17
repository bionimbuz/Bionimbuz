package br.unb.cic.bionimbus.messaging;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class MessageDecoder extends FrameDecoder {

	private final MessageFactory factory;

	public MessageDecoder(MessageFactory factory) {
		this.factory = factory;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

		if (buffer.readableBytes() < 9)
			return null;

		buffer.markReaderIndex();

		// droping magic char
		buffer.readByte();

		final int length = buffer.readInt();
		final int type = buffer.readInt();
		byte[] decoded = null;

		if (length > 0) {
			if (buffer.readableBytes() < length) {
				buffer.resetReaderIndex();
				return null;
			}
			decoded = new byte[length];
			buffer.readBytes(decoded);
		}

		return factory.getMessage(type, decoded);
	}

}
