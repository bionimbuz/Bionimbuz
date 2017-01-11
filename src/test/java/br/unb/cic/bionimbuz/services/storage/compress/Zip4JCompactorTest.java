package br.unb.cic.bionimbuz.services.storage.compress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import br.unb.cic.bionimbuz.services.storage.compress.compactors.Zip4JCompactor;

import com.google.common.io.Files;

public class Zip4JCompactorTest {
	
	@Test
	public void testCompressAndUncompress() throws IOException{
		
		String currentDirectory = new File(".").getCanonicalPath();
		
		String original = currentDirectory + "/src/test/resources/inputFiles/test.txt";
		
		Zip4JCompactor compactor = new Zip4JCompactor();
		
		String compressed = currentDirectory + "/src/test/resources/outputFiles/test.txt.zip4j";
		File comp = new File(compressed);
		
		Files.move(new File(compactor.compact(original, 5)), comp);
		
		assertTrue("Compression OK", comp.exists());
		assertFalse("Contents are different", ComparatorUtil.areFilesEqual(original, compressed));
		
		String uncompressed = compactor.descompact(compressed);
		System.out.println(uncompressed);
		assertTrue("Compression OK", new File(uncompressed).exists());
		assertTrue("Contents are equal", ComparatorUtil.areFilesEqual(original, uncompressed));
		
		new File(compressed).delete();
		new File(uncompressed).delete();
		
	}

}
