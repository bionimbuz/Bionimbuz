package br.unb.cic.bionimbuz.services.storage.compress;

import java.io.File;
import java.io.IOException;

public interface Compactor {

	/**
	 * Compact a {@link File}
	 * 
	 * @param in
	 *            {@link File} to be compressed
	 * @param compressionLevel
	 *            Some compressors allow you to tell them how much they should
	 *            compress. Not always used.
	 * @return compressed {@link File}
	 * @throws IOException
	 */
	public String compact(String in, int compressionLevel) throws IOException;

	/**
	 * Descompact a {@link File}
	 * 
	 * @param compressed
	 *            {@link File} to be uncompressed
	 * @return uncompressed {@link File}
	 * @throws IOException
	 */
	public String descompact(String compressed) throws IOException;
}
