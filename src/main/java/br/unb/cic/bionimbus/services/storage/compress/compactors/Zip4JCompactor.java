package br.unb.cic.bionimbus.services.storage.compress.compactors;

import java.io.File;
import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import br.unb.cic.bionimbus.services.storage.compress.Compactor;

public class Zip4JCompactor implements Compactor{

	@Override
	public File compact(File in, int compressionLevel) throws IOException{
		File out = new File(in.getName() + ".zip4j");

		try {

			ZipFile zipFile = new ZipFile(out.getPath());
			ZipParameters parametes = new ZipParameters();
			parametes.setCompressionLevel(compressionLevel);
			zipFile.addFile(in, parametes);

		} catch (ZipException e) {
			throw new IOException(e);
		}
		return out;
	}

}
