package br.unb.cic.bionimbus.p2p.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class GzipDecoder extends FrameDecoder {
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

		//wait until the length prefix is available (in bytes)
		if (buffer.readableBytes() < 4){
			return null;
		}

		buffer.markReaderIndex();

		//wait until the whole data is available
		int dataLength = buffer.readInt();
		if (buffer.readableBytes() < dataLength){
			buffer.resetReaderIndex();
			return null;
		}

		byte[] decoded = new byte[dataLength];
		buffer.readBytes(decoded);

		return deserialize(decoded);
	}

	private Object deserialize(byte[] buf) throws CorruptedFrameException, IOException {
		
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(buf));
		
		byte[] data = new byte[buf.length];
		gis.read(data, 0, data.length);
		
		ChannelBuffer cb = ChannelBuffers.dynamicBuffer();
		cb.writeInt(data.length);
		cb.writeBytes(data);
		return cb;
	}
}
