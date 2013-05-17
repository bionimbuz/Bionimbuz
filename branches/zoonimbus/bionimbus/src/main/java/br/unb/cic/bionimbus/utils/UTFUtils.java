package br.unb.cic.bionimbus.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class UTFUtils {
	
	private UTFUtils() {}
	
	public static void writeString(OutputStream outputStream, String data) throws IOException {
		DataOutputStream dos = new DataOutputStream(outputStream);
		dos.writeUTF(data);
		dos.flush();
	}

	public static String readString(InputStream inputStream) throws IOException {
		DataInputStream dais = new DataInputStream(inputStream);
		return dais.readUTF();
	}		

}
