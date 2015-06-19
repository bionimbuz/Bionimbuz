package br.unb.cic.bionimbus.services.storage.compress.compactors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
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

}
