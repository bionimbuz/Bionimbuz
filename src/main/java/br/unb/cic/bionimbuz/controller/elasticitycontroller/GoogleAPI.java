/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.controller.elasticitycontroller;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.AttachedDisk;
import com.google.api.services.compute.model.AttachedDiskInitializeParams;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.ServiceAccount;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilherme
 */
public class GoogleAPI implements ProvidersAPI {

    private static final String APPLICATION_NAME = "BioNimbuZ";

    /**
     * Set PROJECT_ID to your Project ID from the Overview pane in the
     * Developers console.
     */
    private static final String PROJECT_ID = "bionimbuz-150212";
            //"bustling-cosmos-151913";

    /**
     * Set Compute Engine zone
     */
    private static final String ZONE_NAME = "us-central1-f";

    /**
     * Set the name of the sample VM instance to be created.
     */
    private static final String SAMPLE_INSTANCE_NAME ="my-sample-instance";
            //"my-sample-instance2";

    /**
     * Set the path of the OS image for the sample VM instance to be created.
     */
    private static final String SOURCE_IMAGE_PREFIX = "https://www.googleapis.com/compute/v1/projects/";
    private static final String SOURCE_IMAGE_PATH = "debian-cloud/global/images/debian-7-wheezy-v20150710";
//    private static final String SOURCE_IMAGE_PATH = "bustling-cosmos-151913/global/images/bionimbuz";

    //private static final String TYPE = "n1-standard-1";
    /**
     * Set the Network configuration values of the sample VM instance to be
     * created.
     */
    private static final String NETWORK_INTERFACE_CONFIG = "ONE_TO_ONE_NAT";
    private static final String NETWORK_ACCESS_CONFIG = "External NAT";

    /**
     * Set the time out limit for operation calls to the Compute Engine API.
     */
    private static final long OPERATION_TIMEOUT_MILLIS = 60 * 1000;

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport httpTransport;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final String authpath = System.getProperty("user.home") + "/BionimbuzClient/target/BionimbuzClient-0.0.1-SNAPSHOT/resources/apiCredentials/GoogleCredentials.json";
    private String ipInstance;

    @Override
    public void setup() {

    }

    //@Override
    public void createinstance(String type, String instanceName) throws IOException {
        System.out.println("================== Setup ==================");
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Authenticate using Google Application Default Credentials.
            //GoogleCredential credential = GoogleCredential.getApplicationDefault();
            GoogleCredential credential;

            //InputStream auth = new ByteArrayInputStream(authpath.getBytes(StandardCharsets.UTF_8));
            InputStream is = null;
            is = new FileInputStream(authpath);

            credential = GoogleCredential.fromStream(is, httpTransport, JSON_FACTORY);

            if (credential.createScopedRequired()) {
                List<String> scopes = new ArrayList();
                // Set Google Clo  ud Storage scope to Full Control.
                scopes.add(ComputeScopes.DEVSTORAGE_FULL_CONTROL);
                // Set Google Compute Engine scope to Read-write.
                scopes.add(ComputeScopes.COMPUTE);
                credential = credential.createScoped(scopes);
            }

            // Create Compute Engine object for listing instances.
            Compute compute = new Compute.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            
            System.out.println("================== Starting New Instance ==================");

            // Create VM Instance object with the required properties.
            com.google.api.services.compute.model.Instance instance = new com.google.api.services.compute.model.Instance();
            instance.setName(instanceName);
            instance.setMachineType("https://www.googleapis.com/compute/v1/projects/" + PROJECT_ID
                    + "/zones/" + ZONE_NAME + "/machineTypes/" + type);

            // Add Network Interface to be used by VM Instance.
            NetworkInterface ifc = new NetworkInterface();
            ifc.setNetwork("https://www.googleapis.com/compute/v1/projects/" + PROJECT_ID + "/global/networks/default");
            List<AccessConfig> configs = new ArrayList();
            AccessConfig config = new AccessConfig();
            config.setType(NETWORK_INTERFACE_CONFIG);
            config.setName(NETWORK_ACCESS_CONFIG);
            configs.add(config);
            ifc.setAccessConfigs(configs);
            instance.setNetworkInterfaces(Collections.singletonList(ifc));
            //get Internal ip, do a method that set it
            
            // Add attached Persistent Disk to be used by VM Instance.
            AttachedDisk disk = new AttachedDisk();
            disk.setBoot(true);
            disk.setAutoDelete(true);
            disk.setType("PERSISTENT");
            AttachedDiskInitializeParams params = new AttachedDiskInitializeParams();
            // Assign the Persistent Disk the same name as the VM Instance.
            params.setDiskName(instanceName);
            // Specify the source operating system machine image to be used by the VM Instance.
            params.setSourceImage(SOURCE_IMAGE_PREFIX + SOURCE_IMAGE_PATH);
            // Specify the disk type as Standard Persistent Disk
            params.setDiskType("https://www.googleapis.com/compute/v1/projects/" + PROJECT_ID + "/zones/"
                    + ZONE_NAME + "/diskTypes/pd-standard");
            disk.setInitializeParams(params);
            instance.setDisks(Collections.singletonList(disk));

            // Initialize the service account to be used by the VM Instance and set the API access scopes.
            ServiceAccount account = new ServiceAccount();
            account.setEmail("default");
            List<String> scopes = new ArrayList();
            scopes.add("https://www.googleapis.com/auth/devstorage.full_control");
            scopes.add("https://www.googleapis.com/auth/compute");
            account.setScopes(scopes);
            instance.setServiceAccounts(Collections.singletonList(account));

            // Optional - Add a startup script to be used by the VM Instance.
            Metadata meta = new Metadata();
            Metadata.Items item = new Metadata.Items();
            item.setKey("startup-script-url");
            // If you put a script called "vm-startup.sh" in this Google Cloud Storage bucket, it will execute on VM startup.
            // This assumes you've created a bucket named the same as your PROJECT_ID
            // For info on creating buckets see: https://cloud.google.com/storage/docs/cloud-console#_creatingbuckets
            item.setValue("gs://" + PROJECT_ID + "/vm-startup.sh");
            meta.setItems(Collections.singletonList(item));
            instance.setMetadata(meta);

            System.out.println(instance.toPrettyString());
            Compute.Instances.Insert insert = compute.instances().insert(PROJECT_ID, ZONE_NAME, instance);
            insert.execute();
            
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("OK");

            String instanceCreatedName= instance.getName();
            System.out.println(instanceCreatedName);
            Compute.Instances.Get get = compute.instances().get(PROJECT_ID, ZONE_NAME, instanceCreatedName);
            Instance instanceCreated = get.execute();
            setIpInstance(instanceCreated.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP()); 
            
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(GoogleAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getIpInstance() {
        return ipInstance;
    }

    public void setIpInstance(String ipInstance) {
        this.ipInstance = ipInstance;
    }

    @Override
    public void createinstance(String type) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
