package br.unb.cic.bionimbus.services.storage.compress.compactors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import br.unb.cic.bionimbus.services.storage.compress.Compactor;

public class GZipCompactor implements Compactor{

	@Override
	public File compact(File in, int compressionLevel) throws IOException {
		
		File out = new File(in.getName()+ ".gzip");
		GZIPOutputStream gzip;
		gzip = new GZIPOutputStream( new FileOutputStream(out));
		gzip.write(IOUtils.toByteArray(new FileReader(in)));
		IOUtils.closeQuietly(gzip);
		
		return out;
	}

	
	
}
