/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import br.unb.cic.bionimbus.toSort.CloudStorageMethods.*;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import java.util.List;

import com.google.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lucas
 */

@Singleton
public class CloudStorageService extends AbstractBioService{

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudStorageService.class);
    
    @Inject
    private final ScheduledExecutorService executorService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("CloudStorageService-%d").build());
    
    private static final String BUCKETSPATH = "/home/baile/TESTE-BIO/";
    List<BioBucket> bucketList;
    static private CloudStorageMethods methodsInstance;
    
    @Inject
    public CloudStorageService(final CloudMessageService cms) {
        
        Preconditions.checkNotNull(cms);
        
        this.cms = cms;
    }
    
    @Override
    public void run() {
        for (BioBucket aux : bucketList) {
            boolean result = methodsInstance.CheckStorageLatency(aux);
            
            if (!result) {
                LOGGER.error("Erro ao checar a Latência do bucket " + aux.getName());
            }  
            
            result = methodsInstance.CheckStorageBandwith(aux);
            
            if (!result) {
                LOGGER.info("Erro ao checar a Banda do bucket " + aux.getName());
            } 
        }
    }
    
    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {
        this.config = config;
        this.listeners = listeners;
        if (listeners != null) {
            listeners.add(this);
        }
        
        LOGGER.info("[CloudStorageService] Starting");
        
        //Instance methods object
        methodsInstance = new CloudStorageMethodsV1();
        
        //Instance all buckets
        InstanceBuckets();
        
        //Authenticate providers
        for (StorageProvider aux : StorageProvider.values()) {
            methodsInstance.StorageAuth(aux);
        }
        
        //Mount all buckets
        for (BioBucket aux : bucketList) {
            methodsInstance.StorageMount(aux);
        }
        
        executorService.scheduleAtFixedRate(this, 0, 30, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
        
        for (BioBucket aux : bucketList) {
            methodsInstance.StorageUmount(aux);
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
        aux = new BioBucket(StorageProvider.AMAZON, "testbionimbuz", (BUCKETSPATH + "testbionimbuz"));
        aux.setEndPoint("s3-sa-east-1.amazonaws.com");
        bucketList.add(aux);
        //US
        aux = new BioBucket(StorageProvider.AMAZON, "testusastandard", (BUCKETSPATH + "testusastandard"));
        bucketList.add(aux);
        //EU
        aux = new BioBucket(StorageProvider.AMAZON, "testbucketgerman", (BUCKETSPATH + "testbucketgerman"));
        aux.setEndPoint("s3.eu-central-1.amazonaws.com");
        bucketList.add(aux);
        
        //GOOGLE
        //US
        aux = new BioBucket(StorageProvider.GOOGLE, "bionimbuzteste", (BUCKETSPATH + "bionimbuzteste"));
        bucketList.add(aux);
        //EU
        aux = new BioBucket(StorageProvider.GOOGLE, "biotesteu", (BUCKETSPATH + "biotesteu"));
        bucketList.add(aux);
    }   
    
    private BioBucket getBucket (String name) {
        for (BioBucket aux : bucketList) {
            if (aux.getName().equals(name)) {
                return aux;
            }
        }
        return null;
    }
}
