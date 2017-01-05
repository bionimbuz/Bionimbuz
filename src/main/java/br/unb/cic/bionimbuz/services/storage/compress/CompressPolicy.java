/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.services.storage.compress;

import java.io.File;
import java.io.IOException;

import br.unb.cic.bionimbuz.services.storage.compress.compactors.ApacheXZCompactor;
import br.unb.cic.bionimbuz.services.storage.compress.compactors.GZipCompactor;
import br.unb.cic.bionimbuz.services.storage.compress.compactors.JavaZipCompactor;
import br.unb.cic.bionimbuz.services.storage.compress.compactors.Zip4JCompactor;

/**
 * 
 * @author <img src="https://avatars2.githubusercontent.com/u/3778188?v=2&s=30"
 *         width="30" height="30"/> <a href="https://github.com/DRA2840"
 *         target="_blank"> DRA2840 - Diego Azevedo </a>
 */
public class CompressPolicy {

	public enum Compression {
		NO_COMPRESSION, 
		SUPER_FAST_COMPRESSION, 
		FAST_COMPRESSION, 
		MEDIUM_COMPRESSION, 
		SLOW_COMPRESSION, 
		SUPER_SLOW_COMPRESSION,
		ULTRA_SLOW_COMPRESSION
	}

	private static final int COMPRESSION_LEVEL_SUPER_FAST = 1;
	private static final int COMPRESSION_LEVEL_FAST = 2;
	private static final int COMPRESSION_LEVEL_MEDIUM = 4;
	private static final int COMPRESSION_LEVEL_SLOW = 1;
	private static final int COMPRESSION_LEVEL_SUPER_SLOW = 1;
	private static final int COMPRESSION_LEVEL_ULTRA_SLOW = 1;

	/*
	 * O tempo por mega deve ser menor sem comprimir do que comprimindo para
	 * valer a pena:
	 * 
	 * 1/band < (1-ratio)/band + time
	 * 1/band < ((1 -ratio) + time*band)/band 
	 * 1 < (1-ratio) + time*band 
	 * ratio < time*band 
	 * ratio/time < band
	 * 
	 * a banda máxima para valer a pena é de 36.1277215253224 MB/s (usando Zip4J)
	 * 
	 * Para trocar entre os compactadores, deve se calcular o tempo de cada um e
	 * verificar onde há os cortes no gráfico:
	 * 
	 * (1-ratio1)/x + time1 = (1-ratio2)/x +time2
	 * 
	 * 
	 * De maneira ótima, a troca seria:
	 * 
	 * Zip4J Level 1 - a partir de 1.39205062775208 até 36.1277215253224 MB/s
	 * Zip4J Level 2 - a partir de 0.987069232426755 até 1.39205062775208 MB/s
	 * Zip4J Level 4 - a partir de 0.160482105353177 até 0.987069232426755 MB/s
	 * Java Zip - a partir de 0.0560584696222452 até 0.160482105353177 MB/s
	 * Java GZip - a partir de 0.0177536044302058 até 0.0560584696222452 MB/s
	 * Apache XZ - abaixo de 0.0177536044302058
	 */
	private static final double NO_COMPRESSION_QUOTA = 36.1277215253224;
	private static final double SUPER_FAST_QUOTA = 1.39205062775208;
	private static final double FAST_QUOTA = 0.987069232426755;
	private static final double MEDIUM_QUOTA = 0.160482105353177;
	private static final double SLOW_QUOTA = 0.0560584696222452;
	private static final double SUPER_SLOW_QUOTA = 0.0177536044302058;
	

	/**
	 * Define what compression to use:
	 * <ul>
	 * <li>NO_COMPRESSION: bandwidth so high the fastest compression is slower
	 * than just sending the file</li>
	 * <li>SUPER_FAST_COMPRESSION: Very High bandwidth. Compress the fastest possible</li>
	 * <li>FAST_COMPRESSION: High bandwidth. Compress quite fast, but a little better than the above</li>
	 * <li>MEDIUM_COMPRESSION: good bandwidth. Takes some time compressing</li>
	 * <li>SLOW_COMPRESSION: not so good bandwidth. Takes more time compressing to do a better job</li>
	 * <li>SUPER_SLOW_COMPRESSION: bad bandwidth. It's worth take a lot of time compressing</li>
	 * <li>ULTRA_SLOW_COMPRESSION: VERY bad bandwidth. It's worth take A LOT of time compressing. REALLY.</li>
	 * </ul>
	 * 
	 * @param bandwidth Bandwidth in Megabytes per second
	 * @return {@link Compression} if you should compress at all, very fast, fast, medium, slow, super slow or ultra slow
	 */
	private static Compression compressionType(double bandwidth) {

		if (NO_COMPRESSION_QUOTA < bandwidth ) {
			return Compression.NO_COMPRESSION;
		} else if (SUPER_FAST_QUOTA < bandwidth ) {
			return Compression.SUPER_FAST_COMPRESSION;
		} else if (FAST_QUOTA < bandwidth ) {
			return Compression.FAST_COMPRESSION;
		} else if (MEDIUM_QUOTA < bandwidth ) {
			return Compression.MEDIUM_COMPRESSION;
		} else if (SLOW_QUOTA < bandwidth ) {
			return Compression.SLOW_COMPRESSION;
		} else if (SUPER_SLOW_QUOTA < bandwidth ) {
			return Compression.SUPER_SLOW_COMPRESSION;
		}
		return Compression.ULTRA_SLOW_COMPRESSION;
	}

	/**
	 * Returns the file to be sent, based in the original file and the
	 * bandwidth. Probably is gonna return a compressed file.
	 * 
	 * @param in
	 *            {@link File} original file
	 * @param d
	 *            Bandwidth in bytes per second (1MB/s -> 1024, for example)
	 * @return {@link File} file to be sent. probably compressed.
	 * @throws IOException
	 */
	public static String verifyAndCompress(String in, double d) throws IOException {

		Compression c = compressionType(d);
		if (c.equals(Compression.NO_COMPRESSION)) {
			return in;
		} else if (c.equals(Compression.SUPER_FAST_COMPRESSION)) {
			return new Zip4JCompactor().compact(in, COMPRESSION_LEVEL_SUPER_FAST);
		} else if (c.equals(Compression.FAST_COMPRESSION)) {
			return new Zip4JCompactor().compact(in, COMPRESSION_LEVEL_FAST);
		} else if (c.equals(Compression.MEDIUM_COMPRESSION)) {
			return new Zip4JCompactor().compact(in, COMPRESSION_LEVEL_MEDIUM);
		} else if (c.equals(Compression.SLOW_COMPRESSION)) {
			return new JavaZipCompactor().compact(in, COMPRESSION_LEVEL_SLOW);
		} else if (c.equals(Compression.SUPER_SLOW_COMPRESSION)) {
			return new GZipCompactor().compact(in, COMPRESSION_LEVEL_SUPER_SLOW);
		}

		return new ApacheXZCompactor().compact(in, COMPRESSION_LEVEL_ULTRA_SLOW);
	}
	
	/**
	 * decompress a file based on the extension.
	 * 
	 * @param compressed {@link File}
	 * @return Uncompressed file.
	 * @throws IOException
	 */
	public static String decompress(String compressed) throws IOException{
		
		if (compressed.endsWith(".zip4j")){
			return new Zip4JCompactor().descompact(compressed);
		} else if(compressed.endsWith(".zip")){
			return new JavaZipCompactor().descompact(compressed);
		} else if(compressed.endsWith(".gzip")){
			return new GZipCompactor().descompact(compressed);
		} else if(compressed.endsWith(".xz")){
			return new ApacheXZCompactor().descompact(compressed);
		}
		return compressed;
	}
	
	public static void deleteIfCompressed(String file){
		if (file.endsWith(".zip4j") || file.endsWith(".zip") || file.endsWith(".gzip") || file.endsWith(".xz")){
			new File(file).delete();
		}
	}

}
