/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.storage.bucket;

import br.unb.cic.bionimbuz.services.storage.bucket.methods.CloudMethodsAmazonGoogle;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lucas
 */
public class BandwithCheckerBucket implements Runnable {

    Logger LOGGER = LoggerFactory.getLogger(BandwithCheckerBucket.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    static private List<BioBucket> bucketList = CloudStorageService.getBucketList();
    CloudStorageMethods methodsInstance = new CloudMethodsAmazonGoogle();

    @Override
    public void run() {
        LOGGER.info("[BandwithChecker] Starting Latency/Bandiwth check on buckets ");
        try {
            for (BioBucket aux : bucketList) {

                LOGGER.info("[BandwithChecker] Checking Latency for bucket " + aux.getName());
                methodsInstance.CheckStorageLatency(aux);
                LOGGER.debug("[BandwithChecker] Latency for bucket " + aux.getName() + ": " + aux.getLatency());

                LOGGER.info("[BandwithChecker] Checking Bandwith for bucket " + aux.getName());
                methodsInstance.CheckStorageBandwith(aux);
                LOGGER.debug("[BandwithChecker] Upload for bucket " + aux.getName() + ": " + (aux.getUpBandwith()/1024)/1024 + " MB/s" );
                LOGGER.debug("[BandwithChecker] Download for bucket " + aux.getName() + ": " + (aux.getDlBandwith()/1024)/1024 + " MB/s" );
            }
        } catch (Throwable t) {
            LOGGER.error("[BandwithChecker] Exception: " + t.getMessage());
            t.printStackTrace();
        }
        LOGGER.info("[BandwithChecker] Finishing Latency/Bandiwth check on buckets ");
    }

    public void start() {

        scheduler.scheduleAtFixedRate(this, 0, 15, TimeUnit.MINUTES);
    }

}
