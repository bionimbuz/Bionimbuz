/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author rafael
 */
public class AESEncryptor {

    private static final byte[] keyValue = new byte[]{'Z', 'o', 'o', 'n', 'i', 'm', 'b', 'u', 's', '1', '2', '3', '4', '5', '6', '7'};

    public void encrypt(String filePath) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        Cipher aesCipher = Cipher.getInstance("AES");  //getting cipher for AES
        aesCipher.init(Cipher.ENCRYPT_MODE, key);  //initializing cipher for encryption with key

        String newFilePath = filePath + ".aes";
        //creating file output stream to write to file
        try (FileOutputStream fos = new FileOutputStream(newFilePath)) {
            //creating object output stream to write objects to file
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            //creating file input stream to read contents for encryption
            try (FileInputStream fis = new FileInputStream(filePath)) {
                //creating cipher output stream to write encrypted contents
                try (CipherOutputStream cos = new CipherOutputStream(fos, aesCipher)) {
                    int read;
                    byte buf[] = new byte[4096];
                    while ((read = fis.read(buf)) != -1) //reading from file 
                    {
                        cos.write(buf, 0, read);  //encrypting and writing to file
                    }
                }
            }
        }
        File file = new File(filePath);
        file.delete();
        File newFile = new File(newFilePath);
        newFile.renameTo(file);
    }

    public void decrypt(String filePath) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        Cipher aesCipher = Cipher.getInstance("AES");  //getting cipher for AES
        aesCipher.init(Cipher.DECRYPT_MODE, key);  //initializing cipher for decryption with key
        
        //Came back to the original file name
        String newFilePath = filePath + ".aes";
        //creating file input stream to read from file
        try (FileInputStream fis = new FileInputStream(filePath)) {
            //creating object input stream to read objects from file
            ObjectInputStream ois = new ObjectInputStream(fis);
            //creating file output stream to write back original contents
            try (FileOutputStream fos = new FileOutputStream(newFilePath)) {
                //creating cipher input stream to read encrypted contents
                try (CipherInputStream cis = new CipherInputStream(fis, aesCipher)) {
                    int read;
                    byte buf[] = new byte[4096];
                    while ((read = cis.read(buf)) != -1) //reading from file
                    {
                        fos.write(buf, 0, read);  //decrypting and writing to file
                    }
                }
            }            
        }
        File file = new File(filePath);
        file.delete();
        File newFile = new File(newFilePath);
        newFile.renameTo(file);
    }
}
