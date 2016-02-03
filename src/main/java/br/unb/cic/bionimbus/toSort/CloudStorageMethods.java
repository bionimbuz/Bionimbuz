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
import java.util.Vector;

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

    public static boolean StorageAuth(StorageProvider sp) {

        switch (sp) {
            case AMAZON: {
                try {

                    byte[] encoded = Files.readAllBytes(Paths.get(AMAZONKEYPATH));
                    String fileContent = new String(encoded, Charset.defaultCharset());
                    System.out.println("AuthString: " + fileContent);
                    String accessKeyID, accessKey;
                    int delimiter = fileContent.indexOf(':');
                    accessKeyID = fileContent.substring(0, delimiter);
                    accessKey = fileContent.substring(delimiter + 1);
                    AWSCredentials credentials = new BasicAWSCredentials(accessKeyID, accessKey);
                    s3client = new AmazonS3Client(credentials);
                } catch (Throwable t) {
                    t.printStackTrace();
                    return false;
                }
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gcloud auth activate-service-account --key-file=" + JSONPATH;
                boolean status = ExecCommand(command);

                if (status == false) {
                    return false;
                }

                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
                return false;
            }
        }
        return true;
    }

    public static boolean StorageUploadFile(BioBucket bucket, String bucketPath, String localPath, String fileName) {

        switch (bucket.getProvider()) {
            case AMAZON: {
                try {

                    File file = new File(localPath + "/" + fileName);
                    s3client.setEndpoint(bucket.getEndPoint());
                    s3client.putObject(new PutObjectRequest(bucket.getName() + bucketPath, fileName, file));

                } catch (Throwable t) {
                    t.printStackTrace();
                    return false;
                }
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gsutil cp " + localPath + "/" + fileName + " gs://" + bucket.getName() + bucketPath + "/" + fileName;
                boolean status = ExecCommand(command);

                if (status == false) {
                    return false;
                }

                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
                return false;
            }
        }
        return true;
    }

    public static boolean StorageDownloadFile(BioBucket bucket, String bucketPath, String localPath, String fileName) {

        switch (bucket.getProvider()) {
            case AMAZON: {
                try {
                    s3client.setEndpoint(bucket.getEndPoint());
                    S3Object object = s3client.getObject(new GetObjectRequest(bucket.getName() + bucketPath, fileName));
                    InputStream objectData = object.getObjectContent();
                    Files.copy(objectData, Paths.get(localPath + "/" + fileName));
                    objectData.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                    return false;
                }
                break;
            }
            case GOOGLE: {
                String command = "/home/baile/google-cloud-sdk/bin/gsutil cp gs://" + bucket.getName() + bucketPath + "/" + fileName + " " + localPath + "/" + fileName;
                boolean status = ExecCommand(command);

                if (status == false) {
                    return false;
                }

                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
                return false;
            }
        }
        return true;
    }

    public static boolean StorageMount(BioBucket bucket) {

        String command = "/bin/mkdir " + bucket.getMountPoint();
        boolean status = ExecCommand(command);

        if (status == false) {
            return false;
        }

        switch (bucket.getProvider()) {
            case AMAZON: {
                command = "/usr/bin/s3fs " + bucket.getName() + " " + bucket.getMountPoint();
                status = ExecCommand(command);

                if (status == false) {
                    return false;
                }

                break;
            }
            case GOOGLE: {
                command = "/usr/bin/gcsfuse --key-file=" + JSONPATH + " " + bucket.getName() + " " + bucket.getMountPoint();
                status = ExecCommand(command);

                if (status == false) {
                    return false;
                }

                break;
            }
            default: {
                System.out.println("Provedor incorreto!");
                return false;
            }
        }
        return true;
    }

    public static boolean StorageUmount(BioBucket bucket) {

        String command = "/bin/fusermount -u " + bucket.getMountPoint();
        boolean status = ExecCommand(command);

        if (status == false) {
            return false;
        }

        command = "/bin/rm -r " + bucket.getMountPoint();
        status = ExecCommand(command);

        if (status == false) {
            return false;
        }

        return true;
    }

    public static boolean CheckStorageBandwith(BioBucket bucket) {

        //Upload
        String command = "/bin/dd if=/dev/zero of=" + bucket.getMountPoint() + "/testfile bs=30M count=1 oflag=dsync";

        try {

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);
            System.out.println("\nRunning command: " + command);
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

            Vector<String> output = new Vector<String>();

            while ((line = br.readLine()) != null) {
                output.add(line);
                System.out.println("[command] " + line);
            }

            int exitVal = proc.waitFor();
            System.out.println("[command] Process exitValue: " + exitVal);

            if (exitVal != 0) {
                return false;
            }

            int pos1, pos2;

            pos1 = output.lastElement().indexOf(" copied, ");
            pos1 += 9;

            pos2 = output.lastElement().indexOf(" s, ");

            String aux;

            aux = output.lastElement().substring(pos1, pos2);
            aux = aux.replace(',', '.');

            float value = Float.parseFloat(aux);

            bucket.setBandwith((30 * 1024 * 1024) / value);

            command = "/bin/rm " + bucket.getMountPoint() + "/testfile";
            boolean status = ExecCommand(command);

            if (status == false) {
                return false;
            }

        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean CheckStorageLatency(BioBucket bucket) {

        float latency = 0;
        int i;

        for (i = 0; i < 10; i++) {
            //Upload
            String command = "/bin/dd if=/dev/zero of=" + bucket.getMountPoint() + "/pingfile bs=64 count=1 oflag=dsync";

            try {

                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(command);
                System.out.println("\nRunning command: " + command);
                InputStream stderr = proc.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;

                Vector<String> output = new Vector<String>();

                while ((line = br.readLine()) != null) {
                    output.add(line);
                    System.out.println("[command] " + line);
                }

                int exitVal = proc.waitFor();
                System.out.println("[command] Process exitValue: " + exitVal);

                if (exitVal != 0) {
                    return false;
                }
                int pos1, pos2;

                pos1 = output.lastElement().indexOf(" copied, ");
                pos1 += 9;

                pos2 = output.lastElement().indexOf(" s, ");

                String aux;

                aux = output.lastElement().substring(pos1, pos2);
                aux = aux.replace(',', '.');

                float value = Float.parseFloat(aux);

                latency += value;
                System.out.println("[current] Latency: " + (latency / (i + 1)));

                command = "/bin/rm " + bucket.getMountPoint() + "/pingfile";

                boolean status = ExecCommand(command);

                if (status == false) {
                    return false;
                }

            } catch (Throwable t) {
                t.printStackTrace();
                return false;
            }
        }

        bucket.setLatency(latency / (i + 1));

        return true;

    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Iniciando Teste.\n\n");

        BioBucket gBucket = new BioBucket(StorageProvider.GOOGLE, "bionimbuzteste", "/home/baile/TESTE-BIO/mybucket");

        BioBucket aBucket = new BioBucket(StorageProvider.AMAZON, "testbionimbuz", "/home/baile/TESTE-BIO/mybucket");
        aBucket.setEndPoint("s3-sa-east-1.amazonaws.com");

        StorageAuth(aBucket.getProvider());

        StorageMount(aBucket);

        for (int i = 0; i < 5; i++) {
            CheckStorageBandwith(aBucket);
            //CheckStorageLatency(gBucket);

            System.out.println("\tBandwith: " + aBucket.getBandwith());
            //System.out.println("\tLatency: " + gBucket.getLatency());
            Thread.sleep(5000);
        }

        StorageUmount(aBucket);

        //StorageDownloadFile(aBucket, "/test", "/home/baile/TESTE-BIO/testee", "mclovin.png");
        //StorageUploadFile(gBucket, "/test", "/home/baile/TESTE-BIO/testee", "mclovin.png");
        System.out.println("\n\nTeste Realizado.");
    }
}