/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import java.io.File;
//import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.FileOutputStream;
import java.io.BufferedReader;
//import java.io.OutputStream;
//import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.AmazonClientException;
//import com.amazonaws.AmazonServiceException;
//import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;



/**
 *
 * @author Lucas
 */
public class CloudStorageMethods {

    public enum StorageProvider {

        AMAZON,
        GOOGLE
    }
    
    public static String JSONPATH = "/home/baile/Dropbox/cred.json";
    public static String AMAZONKEYPATH = "/home/baile/Dropbox/accesskey.txt";
    
    private static AmazonS3 s3client;

    public static boolean ExecCommand (String command) {
        
        boolean status;
        
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
            status = true;
            
        } catch (Throwable t) {
            t.printStackTrace();
            status = false;
        }
        return status;
    }
    
    public static boolean StorageAuth(StorageProvider sp) {
        
        boolean status;
        
        switch (sp) {
            case AMAZON: {
                try {
                    
                    byte[] encoded = Files.readAllBytes(Paths.get(AMAZONKEYPATH));
                    String fileContent = new String(encoded, Charset.defaultCharset());
                    System.out.println("AuthString: " + fileContent);
                    String accessKeyID, accessKey;
                    int delimiter = fileContent.indexOf(':');
                    accessKeyID = fileContent.substring(0,delimiter);
                    accessKey = fileContent.substring(delimiter+1);
                    AWSCredentials credentials = new BasicAWSCredentials(accessKeyID, accessKey);
                    s3client = new AmazonS3Client(credentials);
                    status = true;
                } catch (Throwable t) {
                    t.printStackTrace();
                    status = false;
                }
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gcloud auth activate-service-account --key-file=" + JSONPATH;
                status = ExecCommand(command);
                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
                status = false;
            }
        }
        return status;
    }

    public static boolean StorageUploadFile(StorageProvider sp, String bucketName, String localPath, String bucketPath, String fileName) {
        
        boolean status;
        
        switch (sp) {
            case AMAZON: {
                try {
                    
                    File file = new File (localPath + "/" + fileName);
                    s3client.putObject(new PutObjectRequest(bucketName+bucketPath, fileName , file));
                    status = true;
                } catch (Throwable t) {
                    t.printStackTrace();
                    status = false;
                }
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gsutil cp " + localPath + "/" + fileName + " gs://" + bucketName + bucketPath + "/" + fileName;
                status = ExecCommand(command);
                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
                status = false;
            }
        }
        return status;
    }

    public static boolean StorageDownloadFile(StorageProvider sp, String bucketName, String localPath, String bucketPath, String fileName) {
        
        boolean status;
        
        switch (sp) {
            case AMAZON: {
                try {
                    
                    S3Object object = s3client.getObject(new GetObjectRequest(bucketName+bucketPath, fileName));
                    InputStream objectData = object.getObjectContent();
                    Files.copy(objectData, Paths.get(localPath + "/" + fileName));
                    objectData.close();
                    status = true;
                } catch (Throwable t) {
                    t.printStackTrace();
                    status = false;
                }
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gsutil cp gs://" + bucketName + bucketPath + "/" + fileName + " " + localPath + "/" + fileName;
                status = ExecCommand(command);
                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
                status = false;
            }
        }
        return status;
    }

    public static boolean StorageMount(StorageProvider sp, String bucketName, String mountPoint) {
        
        boolean status;
        
        String command = "/bin/mkdir " + mountPoint;
        status = ExecCommand(command);
        
        if (status == false)
            return status;

        switch (sp) {
            case AMAZON: {
                command = "/usr/bin/s3fs " + bucketName + " " + mountPoint;
                status = ExecCommand(command);
                break;
            }
            case GOOGLE: {
                command = "/usr/bin/gcsfuse --key-file=" + JSONPATH + " " + bucketName + " " + mountPoint;
                status = ExecCommand(command);
                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
                status = false;
            }
        }
        return status;
    }

    public static boolean StorageUmount(String mountPoint) {
        
        boolean status;
        
        String command = "/bin/fusermount -u " + mountPoint;
        status = ExecCommand(command);

        if (status == false)
            return status;
        
        command = "/bin/rm -r " + mountPoint;
        status = ExecCommand(command);
        
        return status;
    }

    public static void main(String[] args) {
        System.out.println("Iniciando Teste.\n\n");
        
        StorageAuth(StorageProvider.GOOGLE);
        
        StorageDownloadFile(StorageProvider.GOOGLE, "bionimbuzteste", "/home/baile/TESTE-BIO", "/test", "mclovin.png");   
        
        //StorageUmount("/home/baile/TESTE-BIO/mybucket");
        
        System.out.println("\n\nTeste Realizado.");
    }
}
