/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.jboss.resteasy.util.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbuz.utils.RuntimeUtil;
import br.unb.cic.bionimbuz.utils.RuntimeUtil.Command;

/**
 *
 * @author rafaelsardenberg
 */
public class HashUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HashUtil.class);
    private static final String SHA256SUM = "sha256sum";
    private static final String SPACE_STRING = " ";
    private static final int KEY_SIZE = 256;
    
    // --------------------------------------------------------------
    // Constructors.
    // --------------------------------------------------------------
    private HashUtil() {
        super();
    }
    
    public static String computeNativeSHA3(final String filePath) throws IOException, InterruptedException {
        final long now = System.currentTimeMillis();
        final String response = RuntimeUtil.runCommand(new Command(SHA256SUM, filePath));
        LOGGER.info(String.format("Native HASH time: %d", System.currentTimeMillis() - now));
        return response.split(SPACE_STRING)[0];
    }
    
    /**
     * @deprecated This method results in erratic behavior or deadlock (with large files > 2GB).
     * @param filePath
     *            indicating the file path
     * @throws IOException
     * @see br.unb.cic.bionimbus.security#computeNativeSHA3(String)
     */
    @Deprecated
    public static String computeSHA3(final String filePath) throws IOException {
        try (
             FileInputStream fileInputStream = new FileInputStream(filePath);) {
            return computeSHA3(fileInputStream);
        }
    }
    
    /**
     * @deprecated This method results in erratic behavior or deadlock (with large files > 2GB).
     * @param inputStream
     *            indicating the file path
     * @throws IOException
     * @see br.unb.cic.bionimbus.security#computeNativeSHA3(String)
     */
    @Deprecated
    public static String computeSHA3(final InputStream inputStream) throws IOException {
        final long now = System.currentTimeMillis();
        final byte[] dataBytes = new byte[KEY_SIZE];
        final SHA3.DigestSHA3 digester = new SHA3.DigestSHA3(KEY_SIZE);
        int nread = 0;
        while ((nread = inputStream.read(dataBytes)) != -1) {
            digester.update(dataBytes, 0, nread);
        }
        final byte[] mdbytes = digester.digest();
        LOGGER.info(String.format("HASH time: %d", System.currentTimeMillis() - now));
        // Convert the byte to hex format
        return Hex.encodeHex(mdbytes);
    }
}
