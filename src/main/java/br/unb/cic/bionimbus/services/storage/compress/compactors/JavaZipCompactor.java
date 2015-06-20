package br.unb.cic.bionimbus.services.storage.compress.compactors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import br.unb.cic.bionimbus.services.storage.compress.Compactor;

public class JavaZipCompactor implements Compactor {

	@Override
	public File compact(File in, int compressionLevel) throws IOException {
		File out = new File(in.getName() + ".zip");
		ZipOutputStream zip;
		zip = new ZipOutputStream(new FileOutputStream(out));

		ZipEntry entry = new ZipEntry(in.getPath());
		zip.putNextEntry(entry);

		zip.write(IOUtils.toByteArray(new FileReader(in)));
		
		IOUtils.closeQuietly(zip);
		
		return out;
	}
	
	@Override
	public File descompact(File compressed) throws IOException {
		
		File out = new File(compressed.getName().replace(".zip", ""));
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buffer = new byte[(int)compressed.getTotalSpace()];
		
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
		
		return out;
	}

}
