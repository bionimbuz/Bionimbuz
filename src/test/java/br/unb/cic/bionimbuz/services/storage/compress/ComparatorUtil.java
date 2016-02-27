package br.unb.cic.bionimbuz.services.storage.compress;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class ComparatorUtil {

	public static boolean areFilesEqual(String f1, String f2) throws IOException{
		
		BufferedReader reader1 = new BufferedReader(new FileReader(f1));
		BufferedReader reader2 = new BufferedReader(new FileReader(f2));
		
		return IOUtils.contentEquals(reader1, reader2);
		
	}
	
}
