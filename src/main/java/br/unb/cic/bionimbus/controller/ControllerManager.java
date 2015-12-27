/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.controller;

import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        LOGGER.info("Initialing ControllerManager");
    }
}
