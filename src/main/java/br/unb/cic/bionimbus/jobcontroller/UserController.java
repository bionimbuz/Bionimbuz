/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.jobcontroller;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.controller.Controller;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.utils.LoggedUserList;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vinicius
 */
public class UserController implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private CloudMessageService cms;

    @Inject
    public UserController(CloudMessageService cms) {
        Preconditions.checkNotNull(cms);
        this.cms = cms;

        LOGGER.info("UserController started");
    }

    @Override
    public void start(BioNimbusConfig config) {
        LOGGER.info("UserController.start()");
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void logUser() {
        LoggedUserList.add(null);
    }
}
