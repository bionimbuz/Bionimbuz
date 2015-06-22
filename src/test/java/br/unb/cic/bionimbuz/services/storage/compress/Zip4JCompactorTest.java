package br.unb.cic.bionimbuz.services.storage.compress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.io.Files;

import br.unb.cic.bionimbus.services.storage.compress.compactors.Zip4JCompactor;

public class Zip4JCompactorTest {
	
	@Test
	public void testCompressAndUncompress() throws IOException{
		
		String currentDirectory = new File(".").getCanonicalPath();
		
		String original = currentDirectory + "/src/test/resources/inputFiles/test.txt";
		
		Zip4JCompactor compactor = new Zip4JCompactor();
		
		String compressed = currentDirectory + "/src/test/resources/outputFiles/test.txt.zip4j";
		
		Files.move(new File(compactor.compact(original, 5)), new File(compressed));
		
		assertTrue("Compression OK", new File(compressed).exists());
		assertFalse("Contents are different", ComparatorUtil.areFilesEqual(original, compressed));
		
		String uncompressed = compactor.descompact(compressed);
		System.out.println(uncompressed);
		assertTrue("Compression OK", new File(uncompressed).exists());
		assertTrue("Contents are equal", ComparatorUtil.areFilesEqual(original, uncompressed));
		
		new File(compressed).delete();
		new File(uncompressed).delete();
		
	}

}
