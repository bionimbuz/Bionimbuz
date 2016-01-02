/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.controller.usercontroller;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.controller.Controller;
import br.unb.cic.bionimbus.rest.model.User;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vinicius
 */
@Singleton
public class UserController implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final ScheduledExecutorService threadExecutor = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("UserController-%d").build());
    private final CloudMessageService cms;

    @Inject
    public UserController(CloudMessageService cms) {
        Preconditions.checkNotNull(cms);
        this.cms = cms;
        LOGGER.info("UserController started");
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
        LOGGER.info("Checking logged users...");
    }

    /**
     * Logs an user adding a new /users/logged/{id} ZooKeeper node
     *
     * @param user
     * @return
     */
    public boolean logUser(User user) {
        // If it is registered, verifiy if it is logged or not
        if (!cms.getZNodeExist(Path.LOGGED_USERS.getFullPath(user.getLogin()), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.LOGGED_USERS.getFullPath(user.getLogin()), null);

            return true;
        }

        // Already logged
        return true;
    }

}
