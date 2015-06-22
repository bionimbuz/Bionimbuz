package br.unb.cic.bionimbuz.services.storage.compress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.io.Files;

import br.unb.cic.bionimbus.services.storage.compress.compactors.GZipCompactor;

public class GZipCompactorTest {

	@Test
	public void testCompressAndUncompress() throws IOException{
		
		String original = "src/test/resources/inputFiles/test.txt";
		
		GZipCompactor compactor = new GZipCompactor();
		
		String compressed = "src/test/resources/outputFiles/test.txt.gzip";
		
		Files.move(new File(compactor.compact(original, 5)), new File(compressed));
		
		assertTrue("Compression OK", new File(compressed).exists());
		assertFalse("Contents are different", ComparatorUtil.areFilesEqual(original, compressed));
		
		String uncompressed = compactor.descompact(compressed);
		
		assertTrue("Compression OK", new File(uncompressed).exists());
		assertTrue("Contents are equal", ComparatorUtil.areFilesEqual(original, uncompressed));
		
		new File(compressed).delete();
		new File(uncompressed).delete();
		
	}
	
	
}
