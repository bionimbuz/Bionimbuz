package br.unb.cic.bionimbuz.services.storage.compress;

import org.junit.Assert;
import org.junit.Test;

import br.unb.cic.bionimbuz.services.storage.compress.compactors.CompressionUtils;

public class CompressionUtilsTest {
	
	@Test
	public void testGetName(){
		
		Assert.assertEquals("test.txt", CompressionUtils.getName("/Users/diego/test.txt"));
	}
	
	@Test
	public void testGetParentDirectory(){
		
		Assert.assertEquals("/Users/diego", CompressionUtils.getParentFolder("/Users/diego/test.txt"));
	}

}
