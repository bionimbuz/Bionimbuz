package br.unb.cic.bionimbus.services.storage.compress;

import java.io.File;
import java.io.IOException;

public interface Compactor {

	public File compact(File in, int compressionLevel) throws IOException;
	
	public File descompact(File compressed) throws IOException;
}
