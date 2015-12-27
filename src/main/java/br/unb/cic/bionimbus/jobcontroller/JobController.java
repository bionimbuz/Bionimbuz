package br.unb.cic.bionimbus.jobcontroller;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.controller.AbstractBioController;
import com.google.inject.Singleton;
import java.io.IOException;
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
public class JobController extends AbstractBioController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);
    private static final int AVRO_PORT = 8080;
    private static RpcClient rpcClient;
    private boolean isConnected = false;

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
                LOGGER.info("[JobController] RpcClient connected");
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

}
