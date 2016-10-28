/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.controller.slacontroller;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.controller.Controller;
import br.unb.cic.bionimbus.controller.usercontroller.UserController;
import br.unb.cic.bionimbus.model.SLA;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.services.RepositoryService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zoonimbus
 */
public class SlaController  implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final ScheduledExecutorService threadExecutor = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("SlaController-%d").build());
    protected CloudMessageService cms;
    protected BioNimbusConfig config;
    private final RepositoryService repositoryService;

    /**
     * Starts SlaController execution
     *
     * @param cms
     * @param rs
     */
    @Inject
    public SlaController(CloudMessageService cms, RepositoryService rs) {
        Preconditions.checkNotNull(cms);
        this.repositoryService = rs;
        this.cms = cms;

        LOGGER.info("SLAController started");
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
        LOGGER.info("Checking SLA users:");

    }

    public void startSla() {

    }

    public void createSlaTemplate(SLA sla, Workflow workflow) {
      
        
        repositoryService.getPeers().values().stream().forEach((plugin) -> {
            System.out.println(plugin);
        });
    }

}
