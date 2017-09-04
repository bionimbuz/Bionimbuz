/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.storage.bucket.methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
// import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.inject.Singleton;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.services.storage.bucket.BioBucket;
import br.unb.cic.bionimbuz.services.storage.bucket.CloudStorageMethods;

/**
 *
 * @author Lucas
 */

@Singleton
public class CloudMethodsAmazonGoogle extends CloudStorageMethods {
    
    @Override
    public void StorageAuth(StorageProvider sp) throws Exception {
        
        switch (sp) {
            
            case AMAZON: {
                // InputStream is = null;
                // is = new FileInputStream(keyAmazon);
                // PropertiesCredentials credentials = new PropertiesCredentials(is);
                final byte[] encoded = Files.readAllBytes(Paths.get(keyAmazon));
                final String fileContent = new String(encoded, Charset.defaultCharset());
                System.out.println("AuthString: " + fileContent);
                String accessKeyID, accessKey;
                final int delimiter = fileContent.indexOf(':');
                accessKeyID = fileContent.substring(0, delimiter);
                accessKey = fileContent.substring(delimiter + 1);
                final AWSCredentials credentials = new BasicAWSCredentials(accessKeyID, accessKey);
                s3client = new AmazonS3Client(credentials);
                
                break;
            }
            case GOOGLE: {
                
                final String command = gcloudFolder + "gcloud auth activate-service-account --key-file=" + keyGoogle;
                ExecCommand(command);
                
                break;
            }
            default: {
                throw new Exception("Provedor incorreto!");
            }
        }
    }
    
    @Override
    public void StorageUploadFile(BioBucket bucket, String bucketPath, String localPath, String fileName) throws Exception {
        
        switch (bucket.getProvider()) {
            
            case AMAZON: {
                
                final File file = new File(localPath + fileName);
                s3client.setEndpoint(bucket.getEndPoint());
                s3client.putObject(new PutObjectRequest(bucket.getName(), bucketPath.substring(1) + fileName, file));
                
                break;
            }
            case GOOGLE: {
                
                final String command = gcloudFolder + "gsutil cp " + localPath + fileName + " gs://" + bucket.getName() + bucketPath + fileName;
                ExecCommand(command);
                
                break;
            }
            default: {
                throw new Exception("Provedor incorreto!");
            }
        }
    }
    
    @Override
    public void StorageDownloadFile(BioBucket bucket, String bucketPath, String localPath, String fileName) throws Exception {
        
        switch (bucket.getProvider()) {
            case AMAZON: {
                
                s3client.setEndpoint(bucket.getEndPoint());
                final S3Object object = s3client.getObject(new GetObjectRequest(bucket.getName(), bucketPath.substring(1) + fileName));
                final InputStream objectData = object.getObjectContent();
                Files.copy(objectData, Paths.get(localPath + fileName));
                objectData.close();
                
                break;
            }
            case GOOGLE: {
                
                final String command = gcloudFolder + "gsutil cp gs://" + bucket.getName() + bucketPath + fileName + " " + localPath + fileName;
                ExecCommand(command);
                
                break;
            }
            default: {
                throw new Exception("Provedor incorreto!");
            }
        }
    }
    
    @Override
    public void StorageMount(BioBucket bucket) throws Exception {
        
        final File mountFolder = new File(bucket.getMountPoint());
        
        if (!mountFolder.exists()) {
            mountFolder.mkdirs();
        }
        
        String command;
        
        switch (bucket.getProvider()) {
            
            case AMAZON: {
                
                command = "s3fs -o umask=022 " + bucket.getName() + " " + bucket.getMountPoint() + " -o passwd_file=" + keyAmazon;
                ExecCommand(command);
                
                break;
            }
            case GOOGLE: {
                
                command = "gcsfuse --key-file=" + keyGoogle + " " + bucket.getName() + " " + bucket.getMountPoint();
                // command = "/usr/bin/gcsfuse " + bucket.getName() + " " + bucket.getMountPoint();
                ExecCommand(command);
                
                break;
            }
            default: {
                throw new Exception("Incorrect provider!");
            }
        }
        
        bucket.setMounted(true);
    }
    
    @Override
    public void StorageUmount(BioBucket bucket) throws Exception {
        
        final String command = "/bin/fusermount -u " + bucket.getMountPoint();
        ExecCommand(command);
        
        FileUtils.forceDelete(new File(bucket.getMountPoint()));
        
        bucket.setMounted(false);
    }
    
    @Override
    public void CheckStorageBandwith(BioBucket bucket) throws Exception {
        
        if (!bucket.isMounted()) {
            throw new Exception("Cant check bandwith! Bucket not mounted: " + bucket.getName());
        }
        
        this.CheckStorageUpBandwith(bucket);
        this.CheckStorageDlBandwith(bucket);
        
        final File faux = new File(bucket.getMountPoint() + "/testfile-" + myId);
        faux.delete();
        final File faux2 = new File("/tmp/testfile");
        faux2.delete();
    }
    
    private void CheckStorageUpBandwith(BioBucket bucket) throws Exception {
        
        // Upload
        final String command = "/bin/dd if=/dev/zero of=" + bucket.getMountPoint() + "/testfile-" + myId + " bs=30M count=1 iflag=nocache oflag=nocache";
        
        final Runtime rt = Runtime.getRuntime();
        final Process proc = rt.exec(command);
        // System.out.println("\nRunning command: " + command);
        final InputStream stderr = proc.getErrorStream();
        final InputStreamReader isr = new InputStreamReader(stderr);
        final BufferedReader br = new BufferedReader(isr);
        String line;
        
        final List<String> output = new ArrayList<>();
        
        while ((line = br.readLine()) != null) {
            output.add(line);
            // System.out.println("[command] " + line);
        }
        
        final int exitVal = proc.waitFor();
        // System.out.println("[command] Process exitValue: " + exitVal);
        
        if (exitVal != 0) {
            throw new Exception("Error in command: " + command);
        }
        
        int pos1, pos2;
        
        pos1 = output.get(output.size() - 1).indexOf(" copied, ");
        pos1 += 9;
        
        pos2 = output.get(output.size() - 1).indexOf(" s, ");
        
        String aux;
        
        aux = output.get(output.size() - 1).substring(pos1, pos2);
        aux = aux.replace(',', '.');
        
        final float value = Float.parseFloat(aux);
        
        bucket.setUpBandwith(31 * 1024 * 1024 / value);
    }
    
    private void CheckStorageDlBandwith(BioBucket bucket) throws Exception {
        
        // Download
        final String command = "/bin/dd if=" + bucket.getMountPoint() + "/testfile-" + myId + " of=/tmp/testfile bs=30M count=1 iflag=nocache oflag=nocache";
        
        final Runtime rt = Runtime.getRuntime();
        final Process proc = rt.exec(command);
        // System.out.println("\nRunning command: " + command);
        final InputStream stderr = proc.getErrorStream();
        final InputStreamReader isr = new InputStreamReader(stderr);
        final BufferedReader br = new BufferedReader(isr);
        String line;
        
        final List<String> output = new ArrayList<>();
        
        while ((line = br.readLine()) != null) {
            output.add(line);
            // System.out.println("[command] " + line);
        }
        
        final int exitVal = proc.waitFor();
        // System.out.println("[command] Process exitValue: " + exitVal);
        
        if (exitVal != 0) {
            throw new Exception("Error in command: " + command);
        }
        
        int pos1, pos2;
        
        pos1 = output.get(output.size() - 1).indexOf(" copied, ");
        pos1 += 9;
        
        pos2 = output.get(output.size() - 1).indexOf(" s, ");
        
        String aux;
        
        aux = output.get(output.size() - 1).substring(pos1, pos2);
        aux = aux.replace(',', '.');
        
        final float value = Float.parseFloat(aux);
        
        bucket.setDlBandwith(31 * 1024 * 1024 / value);
    }
    
    @Override
    public void CheckStorageLatency(BioBucket bucket) throws Exception {
        
        if (!bucket.isMounted()) {
            throw new Exception("Cant check latency! Bucket not mounted: " + bucket.getName());
        }
        
        float latency = 0;
        int i;
        
        for (i = 0; i < this.LATENCY_CHECKS; i++) {
            // Upload
            final String command = "/bin/dd if=/dev/zero of=" + bucket.getMountPoint() + "/pingfile-" + myId + " bs=64 count=1 oflag=dsync";
            
            final Runtime rt = Runtime.getRuntime();
            final Process proc = rt.exec(command);
            // System.out.println("\nRunning command: " + command);
            final InputStream stderr = proc.getErrorStream();
            final InputStreamReader isr = new InputStreamReader(stderr);
            
            final List<String> output = new ArrayList<>();
            try (
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.add(line);
                    // System.out.println("[command] " + line);
                }
            }
            
            final int exitVal = proc.waitFor();
            // System.out.println("[command] Process exitValue: " + exitVal);
            
            if (exitVal != 0) {
                throw new Exception("Error in command: " + command);
            }
            int pos1, pos2;
            
            pos1 = output.get(output.size() - 1).indexOf(" copied, ");
            pos1 += 9;
            
            pos2 = output.get(output.size() - 1).indexOf(" s, ");
            
            String aux;
            
            aux = output.get(output.size() - 1).substring(pos1, pos2);
            aux = aux.replace(',', '.');
            
            final float value = Float.parseFloat(aux);
            
            latency += value;
            // System.out.println("[current] Latency: " + (latency / (i + 1)));
            
            final File faux = new File(bucket.getMountPoint() + "/pingfile-" + myId);
            faux.delete();
        }
        
        bucket.setLatency(latency / (i + 1));
    }
    
    @Override
    public void DeleteFile(BioBucket bucket, String fileName) throws Exception {
        
        if (!bucket.isMounted()) {
            throw new Exception("Cant delete file " + fileName + "! Bucket not mounted: " + bucket.getName());
        }
        
        final String command = "/bin/rm " + bucket.getMountPoint() + "/" + BioNimbusConfig.get().getDataFolder() + fileName;
        ExecCommand(command);
    }
}
