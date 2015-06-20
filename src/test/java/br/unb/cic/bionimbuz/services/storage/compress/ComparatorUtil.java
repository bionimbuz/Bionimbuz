package br.unb.cic.bionimbuz.services.storage.compress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class ComparatorUtil {

	public static boolean areFilesEqual(File f1, File f2) throws IOException{
		
		BufferedReader reader1 = new BufferedReader(new FileReader(f1));
		BufferedReader reader2 = new BufferedReader(new FileReader(f2));
		
		boolean result = reader1.readLine().equals(reader2.readLine());
		
		IOUtils.closeQuietly(reader1);
		IOUtils.closeQuietly(reader2);
		
		return result;
	}
	
}
