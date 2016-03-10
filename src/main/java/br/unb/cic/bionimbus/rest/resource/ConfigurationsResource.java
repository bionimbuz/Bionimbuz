package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.config.ConfigurationRepository;
import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.rest.request.GetConfigurationsRequest;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.response.GetConfigurationsResponse;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

        List<PluginService> list = ConfigurationRepository.getSupportedServices();
        List<String> references = ConfigurationRepository.getReferences();
        List<String> supportedFormats = ConfigurationRepository.getSupportedFormats();
        List<Instance> instances = ConfigurationRepository.getInstances();
        
        GetConfigurationsResponse response = new GetConfigurationsResponse(list, references, supportedFormats, instances);

        return response;
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
