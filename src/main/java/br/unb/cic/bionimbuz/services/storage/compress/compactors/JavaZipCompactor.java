package br.unb.cic.bionimbuz.services.storage.compress.compactors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import br.unb.cic.bionimbuz.services.storage.compress.Compactor;

public class JavaZipCompactor implements Compactor {

	@Override
	public String compact(String in, int compressionLevel) throws IOException {
		File out = new File(in + ".zip");
		ZipOutputStream zip;
		zip = new ZipOutputStream(new FileOutputStream(out));

		ZipEntry entry = new ZipEntry(CompressionUtils.getName(in));
		zip.putNextEntry(entry);

		zip.write(IOUtils.toByteArray(new FileReader(in)));
		
		IOUtils.closeQuietly(zip);
		
		return out.getAbsolutePath();
	}
	
	@Override
	public String descompact(String compressed) throws IOException {
		
		String name = CompressionUtils.getName(compressed).replace(".zip", "");
		String folder = CompressionUtils.getParentFolder(compressed);
		
		File out = new File(folder + name);
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buffer = new byte[2*1024*1024];
		
		ZipInputStream zip;
		zip = new ZipInputStream( new FileInputStream(compressed));
		
		zip.getNextEntry();
		int len;
        while ((len = zip.read(buffer)) > 0) {
        	fos.write(buffer, 0, len);
        }
		fos.flush();
		IOUtils.closeQuietly(zip);
		IOUtils.closeQuietly(fos);
		
		return out.getAbsolutePath();
	}

}
