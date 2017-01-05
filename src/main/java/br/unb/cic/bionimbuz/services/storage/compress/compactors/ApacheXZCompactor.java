package br.unb.cic.bionimbuz.services.storage.compress.compactors;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import br.unb.cic.bionimbuz.services.storage.compress.Compactor;

public class ApacheXZCompactor implements Compactor{

	@Override
	public String compact(String in, int compressionLevel) throws IOException {
		
		File out = new File(in + ".xz");
		
		try {
			ApacheGenericCompactor.compact(new File(in), out, CompressorStreamFactory.XZ);
		} catch (CompressorException e) {
			throw new IOException(e);
		}
		
		return out.getAbsolutePath();
	}

	@Override
	public String descompact(String compressed) throws IOException {
		
		String out = compressed.replace(".xz", "");
		
		FileInputStream fin = new FileInputStream(compressed);
		BufferedInputStream in = new BufferedInputStream(fin);
		FileOutputStream fos = new FileOutputStream(out);
		XZCompressorInputStream xzIn = new XZCompressorInputStream(in);
		final byte[] buffer = new byte[2*1024*1024];
		int n = 0;
		while (-1 != (n = xzIn.read(buffer))) {
		    fos.write(buffer, 0, n);
		}
		fos.close();
		xzIn.close();
		
		return out;
	}

}
