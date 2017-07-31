/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author rafaelsardenberg
 */

public class Integrity {

    /**
     * @param fileHash1
     * @param fileHash2
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static Boolean verifyHashes(String fileHash1, String fileHash2) throws NoSuchAlgorithmException, IOException {
        //Compara os hashes
        if (fileHash1 == null ? fileHash2 == null : fileHash1.equals(fileHash2)) {            
            return true;
        } else {            
            return false;
        }
    }
}
