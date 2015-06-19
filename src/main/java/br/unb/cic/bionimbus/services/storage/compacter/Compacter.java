package br.unb.cic.bionimbus.services.storage.compacter;

import java.io.File;
import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

/**
 * 
 * @author <img src="https://avatars2.githubusercontent.com/u/3778188?v=2&s=30"
 *         width="30" height="30"/> <a href="https://github.com/DRA2840"
 *         target="_blank"> DRA2840 - Diego Azevedo </a>
 */
public class Compacter {

	public enum Compression {
		NO_COMPRESSION, FAST_COMPRESSION, SLOW_COMPRESSION
	}

	private static final int COMPRESSION_LEVEL_FAST = 1;
	private static final int COMPRESSION_LEVEL_MEDIUM = 4;
	//private static final int COMPRESSION_LEVEL_SLOW = 5; //Not used by Java GZip

	private static final double COMPRESSION_RATIO_FAST = 0.60;
	private static final double COMPRESSION_RATIO_MEDIUM = 0.60;
	private static final double COMPRESSION_RATIO_SLOW = 0.80;

	private static final long TIME_IN_MILIS_PER_MEGA_FAST = 10;
	private static final long TIME_IN_MILIS_PER_MEGA_MEDIUM = 120;
	private static final long TIME_IN_MILIS_PER_MEGA_SLOW = 120;

	/*
	 * O tempo por mega deve ser menor sem comprimir do que comprimindo para
	 * valer a pena:
	 * 
	 * 1/band < (1-0.80)/band + 0.6
	 * 1/band < ((1 -ratio) + time*band)/band
	 * 1 < (1-ratio) + time*band
	 * ratio < time*band
	 * ratio/time < band
	 * 
	 * a banda máxima para valer é de 36.1277215253224 MB/s (usando Zip4J)
	 * 
	 * Para trocar entre os compactadores, deve se calcular o tempo de cada um e verificar onde há os cortes no gráfico:
	 * 
	 * (1-ratio1)/x + time1 = (1-ratio2)/x +time2
	 * 
	 * De maneira ótima, a troca seria:
	 * 
	 * Zip4J Level 1 - a partir de 1.39205062775208 até 36.1277215253224 MB/s
	 * Zip4J Level 2 - a partir de 0.987069232426755 até 1.39205062775208
	 * Zip4J Level 4 - a partir de 0.160482105353177 até 0.987069232426755
	 * Java Zip      - a partir de 0.0560584696222452 até 0.160482105353177
	 * Java GZip     - a partir de 0.0177536044302058 até 0.0560584696222452
	 * Apache XZ     - abaixo de 0.0177536044302058
	 * 
	 * 
	 * 
	 */
	private static final double UPPER_QUOTA_IN_MEGAS_PER_SECOND = COMPRESSION_RATIO_FAST
			/ (TIME_IN_MILIS_PER_MEGA_FAST/1000);
	private static final double LOWER_QUOTA_IN_MEGAS_PER_SECOND = COMPRESSION_RATIO_SLOW
			/ (TIME_IN_MILIS_PER_MEGA_SLOW/1000);

	/**
	 * Define what compression to use:
	 * <ul>
	 * <li>NO_COMPRESSION: bandwidth so high the fastest compression is slower
	 * than just sending the file</li>
	 * <li>FAST_COMPRESSION: normal bandwidth. Compress the fastest possible and
	 * send it</li>
	 * <li>SLOW_COMPRESSION: super low bandwidth. It's better to take a lot of
	 * time compressing the file before sending it</li>
	 * </ul>
	 * 
	 * @param bandwidth
	 *            Bandwidth in bytes per second (1MB/s -> 1024, for example)
	 * @return {@link Compression} if you should compress fast, slow, or don't
	 *         compress at all
	 */
	public Compression compressionType(long bandwidth) {

		if (UPPER_QUOTA_IN_MEGAS_PER_SECOND < bandwidth/1024) {
			return Compression.NO_COMPRESSION;
		} else if (LOWER_QUOTA_IN_MEGAS_PER_SECOND < bandwidth/1024) {
			return Compression.FAST_COMPRESSION;
		}
		return Compression.SLOW_COMPRESSION;
	}

	/**
	 * Returns the file to be sent, based in the original file and the bandwidth.
	 * Probably is gonna return a compressed file.
	 * 
	 * @param in {@link File} original file
	 * @param bandwidth Bandwidth in bytes per second (1MB/s -> 1024, for example)
	 * @return {@link File} file to be sent. probably compressed.
	 * @throws IOException
	 */
	public File verifyAndCompress(File in, long bandwidth) throws IOException {

		Compression c = compressionType(bandwidth);
		if (c.equals(Compression.NO_COMPRESSION)) {
			return in;
		} else if (c.equals(Compression.FAST_COMPRESSION)) {
			return compressFast(in);
		}

		return compressMedium(in);
	}

	/**
	 * Compress the file the fastest possible. Uses zip4j, with the lowest compression level (1)
	 * compression 0 just creates the file, but don't compress, that's why it's not used.
	 * 
	 * @param in {@link File} to be compressed
	 * @return compressed {@link File}
	 * @throws IOException
	 */
	private File compressFast(File in) throws IOException {

		return compressZip4J(COMPRESSION_LEVEL_FAST, in);
	}

	/**
	 * Compress the file a little slower but more efficient. 
	 * Uses zip4j, with the medium compression level (5)
	 * 
	 * @param in {@link File} to be compressed
	 * @return compressed {@link File}
	 * @throws IOException
	 */
	private File compressMedium(File in) throws IOException {
		
		return compressZip4J(COMPRESSION_LEVEL_MEDIUM, in);
	}
	
	/**
	 * Compress using Zip4J
	 * @param level
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private File compressZip4J(int level, File in) throws IOException{
		File out = new File(in.getName() + ".zip4j");

		try {

			ZipFile zipFile = new ZipFile(out.getPath());
			ZipParameters parametes = new ZipParameters();
			parametes.setCompressionLevel(level);
			zipFile.addFile(in, parametes);

		} catch (ZipException e) {
			throw new IOException(e);
		}
		return out;
	}

}
