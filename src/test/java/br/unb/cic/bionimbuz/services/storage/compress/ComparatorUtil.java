package br.unb.cic.bionimbuz.services.storage.compress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class ComparatorUtil {

	public static boolean areFilesEqual(String f1, String f2) throws IOException{
		
		BufferedReader reader1 = new BufferedReader(new FileReader(f1));
		BufferedReader reader2 = new BufferedReader(new FileReader(f2));
		
		String linha1 = reader1.readLine();
		String linha2 = reader2.readLine();
		
		System.out.println("Linha1: " + linha1);
		System.out.println("Linha2: " + linha2);
		System.out.println("Tamanho: " + new File(f2).length());
		System.out.println("Tamanho2: " + (int) new File(f2).getTotalSpace());
		
		IOUtils.closeQuietly(reader1);
		IOUtils.closeQuietly(reader2);
		
		return linha1.equals(linha2);
	}
	
}
