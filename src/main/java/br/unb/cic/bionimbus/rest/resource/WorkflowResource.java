package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.model.Log;
import br.unb.cic.bionimbus.model.LogSeverity;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.model.WorkflowStatus;
import br.unb.cic.bionimbus.persistence.dao.WorkflowDao;
import br.unb.cic.bionimbus.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbus.rest.request.GetWorkflowHistoryRequest;
import br.unb.cic.bionimbus.rest.request.GetWorkflowStatusRequest;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.request.StartWorkflowRequest;
import br.unb.cic.bionimbus.rest.response.GetWorkflowHistoryResponse;
import br.unb.cic.bionimbus.rest.response.GetWorkflowStatusResponse;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import java.util.Collections;
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
    private final WorkflowLoggerDao loggerDao;

    public WorkflowResource(JobController jobController) {
        this.jobController = jobController;
        this.workflowDao = new WorkflowDao();
        this.loggerDao = new WorkflowLoggerDao();
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
                + ",jobs=" + request.getWorkflow().getJobs().size()
                + ",userId=" + request.getWorkflow().getUserId()
                + "}");

        // Logs
        loggerDao.log(new Log("Workflow chegou no servidor do BioNimbuZ", request.getWorkflow().getUserId(), request.getWorkflow().getId(), LogSeverity.INFO));

        try {
            // Starts it
            jobController.startWorkflow(request.getWorkflow());

            // Sets its status as EXECUTING
            request.getWorkflow().setStatus(WorkflowStatus.EXECUTING);

            // If it gets started with success, persists it on database
            workflowDao.persist(request.getWorkflow());

        } catch (Exception e) {
            // Logs
            loggerDao.log(new Log("Um erro ocorreu na execução de seu Workflow", request.getWorkflow().getUserId(), request.getWorkflow().getId(), LogSeverity.ERROR));
            LOGGER.error("[Exception] " + e.getMessage());

            return Response.status(200).entity(false).build();
        }

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

    @POST
    @Path("/workflow/history/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GetWorkflowHistoryResponse getWorkflowHistory(GetWorkflowHistoryRequest request) {
        LOGGER.info("Received GetWorkflowHistory request for workflowId=" + request.getWorkflowId());
        
        // Retrieves from database
        List<Log> log = loggerDao.listByWorkflowId(request.getWorkflowId());
        
        // Sort it by timestamp
        Collections.sort(log, Log.comparator);
        
        return new GetWorkflowHistoryResponse(log);
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
