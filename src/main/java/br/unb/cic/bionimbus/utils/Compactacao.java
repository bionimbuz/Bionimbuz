package br.unb.cic.bionimbus.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

public class Compactacao {

	public static String compactar(String path) throws IOException {
		
		String compressed = path + ".cpt";
		
		FileOutputStream fos = new FileOutputStream(compressed);
        SnappyOutputStream sout = new SnappyOutputStream(fos);

        FileInputStream fis = new FileInputStream(path);
        BufferedInputStream input = new BufferedInputStream(fis);

        ByteArrayOutputStream orig = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        for (int readBytes = 0; (readBytes = input.read(tmp)) != -1;) {
            sout.write(tmp, 0, readBytes);
            orig.write(tmp, 0, readBytes);
        }
        input.close();
        sout.flush();
        sout.close();
        orig.flush();
        orig.close();
        fos.flush();
        fos.close();
        
        return compressed;

	}
	
	public static String descompactar(String path) throws IOException {
		
		String decompressed = path.replaceAll(".cpt","");
		
		FileOutputStream fos = new FileOutputStream(decompressed);

        FileInputStream fis = new FileInputStream(path);
        SnappyInputStream siut = new SnappyInputStream(fis);
        BufferedInputStream input = new BufferedInputStream(siut);
        
        byte[] tmp = new byte[1024];
        for (int readBytes = 0; (readBytes = input.read(tmp)) != -1;) {
            fos.write(tmp, 0, readBytes);
        }
        input.close();
        siut.close();
        fos.flush();
        fos.close();
        
        return decompressed;
        
	}

}
