package br.unb.cic.bionimbus.controller.usercontroller;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.controller.Controller;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls what actions to take when an user logs into BioNimbuZ, logout,
 * timeout
 *
 * @author Vinicius
 */
@Singleton
public class UserController implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private static final int SESSION_TIMEOUT = 2;

    // Controls Thread execution
    private final ScheduledExecutorService threadExecutor = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("UserController-%d").build());
    private final CloudMessageService cms;

    // String  = User Login , LocalDateTime = User Last Access Time
    private final HashMap<String, LocalDateTime> lastAccessMap = new HashMap<>();

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
        LOGGER.info("Checking logged users: " + lastAccessMap.size() + " users");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Map.Entry<String, LocalDateTime> entry : lastAccessMap.entrySet()) {
            String login = entry.getKey();
            LocalDateTime loginTime = entry.getValue();

            long diffInMinutes = Duration.between(entry.getValue(), LocalDateTime
                    .parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), formatter)).toMinutes();

            if (diffInMinutes >= SESSION_TIMEOUT) {
                logoutUser(login);
            }
        }
    }

    /**
     * Logs an user adding a new /users/logged/{login} ZooKeeper node
     *
     * @param login
     * @return
     */
    public boolean logUser(String login) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // If it is registered, verifiy if it is logged or not
        if (!cms.getZNodeExist(Path.LOGGED_USERS.getFullPath(login), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.LOGGED_USERS.getFullPath(login), null);

            // Puts the new user in the last access map
            lastAccessMap.put(login, LocalDateTime.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), formatter));

            return true;
        }

        // Already logged
        return true;
    }

    /**
     * Informs ZooKeeper of an user Logout deleting /users/logged/{login}
     *
     * @param login
     */
    public void logoutUser(String login) {
        if (cms.getZNodeExist(Path.LOGGED_USERS.getFullPath(login), null)) {
            cms.delete(Path.LOGGED_USERS.getFullPath(login));
            lastAccessMap.remove(login);
        }
    }

    /**
     * Update user last access
     *
     * @param login
     * @return
     */
    public boolean updateUserLastAccess(String login) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // If login is no more at lastAccessMap, it returns null. That's why the == null verification
        return (lastAccessMap.replace(login, LocalDateTime.parse(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), formatter)) == null);
    }

    /**
     * Get the count of logged users
     *
     * @return
     */
    public int getLoggedUsersCount() {
        return cms.getChildrenCount(Path.USERS.getFullPath() + Path.LOGGED_USERS, null);
    }
}
