package br.unb.cic.bionimbuz.services.storage.compress.compactors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompressionUtils {

	
	public static String getName(String path){
		Pattern pat = Pattern.compile("(.*)\\/([a-zA-Z0-9 _\\.]+)");
		Matcher mat = pat.matcher(path);
		mat.matches();
		return mat.group(2);
	}
	
	public static String getParentFolder(String path){

		Pattern pat = Pattern.compile("(.*)\\/([a-zA-Z0-9 _\\.]+)");
		Matcher mat = pat.matcher(path);
		mat.matches();
		return mat.group(1);
	}
}
