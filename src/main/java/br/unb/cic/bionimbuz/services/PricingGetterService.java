
package br.unb.cic.bionimbuz.services;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.zookeeper.WatchedEvent;

import com.amazonaws.util.json.JSONException;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.tarification.Amazon.AmazonIndex;
import br.unb.cic.bionimbuz.services.tarification.Google.GoogleCloud;
import br.unb.cic.bionimbuz.toSort.Listeners;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class PricingGetterService extends AbstractBioService {

    private static final int PERIOD_HOURS = 24;
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
    public void start(List<Listeners> listeners) {

        Preconditions.checkNotNull(listeners);
        this.listeners = listeners;

        listeners.add(this);

        schedExecService.scheduleAtFixedRate(this, 0, PERIOD_HOURS, TimeUnit.HOURS);

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
            AmazonIndex amazonIndex = new AmazonIndex("pricing.us-east-1.amazonaws.com", "/offers/v1.0/aws/index.json");
            GoogleCloud googleCloud = new GoogleCloud("www.cloudpricingcalculator.appspot.com","/static/data/pricelist.json");
        } catch (JSONException | IOException ex) {
            Logger.getLogger(PricingGetterService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
