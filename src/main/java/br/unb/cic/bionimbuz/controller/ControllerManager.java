/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.controller;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.services.Service;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;

/**
 *
 * @author Vinicius
 */
@Singleton
public class ControllerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerManager.class);

    private final Set<Controller> controllers = new LinkedHashSet<>();

    private final Set<Service> services = new LinkedHashSet<>();

    private final CloudMessageService cms;

    @Inject
    public ControllerManager(Set<Service> services, Set<Controller> controllers, CloudMessageService cms) {
        this.cms = cms;
        this.controllers.addAll(controllers);
        this.services.addAll(services);

        LOGGER.info("Starting ControllerManager");
    }

    /**
     * Starts all controllers
     *
     * @param config
     */
    public void startAll() {
        try {
            connectZK(BioNimbusConfig.get().getZkHosts());
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("[Exception] " + ex.getMessage());
        }

        // Starts controllers
        for (Controller controller : controllers) {
            controller.start();
        }
        LOGGER.info("All Controller are online");
    }

    /**
     * Connects to ZooKeeper
     *
     * @param hosts
     * @throws IOException
     * @throws InterruptedException
     */
    public void connectZK(String hosts) throws IOException, InterruptedException {
        cms.connect(hosts);
    }
}
