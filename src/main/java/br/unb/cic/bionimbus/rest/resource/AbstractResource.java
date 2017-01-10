package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import static br.unb.cic.bionimbus.config.BioNimbusConfigLoader.loadHostConfig;
import br.unb.cic.bionimbus.config.ConfigurationRepository;
import br.unb.cic.bionimbus.controller.elasticitycontroller.ElasticityController;
import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.controller.slacontroller.SlaController;
import br.unb.cic.bionimbus.controller.usercontroller.UserController;
import br.unb.cic.bionimbus.rest.RestResource;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
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
    protected ElasticityController elasticityController;
    
    static {
        final String configFile = System.getProperty("config.file", "conf/node.yaml");

        try {
            rpcClient = new AvroClient("http", loadHostConfig(configFile).getAddress(), 8080);
        } catch (IOException ex) {
            LOGGER.error("[IOException] " + ex.getMessage());
        }

    }
}
