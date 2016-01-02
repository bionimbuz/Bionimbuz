package br.unb.cic.bionimbus.controller.jobcontroller;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.controller.Controller;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that links the User Interface (Web) with the BioNimbuZ Application
 * Core. Controls user access to execute core functions and manages workflows
 * execution
 *
 * @author Vinicius
 */
@Singleton
public class JobController implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);
    private static final int AVRO_PORT = 8080;
    private static RpcClient rpcClient;
    private boolean isConnected = false;

    protected CloudMessageService cms;
    protected BioNimbusConfig config;
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<>();

    @Inject
    public JobController(CloudMessageService cms) {
        Preconditions.checkNotNull(cms);
        this.cms = cms;

        LOGGER.info("JobController started");
    }

    /**
     * Starts JobController
     *
     * @param config
     */
    @Override
    public void start(BioNimbusConfig config) {
        // Initializes AvroClient
        rpcClient = new AvroClient("http", config.getAddress(), AVRO_PORT);

        try {
            // Test to see if hostname is reachable
            if (rpcClient.getProxy().ping()) {
                isConnected = true;
            }
        } catch (IOException ex) {
            LOGGER.error("[Exception] " + ex.getMessage());
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
        LOGGER.info("JobController");
    }

    /*
     * Methods to implement 
     * - User control: 
     * o void logUser (User user): Keeps a HashMap<id, User> with logged user 
     * o void loggoutUser (User user): Deletes the loggedUsers list 
     * 
     * - Job Control:
     * o ArrayList<JobInfo> listJobs (void); 
     * o ArrayList<JobInfo> listJobsByUserId (long userId); 
     * o JobInfo findJobById (String jobId); 
     * o boolean cancelJob (String jobId);
     *
     */
    /**
     * Return BioNimbus configuration
     *
     * @return BioNimbusConfig
     */
    public BioNimbusConfig getConfig() {
        return this.config;
    }

}