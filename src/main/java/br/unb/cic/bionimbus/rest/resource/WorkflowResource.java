package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.persistence.dao.WorkflowDao;
import br.unb.cic.bionimbus.rest.request.GetWorkflowStatusRequest;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.request.StartWorkflowRequest;
import br.unb.cic.bionimbus.rest.response.GetWorkflowStatusResponse;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Class that handle sent workflow via REST request
 *
 * @author Vinicius
 */
@Path("/rest")
public class WorkflowResource extends AbstractResource {

    private final WorkflowDao workflowDao;

    public WorkflowResource(JobController jobController) {
        this.jobController = jobController;
        this.workflowDao = new WorkflowDao();
    }

    /**
     * Handles StartWorkflowRequests by submiting them to the Core
     *
     * @param request
     * @return
     */
    @POST
    @Path("/workflow/start/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startWorkflow(StartWorkflowRequest request) {
        LOGGER.info("New workflow received {id=" + request.getWorkflow().getId()
                + ",size=" + request.getWorkflow().getJobs().size()
                + ",status=" + request.getWorkflow().getStatus()
                + ",userId=" + request.getWorkflow().getUserId()
                + "}");

        workflowDao.persist(request.getWorkflow());

        return Response.status(200).entity(true).build();
    }

    /**
     * Verifies the status of an user's workflow
     *
     * @param request
     * @return
     */
    @POST
    @Path("/workflow/status/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GetWorkflowStatusResponse getWorkflowStatus(GetWorkflowStatusRequest request) {
        LOGGER.info("Received GetWorkflowStatus request from userId=" + request.getUserId());
        List<Workflow> workflowList = workflowDao.listByUserId(request.getUserId());

        return new GetWorkflowStatusResponse(workflowList);
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
