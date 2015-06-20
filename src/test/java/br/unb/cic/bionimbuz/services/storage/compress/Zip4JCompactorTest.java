package br.unb.cic.bionimbuz.services.storage.compress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import br.unb.cic.bionimbus.services.storage.compress.compactors.Zip4JCompactor;

public class Zip4JCompactorTest {
	
	@Test
	public void testCompressAndUncompress() throws IOException{
		
		File original = new File("src/test/resources/inputFiles/test.txt");
		
		Zip4JCompactor compactor = new Zip4JCompactor();
		
		File compressed = compactor.compact(original, 5);
		
		assertTrue("Compression OK", compressed.exists());
		assertFalse("Contents are different", ComparatorUtil.areFilesEqual(original, compressed));
		
		File uncompressed = compactor.descompact(compressed);
		
		assertTrue("Compression OK", uncompressed.exists());
		assertTrue("Contents are equal", ComparatorUtil.areFilesEqual(original, uncompressed));
		
		compressed.delete();
		uncompressed.delete();
		
	}

}
