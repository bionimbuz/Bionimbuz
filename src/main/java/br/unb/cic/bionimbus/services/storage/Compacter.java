package br.unb.cic.bionimbus.services.storage;

import java.io.File;

/**
 * 
 * @author <img src="https://avatars2.githubusercontent.com/u/3778188?v=2&s=30" width="30" height="30"/> <a href="https://github.com/DRA2840" target="_blank"> DRA2840 - Diego Azevedo </a>
 */
public class Compacter {
	
	public enum Compression{NO_COMPRESSION, FAST_COMPRESSION, SLOW_COMPRESSION}
	
	private static final double COMPRESSION_RATIO_FAST = 0.60;
	private static final double COMPRESSION_RATIO_SLOW = 0.80;
	
	private static final long TIME_IN_MILIS_PER_MEGA_FAST = 10;
	private static final long TIME_IN_MILIS_PER_MEGA_SLOW = 120;
	
	
	/*
	 * O tempo por mega deve ser menor sem comprimir do que comprimindo para valer a pena:
	 * 
	 * 1/band < ratio/band + time
	 * 1/band < (ratio + time*band)/band
	 * 1 < ratio + time*band
	 * 1 - ratio < time*band
	 * (1 - ratio)/time < band
	 * 
	 * */
	private static final double UPPER_QUOTA = (1-COMPRESSION_RATIO_FAST)/TIME_IN_MILIS_PER_MEGA_FAST;
	private static final double LOWER_QUOTA = (1-COMPRESSION_RATIO_SLOW)/TIME_IN_MILIS_PER_MEGA_SLOW;
	
	/**
	 * 
	 * @param bandwidth Bandwidth in bytes per second (1MB/s -> 1024, for example)
	 * @return {@link Compression} if you should compress fast, slow, or don't compress at all
	 */
	public Compression compressionType(long bandwidth){
		
		if(UPPER_QUOTA < bandwidth){
			return Compression.NO_COMPRESSION;
		}else if(LOWER_QUOTA < bandwidth){
			return Compression.FAST_COMPRESSION;
		}
		return Compression.SLOW_COMPRESSION;
	}
	
	public File verifyAndCompress(File in, long bandwidth){
		
		Compression c = compressionType(bandwidth);
		
		if(c.equals(Compression.NO_COMPRESSION)){
			return in;
		}else if(c.equals(Compression.FAST_COMPRESSION)){
			return compressFast(in);
		}
		return compressSlow(in);
	}

	private File compressSlow(File in) {
		// TODO Auto-generated method stub
		return null;
	}

	private File compressFast(File in) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
