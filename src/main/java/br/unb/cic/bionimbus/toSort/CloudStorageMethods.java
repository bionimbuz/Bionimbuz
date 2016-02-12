/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.AmazonS3;

/**
 *
 * @author Lucas
 */
public abstract class CloudStorageMethods {
    
    public enum StorageProvider {

        AMAZON,
        GOOGLE
    }

    protected static String JSONPATH = "/home/baile/Dropbox/cred.json";
    protected static String AMAZONKEYPATH = "/home/baile/Dropbox/accesskey.txt";
    protected static String GCLOUDPATH = "/home/baile/google-cloud-sdk/bin";

    protected static AmazonS3 s3client;
    
    public static boolean ExecCommand(String command) {

        try {

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);
            System.out.println("\nRunning command: " + command);
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

            while ((line = br.readLine()) != null) {
                System.out.println("[command] " + line);
            }

            int exitVal = proc.waitFor();
            System.out.println("[command] Process exitValue: " + exitVal);

            if (exitVal != 0) {
                return false;
            }

        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        return true;
    }
    
    public abstract boolean StorageAuth(CloudStorageMethodsV1.StorageProvider sp);
    public abstract boolean StorageUploadFile(BioBucket bucket, String bucketPath, String localPath, String fileName);
    public abstract boolean StorageDownloadFile(BioBucket bucket, String bucketPath, String localPath, String fileName);
    public abstract boolean StorageMount(BioBucket bucket);
    public abstract boolean StorageUmount(BioBucket bucket);
    public abstract boolean CheckStorageBandwith(BioBucket bucket);
    public abstract boolean CheckStorageLatency(BioBucket bucket);
}
