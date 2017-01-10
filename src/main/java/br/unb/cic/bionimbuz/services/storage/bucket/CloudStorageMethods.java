/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.storage.bucket;

import br.unb.cic.bionimbuz.services.storage.bucket.methods.CloudMethodsAmazonGoogle;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.AmazonS3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lucas
 */

public abstract class CloudStorageMethods {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudStorageMethods.class);
    
    public enum StorageProvider {

        AMAZON,
        GOOGLE
    }

    protected static String keyGoogle;
    protected static String keyAmazon;
    protected static String gcloudFolder;
    protected static AmazonS3 s3client;
    
    protected static String myId;
    
    //Config
    protected int LATENCY_CHECKS = 5;
    
    public static void ExecCommand(String command) throws Exception {

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(command);
        LOGGER.debug("\nRunning command: " + command);
        InputStream stderr = proc.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line;

        while ((line = br.readLine()) != null) {
            LOGGER.debug("[command] " + line);
        }

        int exitVal = proc.waitFor();
        LOGGER.debug("[command] Process exitValue: " + exitVal);

        if (exitVal != 0) {
            throw new Exception ("Error in command: " + command);
        }
    }

    public static void setKeyGoogle(String keyGoogle) {
        CloudStorageMethods.keyGoogle = keyGoogle;
    }
    public static void setKeyAmazon(String keyAmazon) {
        CloudStorageMethods.keyAmazon = keyAmazon;
    }

    public static void setGcloudFolder(String gcloudFolder) {
        CloudStorageMethods.gcloudFolder = gcloudFolder;
    }

    public static String getMyId() {
        return myId;
    }

    public static void setMyId(String myId) {
        CloudStorageMethods.myId = myId;
    }
    
    public static AmazonS3 getS3client() {
        return s3client;
    }
    
    public abstract void StorageAuth(CloudMethodsAmazonGoogle.StorageProvider sp) throws Exception;
    public abstract void StorageUploadFile(BioBucket bucket, String bucketPath, String localPath, String fileName) throws Exception;
    public abstract void StorageDownloadFile(BioBucket bucket, String bucketPath, String localPath, String fileName) throws Exception;
    public abstract void StorageMount(BioBucket bucket) throws Exception;
    public abstract void StorageUmount(BioBucket bucket) throws Exception;
    public abstract void CheckStorageBandwith(BioBucket bucket) throws Exception;
    public abstract void CheckStorageLatency(BioBucket bucket) throws Exception;
    public abstract void DeleteFile(BioBucket bucket, String fileName) throws Exception;
}
