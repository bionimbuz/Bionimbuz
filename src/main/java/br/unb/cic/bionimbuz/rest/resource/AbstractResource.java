package br.unb.cic.bionimbuz.rest.resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbuz.avro.rpc.AvroClient;
import br.unb.cic.bionimbuz.avro.rpc.RpcClient;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.ElasticityController;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.controller.slacontroller.SlaController;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.rest.RestResource;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;

/**
 * Base resource for other REST resources
 *
 * @author Vinicius
 */
public abstract class AbstractResource implements RestResource {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractResource.class);
    protected static RpcClient rpcClient;
    protected CloudMessageService cms;
    
    // Controllers
    protected JobController jobController;
    protected UserController userController;
    protected SlaController slaController;
    protected ElasticityController elasticityController;
    
    static {
        rpcClient = new AvroClient("http", BioNimbusConfig.get().getAddress(), 8080);
    }
}
