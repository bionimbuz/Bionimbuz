package br.unb.cic.bionimbuz.rest.resource;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbuz.avro.rpc.AvroClient;
import br.unb.cic.bionimbuz.avro.rpc.RpcClient;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.config.ConfigurationRepository;
import br.unb.cic.bionimbuz.constants.SystemConstants;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.ElasticityController;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.controller.slacontroller.SlaController;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.rest.RestResource;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.utils.YamlUtils;

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
        final String configFile = System.getProperty("config.file", SystemConstants.CFG_FILE_NODE);

        try {
            rpcClient = new AvroClient("http", YamlUtils.mapToClass(configFile, BioNimbusConfig.class).getAddress(), 8080);
        } catch (IOException ex) {
            LOGGER.error("[IOException] " + ex.getMessage());
        }

    }
}
