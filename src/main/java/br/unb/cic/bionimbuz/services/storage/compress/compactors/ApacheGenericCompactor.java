package br.unb.cic.bionimbuz.services.storage.compress.compactors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;

public abstract class ApacheGenericCompactor {

	public static void compact(File in, File output, String compressor) throws CompressorException, IOException {
		
			OutputStream out = new FileOutputStream(output);
			CompressorOutputStream cos;

			cos = new CompressorStreamFactory().createCompressorOutputStream(
					compressor, out);

			IOUtils.copy(new FileInputStream(in), cos);
			cos.close();
	}

}
