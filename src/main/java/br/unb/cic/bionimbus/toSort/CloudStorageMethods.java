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

    protected static String authFolder;
    protected static String gcloudFolder;
    protected static AmazonS3 s3client;
    
    protected static String myId;
    
    //Config
    protected int LATENCY_CHECKS = 5;
    
    public static void ExecCommand(String command) throws Exception {

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(command);
        System.out.println("\nRunning command: " + command);
        InputStream stderr = proc.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line;

        while ((line = br.readLine()) != null) {
            System.out.println("[command] " + line);
        }

        int exitVal = proc.waitFor();
        System.out.println("[command] Process exitValue: " + exitVal);

        if (exitVal != 0) {
            throw new Exception ("Error in command: " + command);
        }
    }

    public static void setAuthFolder(String authFolder) {
        CloudStorageMethods.authFolder = authFolder;
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
    
    public abstract void StorageAuth(CloudStorageMethodsV1.StorageProvider sp) throws Exception;
    public abstract void StorageUploadFile(BioBucket bucket, String bucketPath, String localPath, String fileName) throws Exception;
    public abstract void StorageDownloadFile(BioBucket bucket, String bucketPath, String localPath, String fileName) throws Exception;
    public abstract void StorageMount(BioBucket bucket) throws Exception;
    public abstract void StorageUmount(BioBucket bucket) throws Exception;
    public abstract void CheckStorageBandwith(BioBucket bucket) throws Exception;
    public abstract void CheckStorageLatency(BioBucket bucket) throws Exception;
    public abstract void DeleteFile(BioBucket bucket, String fileName) throws Exception;
}
