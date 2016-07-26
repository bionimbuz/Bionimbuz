/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.tarifation.Amazon.Index;
import br.unb.cic.bionimbus.services.tarifation.Google.GoogleDataGet;
import br.unb.cic.bionimbus.toSort.Listeners;
import com.amazonaws.util.json.JSONException;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.WatchedEvent;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class PricingGetterService extends AbstractBioService {

    private static final int PERIOD_HOURS = 12;
    private final ScheduledExecutorService schedExecService;

    public PricingGetterService(final CloudMessageService cms) {

        Preconditions.checkNotNull(cms);
        this.cms = cms;

        schedExecService = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("PricingGetterService-%d")
                .build());
    }

    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {
        try {
            Preconditions.checkNotNull(listeners);
            this.config = config;
            this.listeners = listeners;

            listeners.add(this);

            schedExecService.scheduleAtFixedRate(this, 0, PERIOD_HOURS, TimeUnit.HOURS);

        } catch (Exception ex) {
            Logger.getLogger(PricingGetterService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verifyPlugins() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void event(WatchedEvent eventType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        try {
            Index AmazonInstancesInfo = new Index("pricing.us-east-1.amazonaws.com", "/offers/v1.0/aws/index.json");
        } catch (JSONException | IOException ex) {
            Logger.getLogger(PricingGetterService.class.getName()).log(Level.SEVERE, null, ex);
        }
        GoogleDataGet gdg = new GoogleDataGet();
    }

}
