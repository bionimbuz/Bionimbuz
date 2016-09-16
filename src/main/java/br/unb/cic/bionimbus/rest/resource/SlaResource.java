/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.unb.cic.bionimbus.model.Log;
import br.unb.cic.bionimbus.model.LogSeverity;
import br.unb.cic.bionimbus.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.request.StartSlaRequest;
import br.unb.cic.bionimbus.rest.request.StartWorkflowRequest;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;

/**
 *
 * @author biolabid2
 */
@Path("/rest")
public class SlaResource extends AbstractResource{
    private final WorkflowLoggerDao loggerDao;
    private StartWorkflowRequest startworkflow;
    private WorkflowResource workflowR;
    
    public SlaResource() {
        this.loggerDao = null;
    }
    
    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Handles StartWorkflowRequests by submiting them to the Core
     *
     * @param request
     * @return
     */
    @POST
    @Path("/sla/start/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startWorkflow(StartSlaRequest request) {
        System.out.println(" algo"+ request.toString());
        LOGGER.info("New workflow received {id=" + request.getWorkflow().getId()
                + ",jobs=" + request.getWorkflow().getJobs().size()
                + ",userId=" + request.getWorkflow().getUserId()
                + "}");

        // Logs
        loggerDao.log(new Log("QoS chegou no servidor do BioNimbuZ", request.getSla().getUser().getId(), request.getSla().getId(), LogSeverity.INFO));

        try {
            startworkflow= new StartWorkflowRequest(request.getWorkflow());
        
            // Starts it
            workflowR.startWorkflow(startworkflow);

            // Sets its status as EXECUTING
      //      request.getWorkflow().setStatus(WorkflowStatus.EXECUTING);
             
            // If it gets started with success, persists it on database
  

        } catch (Exception e) {
            // Logs
            loggerDao.log(new Log("Um erro ocorreu na execução de seu Workflow", request.getSla().getUser().getId(), request.getSla().getId(), LogSeverity.ERROR));
            LOGGER.error("[Exception] " + e.getMessage());

            return Response.status(200).entity(false).build();
        }

        return Response.status(200).entity(true).build();
    }
}
