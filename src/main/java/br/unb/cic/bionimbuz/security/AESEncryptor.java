/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author rafael
 */
public class AESEncryptor {
    static String encryptionKey = "zoonimbus1234567";
    
    public void encrypt(String filePath) throws Exception {
        Cipher cipher = Cipher.getInstance("AES", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] input = new byte[64];
        int bytesRead;

        FileInputStream inFile = new FileInputStream(filePath);
        FileOutputStream outFile = new FileOutputStream(filePath + ".aes");

        while ((bytesRead = inFile.read(input)) != -1) {
            byte[] output = cipher.update(input, 0, bytesRead);
            if (output != null) {
                outFile.write(output);
            }
        }

        byte[] output = cipher.doFinal();
        if (output != null) {
            outFile.write(output);
        }
                
        inFile.close();
        outFile.flush();
        outFile.close();
        
        //Sobreescreve o arquivo
        File file =  new File(filePath);        
        File oldFile = new File(filePath + ".old");
        file.renameTo(oldFile);
        file.delete();       
        File newFile = new File(filePath + ".aes");
        newFile.renameTo(file);   
    }

    public void decrypt(String filePath) throws Exception {     
        Cipher cipher = Cipher.getInstance("AES", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        FileInputStream fis = new FileInputStream(filePath);
        FileOutputStream fos = new FileOutputStream(filePath + ".aes");
        byte[] in = new byte[64];
        int read;
        while ((read = fis.read(in)) != -1) {
            byte[] output = cipher.update(in, 0, read);
            if (output != null) {
                fos.write(output);
            }
        }

        byte[] output = cipher.doFinal();
        if (output != null) {
            fos.write(output);
        }
        fis.close();
        fos.flush();
        fos.close();
        
        File file = new File(filePath);
        file.delete();
        File newFile = new File(filePath + ".aes");
        newFile.renameTo(file);
    }
    
    public void setCorrectFilePath(String filePath) {
        File file =  new File(filePath);          
        File newFile = new File(filePath + ".aes");
        file.renameTo(newFile);
        File oldFile = new File(filePath + ".old");
        oldFile.renameTo(file);
        oldFile.delete();
    }
}
