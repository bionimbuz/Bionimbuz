package br.unb.cic.bionimbuz.rest.resource;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.config.ConfigurationRepository;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.plugin.PluginService;
import br.unb.cic.bionimbuz.rest.request.GetConfigurationsRequest;
import br.unb.cic.bionimbuz.rest.request.RequestInfo;
import br.unb.cic.bionimbuz.rest.response.GetConfigurationsResponse;
import br.unb.cic.bionimbuz.rest.response.ResponseInfo;

/**
 *
 * @author Vinicius
 */
@Path("/rest")
public class ConfigurationsResource extends AbstractResource {

    public ConfigurationsResource(JobController jobController) {
        this.jobController = jobController;
    }

    @PermitAll
    @POST
    @Path("/services")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GetConfigurationsResponse returnSupportedServices(GetConfigurationsRequest request) {

        LOGGER.info("Received request from web application");

        List<PluginService> list = BioNimbusConfig.get().getSupportedServices();
        List<String> references = BioNimbusConfig.get().getReferences();
        List<String> supportedFormats = BioNimbusConfig.get().getSupportedFormats();
        List<Instance> instances = ConfigurationRepository.getInstances();
       
        GetConfigurationsResponse response = new GetConfigurationsResponse(list, references, supportedFormats, instances);

        return response;
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
