/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.security;

import java.io.FileInputStream;
import java.io.IOException;
import org.bouncycastle.jcajce.provider.digest.SHA3;

/**
 *
 * @author rafaelsardenberg
 */
public class Hash {
    
    public static String calculateSha3(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] dataBytes = new byte[256];

        int nread = 0; 

        SHA3.DigestSHA3 md = new SHA3.DigestSHA3(256);
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        }
        
        byte[] mdbytes = md.digest();

        //Convert the byte to hex format
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
