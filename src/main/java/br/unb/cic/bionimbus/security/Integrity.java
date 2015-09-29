/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author rafaelsardenberg
 */
public class Integrity {
    public Boolean verifyFile(String filePeerHash, String fileUploadedHash) throws NoSuchAlgorithmException, IOException {
        //Compara os hashes
        if (filePeerHash == null ? fileUploadedHash == null : filePeerHash.equals(fileUploadedHash)) {            
            return true;
        } else {            
            return false;
        }
    }
}
