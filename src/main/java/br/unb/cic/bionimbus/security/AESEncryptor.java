/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.security;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author rafael
 */
public class AESEncryptor {
    static String encryptionKey = "zoonimbus1234567";
    
    public void encrypt(String filePath) throws Exception {
        String plainText;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            plainText = sb.toString();
        }

        Cipher cipher = Cipher.getInstance("AES", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] bytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        
        File newEncryptFile = new File(filePath + ".aes");
        FileOutputStream output = new FileOutputStream(newEncryptFile);
        IOUtils.write(bytes, output);

        File file =  new File(filePath);        
        File oldFile = new File(filePath + "_old.txt");
        file.renameTo(oldFile);
        file.delete();       
        newEncryptFile.renameTo(file);
    }

    public void decrypt(String filePath) throws Exception {
        File file = new File(filePath);
        byte[] cipherText = Files.toByteArray(file);
        
        Cipher cipher = Cipher.getInstance("AES", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        String originalText = new String(cipher.doFinal(cipherText), "UTF-8");
        
        File newDecryptFile = new File(filePath + ".aes");
        try (FileWriter fileWriter = new FileWriter(newDecryptFile)) {
            fileWriter.write(originalText);
            fileWriter.flush();
        }
        
        file.delete();
        newDecryptFile.renameTo(file);
    }
}
