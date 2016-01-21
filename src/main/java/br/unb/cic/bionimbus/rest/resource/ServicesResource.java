package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.rest.request.GetServicesRequest;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Vinicius
 */
@Path("/rest")
public class ServicesResource extends AbstractResource {

    public ServicesResource(JobController jobController) {
        this.jobController = jobController;
    }

    @PermitAll
    @POST
    @Path("/services")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response returnSupportedServices(GetServicesRequest request) {
       
        LOGGER.info("Received request from web application");
        
        List<PluginService> list =  jobController.getSupportedServices();
        
        return Response.status(200).entity(list).build();
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
