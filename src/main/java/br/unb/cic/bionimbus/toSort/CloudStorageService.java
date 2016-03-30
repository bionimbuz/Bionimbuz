/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbus.toSort.CloudStorageMethods.*;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import java.util.List;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lucas
 */

@Singleton
public class CloudStorageService extends AbstractBioService{

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudStorageService.class);

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("CloudStorageService-%d").build());
    
    private static String bucketsFolder;
    static private List<BioBucket> bucketList = new ArrayList<>();
    static private CloudStorageMethods methodsInstance;
    
    @Inject
    public CloudStorageService(final CloudMessageService cms) {
        
        Preconditions.checkNotNull(cms);
        
        this.cms = cms;
    }
    
    @Override
    public void run() {
        
        try {
            for (BioBucket aux : bucketList) {

                LOGGER.info("[CloudStorageService] Checking Latency for bucket " + aux.getName());
                methodsInstance.CheckStorageLatency(aux);
                LOGGER.info("[CloudStorageService] Latency for bucket " + aux.getName() + ": " + aux.getLatency());

                LOGGER.info("[CloudStorageService] Checking Bandwith for bucket " + aux.getName());
                methodsInstance.CheckStorageBandwith(aux);
                LOGGER.info("[CloudStorageService] Bandwith for bucket " + aux.getName() + ": " + aux.getBandwith());
            }
        } catch (Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }
    }
    
    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {
        this.config = config;
        this.listeners = listeners;
        if (listeners != null) {
            listeners.add(this);
        }
        
        //Criando pastas zookeeper para o módulo de armazenamento
        if (!cms.getZNodeExist(CuratorMessageService.Path.BUCKETS.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, CuratorMessageService.Path.BUCKETS.getFullPath(), null);
        }
        
        LOGGER.info("[CloudStorageService] Starting");        
        
        //Instance methods object
        methodsInstance = new CloudStorageMethodsV1();
        
        //Getting parameters from config file
        bucketsFolder = config.getBucketsFolder();
        methodsInstance.setAuthFolder(config.getBucketsAuthFolder());
        methodsInstance.setGcloudFolder(config.getGcloudFolder());
        
        //Instance all buckets
        LOGGER.info("[CloudStorageService] Instancing Buckets");
        InstanceBuckets();
        
        try {
            //Authenticate providers
            LOGGER.info("[CloudStorageService] Authenticating Providers");
            for (StorageProvider aux : StorageProvider.values()) {
                methodsInstance.StorageAuth(aux);
            }

            //Mount all buckets
            LOGGER.info("[CloudStorageService] Mounting Buckets");
            for (BioBucket aux : bucketList) {
                methodsInstance.StorageMount(aux);
                LOGGER.info(""); //TODO testar o path se tá setando
                cms.setData(CuratorMessageService.Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            }
        } catch (Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }
        
        executorService.scheduleAtFixedRate(this, 0, 30, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
        
        try {
            for (BioBucket aux : bucketList) {
                methodsInstance.StorageUmount(aux);
            }
        } catch (Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }
        
        listeners.remove(this);
        executorService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub
    }

    @Override
    public void verifyPlugins() {
        
    }

    @Override
    public void event(WatchedEvent eventType) {
        
    }
    
    private void InstanceBuckets() {
        
        BioBucket aux;
        
//        //AMAZON
//        //Brazil
//        aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-br", (bucketsFolder + "bionimbuz-a-br"));
//        aux.setEndPoint("s3-sa-east-1.amazonaws.com");
//        bucketList.add(aux);
//        cms.createZNode(CreateMode.PERSISTENT, CuratorMessageService.Path.NODE_BUCKET.getFullPath(aux.getName()), null);
//        //US
//        aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-us", (bucketsFolder + "bionimbuz-a-us"));
//        bucketList.add(aux);
//        cms.createZNode(CreateMode.PERSISTENT, CuratorMessageService.Path.NODE_BUCKET.getFullPath(aux.getName()), null);
//        //EU
//        aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-eu", (bucketsFolder + "bionimbuz-a-eu"));
//        aux.setEndPoint("s3.eu-central-1.amazonaws.com");
//        bucketList.add(aux);
//        cms.createZNode(CreateMode.PERSISTENT, CuratorMessageService.Path.NODE_BUCKET.getFullPath(aux.getName()), null);
        
        //GOOGLE
        //US
        aux = new BioBucket(StorageProvider.GOOGLE, "bionimbuz-g-us", (bucketsFolder + "bionimbuz-g-us"));
        bucketList.add(aux);
        cms.createZNode(CreateMode.PERSISTENT, CuratorMessageService.Path.NODE_BUCKET.getFullPath(aux.getName()), null);
        //EU
        aux = new BioBucket(StorageProvider.GOOGLE, "bionimbuz-g-eu", (bucketsFolder + "bionimbuz-g-eu"));
        bucketList.add(aux);
        cms.createZNode(CreateMode.PERSISTENT, CuratorMessageService.Path.NODE_BUCKET.getFullPath(aux.getName()), null);
    }   
    
    private BioBucket getBucket (String name) {
        for (BioBucket aux : bucketList) {
            if (aux.getName().equals(name)) {
                return aux;
            }
        }
        return null;
    }
    
    public BioBucket findFile (FileInfo file) {
        
        List<BioBucket> buckets = new ArrayList<>();
        
        try {
            
            List<String> bucketsPaths = cms.getChildren(CuratorMessageService.Path.BUCKETS.getFullPath(), null);
                        
            for (String bucket_aux : bucketsPaths) {

                ObjectMapper mapper = new ObjectMapper();
                BioBucket bucket = mapper.readValue(cms.getData(bucket_aux, null), BioBucket.class);

                List<String> filesPaths = cms.getChildren(CuratorMessageService.Path.BUCKET_FILES.getFullPath(bucket.getName()), null);

                for (String file_aux : filesPaths) {
                    
                    FileInfo bucket_file = mapper.readValue(cms.getData(file_aux, null), FileInfo.class);
                    
                    if (bucket_file.getName() == file.getName()) {
                        
                        buckets.add(bucket);
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }  
        
        if (buckets.isEmpty())
        {
            LOGGER.error("[CloudStorageService] Exception: File not found: " + file.getName());
            return null;
        }
        
        //TODO use some kind of storage policy to chose best bucket
        return buckets.get(0);
    }
    
    public static boolean checkMode (BioBucket bucket) {
        
        //TODO testar valores
        if (bucket.getLatency() < 1) {
            if (bucket.getBandwith() > (50*1024*1024)) { //tresshold = 50 MB
                return true;
            }
        }
        
        return false;
    }
    
    public static void main(String[] args) {
        System.out.println("Iniciando Teste.\n\n");


        System.out.println("\n\nTeste Realizado.");
    }
}
