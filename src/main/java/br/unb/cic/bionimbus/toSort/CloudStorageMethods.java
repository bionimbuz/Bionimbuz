/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import java.io.IOException;
import java.io.*;
import java.util.*;

/**
 *
 * @author Lucas
 */
public class CloudStorageMethods {

    public static String JSONPATH = "/home/baile/TESTE-BIO/cred.json";

    public enum StorageProvider {

        AMAZON,
        GOOGLE
    }

    public static void ExecCommand (String command) {
        
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);
            System.out.println("\nRunning command: " + command);
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null)
                System.out.println("[command] " + line);
            int exitVal = proc.waitFor();
            System.out.println("[command] Process exitValue: " + exitVal);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static void StorageAuth(StorageProvider sp) {
        switch (sp) {
            case AMAZON: {
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gcloud auth activate-service-account --key-file=" + JSONPATH;
                ExecCommand(command);
                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
            }
        }
    }

    public static void StorageUploadFile(StorageProvider sp, String bucketName, String localPath, String bucketPath, String fileName) {
        switch (sp) {
            case AMAZON: {
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gsutil cp " + localPath + "/" + fileName + " gs://" + bucketName + bucketPath + "/" + fileName;
                ExecCommand(command);
                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
            }
        }
    }

    public static void StorageDownloadFile(StorageProvider sp, String bucketName, String localPath, String bucketPath, String fileName) {
        switch (sp) {
            case AMAZON: {
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gsutil cp gs://" + bucketName + bucketPath + "/" + fileName + " " + localPath + "/" + fileName;
                ExecCommand(command);
                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
            }
        }
    }

    public static void StorageMount(StorageProvider sp, String bucketName, String mountPoint) {
        String command = "/bin/mkdir " + mountPoint;
        ExecCommand(command);

        switch (sp) {
            case AMAZON: {
                command = "/usr/bin/s3fs " + bucketName + " " + mountPoint;
                ExecCommand(command);
                break;
            }
            case GOOGLE: {
                command = "/usr/bin/gcsfuse --key-file=" + JSONPATH + " " + bucketName + " " + mountPoint;
                ExecCommand(command);
                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
            }
        }
    }

    public static void StorageUmount(String mountPoint) {
        String command = "/bin/fusermount -u " + mountPoint;
        ExecCommand(command);

        command = "/bin/rm -r " + mountPoint;
        ExecCommand(command);
    }

    public static void main(String[] args) {
        System.out.println("Iniciando Teste.\n\n");
        
        StorageAuth(StorageProvider.GOOGLE);
        
        StorageUploadFile(StorageProvider.GOOGLE, "bionimbuzteste", "/home/baile/TESTE-BIO", "", "mclovin.png");   
        
        System.out.println("\n\nTeste Realizado.");
    }
}
