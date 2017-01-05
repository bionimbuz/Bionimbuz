package br.unb.cic.bionimbuz.rest.resource;

import br.unb.cic.bionimbuz.avro.rpc.AvroClient;
import br.unb.cic.bionimbuz.avro.rpc.RpcClient;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import static br.unb.cic.bionimbuz.config.BioNimbusConfigLoader.loadHostConfig;
import br.unb.cic.bionimbuz.config.ConfigurationRepository;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.controller.slacontroller.SlaController;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.rest.RestResource;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base resource for other REST resources
 *
 * @author Vinicius
 */
public abstract class AbstractResource implements RestResource {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractResource.class);
    protected static RpcClient rpcClient;
    protected CloudMessageService cms;
    protected BioNimbusConfig config = ConfigurationRepository.getConfig();
    
    // Controllers
    protected JobController jobController;
    protected UserController userController;
    protected SlaController slaController;
    
    static {
        final String configFile = System.getProperty("config.file", "conf/node.yaml");

        try {
            rpcClient = new AvroClient("http", loadHostConfig(configFile).getAddress(), 8080);
        } catch (IOException ex) {
            LOGGER.error("[IOException] " + ex.getMessage());
        }

    }
}
