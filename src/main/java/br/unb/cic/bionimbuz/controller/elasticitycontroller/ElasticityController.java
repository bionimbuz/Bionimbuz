/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.controller.elasticitycontroller;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.Controller;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zoonimbus
 */
public class ElasticityController implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final ScheduledExecutorService threadExecutor = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("ElasticityController-%d").build());
    protected CloudMessageService cms;
    protected BioNimbusConfig config;
    private final RepositoryService repositoryService;

    /**
     * Starts ElasticityController execution
     *
     * @param cms
     * @param rs
     */
    @Inject
    public ElasticityController(CloudMessageService cms, RepositoryService rs) {
        Preconditions.checkNotNull(cms);
        this.repositoryService = rs;
        this.cms = cms;
        LOGGER.info("ElasticityController started");
    }

    @Override
    public void start(BioNimbusConfig config) {
        threadExecutor.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
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
        LOGGER.info("Checking Elasticity Controller:");

    }

    public String createInstance(String provider, String type, String nameInstance) throws InterruptedException {
        AmazonAPI amazonapi = new AmazonAPI();
        GoogleAPI googleapi = new GoogleAPI();
        String IP = null;
        switch (provider) {
            case "Amazon": {
                try {
                    //amazonapi.createinstance(type, nameInstance);
                    amazonapi.createinstance("t2.micro", nameInstance);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(ElasticityController.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Amazon IP:" + amazonapi.getIpInstance());

                while ((IP = amazonapi.getIpInstance()) == null) {
                    Thread.sleep(1000);
                }
                break;
            }
            case "Google": {
                try {
                    //                    googleapi.createinstance(type, nameInstance);
                    googleapi.createinstance("n1-standard-1", nameInstance);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(ElasticityController.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Google IP:" + googleapi.getIpInstance());
                while ((IP = googleapi.getIpInstance()) == null) {
                    Thread.sleep(1000);
                }
                break;
            }
        }
        return IP;
    }
}