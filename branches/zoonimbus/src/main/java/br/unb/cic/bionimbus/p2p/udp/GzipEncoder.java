package br.unb.cic.bionimbus.p2p.udp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.util.CharsetUtil;


public class GzipEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		if (!(msg instanceof String)){
			return msg;
		}
		
		String raw = (String) msg;
				
		byte[] data = encode(raw.getBytes(CharsetUtil.UTF_8));
		int dataLength = data.length;

		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
//		buf.writeByte((byte) 'J'); //magic number
		buf.writeInt(dataLength);
		buf.writeBytes(data);
		return buf;
	}
	
	public byte[] encode(byte[] raw) throws IOException {
			
		//TODO: substituir pelo FileBackedOutputStream do Guava
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		GZIPOutputStream gos = new GZIPOutputStream(baos);
		
		gos.write(raw);
		gos.close();
		
		return baos.toByteArray();		
	}
}
