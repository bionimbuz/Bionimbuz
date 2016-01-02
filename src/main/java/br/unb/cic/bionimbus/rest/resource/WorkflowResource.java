package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.request.StartWorkflowRequest;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import br.unb.cic.bionimbus.rest.response.StartWorkflowResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Class that handle sent workflow via REST request
 *
 * @author Vinicius
 */
@Path("/rest")
public class WorkflowResource extends AbstractResource {

    public WorkflowResource(JobController jobController) {
        this.jobController = jobController;
    }

    @POST
    @Path("/workflow/start/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public StartWorkflowResponse startWorkflow(StartWorkflowRequest request) {
        LOGGER.info("Dados sobre o Workflow: ");
        LOGGER.info("Id: " + request.getWorkflow().getId());
        LOGGER.info("Data: " + request.getWorkflow().getCreationDatestamp());
        LOGGER.info("Tamanho: " + request.getWorkflow().getPipeline().size());
        
        return new StartWorkflowResponse(true);
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
