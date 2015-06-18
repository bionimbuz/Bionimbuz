package br.unb.cic.bionimbus.services.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import org.apache.commons.io.IOUtils;

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
	// private static final int COMPRESSION_LEVEL_SLOW = 5; //Not used by Java
	// Zip

	private static final double COMPRESSION_RATIO_FAST = 0.60;
	private static final double COMPRESSION_RATIO_SLOW = 0.80;

	private static final long TIME_IN_MILIS_PER_MEGA_FAST = 10;
	private static final long TIME_IN_MILIS_PER_MEGA_SLOW = 120;

	/*
	 * O tempo por mega deve ser menor sem comprimir do que comprimindo para
	 * valer a pena:
	 * 
	 * 1/band < ratio/band + time
	 * 1/band < (ratio + time*band)/band
	 * 1 < ratio + time*band
	 * 1 - ratio < time*band
	 * (1 - ratio)/time < band
	 * 
	 * 
	 */
	private static final double UPPER_QUOTA_IN_MEGAS_PER_SECOND = (1 - COMPRESSION_RATIO_FAST)
			/ (TIME_IN_MILIS_PER_MEGA_FAST/1000);
	private static final double LOWER_QUOTA_IN_MEGAS_PER_SECOND = (1 - COMPRESSION_RATIO_SLOW)
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

		return compressSlow(in);
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

		File out = new File(in.getName() + ".zip4j");

		try {

			ZipFile zipFile = new ZipFile(out.getPath());
			ZipParameters parametes = new ZipParameters();
			parametes.setCompressionLevel(COMPRESSION_LEVEL_FAST);
			zipFile.addFile(in, parametes);

		} catch (ZipException e) {
			throw new IOException(e);
		}
		return out;
	}

	/**
	 * Compress the file the fastest possible. Uses java default implementation for Zip.
	 * 
	 * @param in {@link File} to be compressed
	 * @return compressed {@link File}
	 * @throws IOException
	 */
	private File compressSlow(File in) throws IOException {
		File out = new File(in.getName() + ".zip");

		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(out));

		ZipEntry entry = new ZipEntry(in.getPath());
		zip.putNextEntry(entry);

		zip.write(IOUtils.toByteArray(new FileReader(in)));

		IOUtils.closeQuietly(zip);
		return out;
	}

}
