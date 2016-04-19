/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbus.toSort.CloudStorageMethods.*;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import java.util.List;

import com.google.inject.Singleton;
import java.io.File;
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
        
        LOGGER.info("[CloudStorageService] Checking files on zookeeper");
        checkFiles();
        LOGGER.info("[CloudStorageService] Cleaning files on zookeeper");
        cleanFiles();
    }
    
    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {
        this.config = config;
        this.listeners = listeners;
        if (listeners != null) {
            listeners.add(this);
        }
        
        //Criando pastas zookeeper para o módulo de armazenamento
        if (!cms.getZNodeExist(Path.BUCKETS.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.BUCKETS.getFullPath(), null);
        }
        
        LOGGER.info("[CloudStorageService] Starting");        
        
        //Instance methods object
        methodsInstance = new CloudStorageMethodsV1();
        
        //Getting parameters from config file
        bucketsFolder = config.getBucketsFolder();
        methodsInstance.setAuthFolder(config.getBucketsAuthFolder());
        methodsInstance.setGcloudFolder(config.getGcloudFolder());
        methodsInstance.setMyId(config.getId());
        
        //Instance all buckets
        LOGGER.info("[CloudStorageService] Instancing Buckets");
        InstanceBuckets();
        
        // Cleaning possible mounted buckets from last execution
        for (BioBucket aux : bucketList) {
            try {
                methodsInstance.StorageUmount(aux);
            } catch (Throwable t) {
                // Ignore
            }
        }
        
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
            }
        } catch (Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
            t.printStackTrace();
        }
        
        BandwithChecker checker = new BandwithChecker();
        checker.start();
        executorService.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
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
        
        //AMAZON
        //Brazil
//        aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-br", (bucketsFolder + "bionimbuz-a-br"));
//        aux.setEndPoint("s3-sa-east-1.amazonaws.com");
//        bucketList.add(aux);
//        if (!cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
//            cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
//            //cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
//            cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
//        }
//        //US
//        aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-us", (bucketsFolder + "bionimbuz-a-us"));
//        bucketList.add(aux);
//        if (!cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
//            cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
//            //cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
//            cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
//        }
//        //EU
//        aux = new BioBucket(StorageProvider.AMAZON, "bionimbuz-a-eu", (bucketsFolder + "bionimbuz-a-eu"));
//        aux.setEndPoint("s3.eu-central-1.amazonaws.com");
//        bucketList.add(aux);
//        if (!cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
//            cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
//            //cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
//            cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
//        }
        
        //GOOGLE
        //US
        aux = new BioBucket(StorageProvider.GOOGLE, "bionimbuz-g-us", (bucketsFolder + "bionimbuz-g-us"));
        bucketList.add(aux);
        if (!cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            //cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
        }
        //EU
        aux = new BioBucket(StorageProvider.GOOGLE, "bionimbuz-g-eu", (bucketsFolder + "bionimbuz-g-eu"));
        bucketList.add(aux);
        if (!cms.getZNodeExist(Path.NODE_BUCKET.getFullPath(aux.getName()), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            //cms.setData(Path.NODE_BUCKET.getFullPath(aux.getName()), aux.toString());
            cms.createZNode(CreateMode.PERSISTENT, Path.BUCKET_FILES.getFullPath(aux.getName()), null);
        }
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
        
        LOGGER.debug("[CloudStorageService] Looking for file: " + file.getName());
        
        List<BioBucket> buckets = new ArrayList<>();
        
        try {
            
            List<String> bucketsPaths = cms.getChildren(Path.BUCKETS.getFullPath(), null);
                        
            for (String bucket_aux : bucketsPaths) {

                LOGGER.debug("[CloudStorageService] Looking file on bucket: " + bucket_aux);
                ObjectMapper mapper = new ObjectMapper();
                //BioBucket bucket = mapper.readValue(cms.getData(Path.NODE_BUCKET.getFullPath(bucket_aux), null), BioBucket.class);

                List<String> filesList = cms.getChildren(Path.BUCKET_FILES.getFullPath(bucket_aux), null);

                for (String file_aux : filesList) {
                    
                    LOGGER.debug("[CloudStorageService] File (" + file_aux + ") on Bucket (" + bucket_aux + ")");
                    
                    //FileInfo bucket_file = mapper.readValue(cms.getData(Path.NODE_BUCKET_FILE(bucket_aux, file_aux), null), FileInfo.class);
                    
                    LOGGER.debug("[CloudStorageService] Comparing file (" + file.getName() + ") and file_aux (" + file_aux + ")");
                    if (file_aux.equals(file.getName())) {
                        
                        LOGGER.debug("[CloudStorageService] Match");
                        
                        buckets.add(getBucket(bucket_aux));
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }  
        
        if (buckets.isEmpty())
        {
            LOGGER.info("[CloudStorageService] Exception: File not found: " + file.getName());
            return null;
        }
        
        //TODO use some kind of storage policy to chose best bucket
        return buckets.get(0);
    }
    
    public synchronized void checkFiles () {

        try {
            
            for (BioBucket bucket : bucketList) {
                File dataFolder = new File (bucket.getMountPoint() + "/data-folder/");
                
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                } else {
                    
                    if (dataFolder.listFiles() == null)
                        LOGGER.debug("[CloudStorageService] listfiles() for bucket " + bucket.getName() + " is null");
                    else {
                        for (File file : dataFolder.listFiles()) {
                            PluginFile pluginFile = new PluginFile();
                            pluginFile.setId(file.getName());
                            pluginFile.setName(file.getName());
                            pluginFile.setPath(file.getPath());

                            List<String> bucketFiles = cms.getChildren(Path.BUCKET_FILES.getFullPath(bucket.getName()), null);

                            int hits = 0;
                            for (String stringFile : bucketFiles) {

                                ObjectMapper mapper = new ObjectMapper();
                                PluginFile auxFile = mapper.readValue(cms.getData(Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), stringFile), null), PluginFile.class);

                                if (auxFile.equals(pluginFile)) {
                                    hits += 1;
                                }
                            }

                            if (hits == 0) {
                                cms.createZNode(CreateMode.PERSISTENT, Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), pluginFile.getName()), pluginFile.toString());
                            }
                        }
                    }
                }
            }
            
            
            
        } catch (Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    public synchronized void cleanFiles () {
        
        try {
            
            for (BioBucket bucket : bucketList) {
                
                File dataFolder = new File (bucket.getMountPoint() + "/data-folder/");
                List<String> bucketFiles = cms.getChildren(Path.BUCKET_FILES.getFullPath(bucket.getName()), null);
                
                for (String stringFile : bucketFiles) {
                    
                    ObjectMapper mapper = new ObjectMapper();
                    PluginFile auxFile = mapper.readValue(cms.getData(Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), stringFile), null), PluginFile.class);
                    
                    int hits = 0;
                    for (File file : dataFolder.listFiles()) {
                        PluginFile pluginFile = new PluginFile();
                        pluginFile.setId(file.getName());
                        pluginFile.setName(file.getName());
                        pluginFile.setPath(file.getPath());
                        
                        if (pluginFile.equals(auxFile)) {
                            hits += 1;
                        }
                    }
                    
                    if (hits == 0) {
                        cms.delete(Path.NODE_BUCKET_FILE.getFullPath(bucket.getName(), auxFile.getName()));
                    }
                }
                
            }
            
        } catch (Throwable t) {
            LOGGER.error("[CloudStorageService] Exception: " + t.getMessage());
        }
    }
    
    public static boolean checkMode (BioBucket bucket) {
        
        //TODO testar valores de treshold
        if (bucket.getLatency() < 1) {
            if (bucket.getBandwith() > (50*1024*1024)) { //tresshold = 50 MB
                return true;
            }
        }
        
        return false;
    }

    public static List<BioBucket> getBucketList() {
        return bucketList;
    }
    
    public static void main(String[] args) {
        System.out.println("Iniciando Teste.\n\n");


        System.out.println("\n\nTeste Realizado.");
    }
}
