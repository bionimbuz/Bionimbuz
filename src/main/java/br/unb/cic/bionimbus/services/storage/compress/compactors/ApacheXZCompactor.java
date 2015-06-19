package br.unb.cic.bionimbus.services.storage.compress.compactors;

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import br.unb.cic.bionimbus.services.storage.compress.Compactor;

public class ApacheXZCompactor implements Compactor{

	@Override
	public File compact(File in, int compressionLevel) throws IOException {
		
		File out = new File(in.getName() + ".xz");
		
		try {
			ApacheGenericCompactor.compact(in, out, CompressorStreamFactory.XZ, compressionLevel);
		} catch (CompressorException e) {
			throw new IOException(e);
		}
		
		return out;
	}

}
