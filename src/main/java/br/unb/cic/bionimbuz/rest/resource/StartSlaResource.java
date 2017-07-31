/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest.resource;

import br.unb.cic.bionimbuz.controller.slacontroller.SlaController;
import br.unb.cic.bionimbuz.model.Instance;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import br.unb.cic.bionimbuz.model.SLA;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbuz.rest.request.RequestInfo;
import br.unb.cic.bionimbuz.rest.request.StartSlaRequest;
import br.unb.cic.bionimbuz.rest.request.StartWorkflowRequest;
import br.unb.cic.bionimbuz.rest.response.ResponseInfo;

/**
 *
 * @author biolabid2
 * 
 * CLASSE nao utilizada
 */
@Path("/rest")
public class StartSlaResource extends AbstractResource{
    private final WorkflowLoggerDao loggerDao;
    private StartWorkflowRequest startworkflow;
    private WorkflowResource workflowR;
    
    public StartSlaResource() {
        this.loggerDao = null;
    }    
    
    public StartSlaResource(SlaController slaController) {
        this.slaController = slaController;
        this.loggerDao = null;
    }    
    /**
     * Handles StartWorkflowRequests by submiting them to the Core
     * 
     * 
     * @param request
     * @return
     */
    @POST
    @Path("/sla/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SLA startWorkflow(StartSlaRequest request) {
        SLA sla = request.getWorkflow().getSla();
        Workflow workflow = request.getWorkflow();
        LOGGER.info("New SLA received {id= " + sla.getId()
                + ",workflowId= " + workflow.getId()
                + ",workflowdescription= " + workflow.getDescription()
                + ",limitationType: " + sla.getLimitationType()
                + ",limitationValueExecutionCost: "+sla.getLimitationValueExecutionCost()
                + ",limitationValueExecutionTime: "+sla.getLimitationValueExecutionTime()
                + ",objective: "+sla.getObjective()
                + "}");
                
                slaController.createSlaTemplate(sla, workflow);
        
        // Logs
//        loggerDao.log(new Log("QoS chegou no servidor do BioNimbuZ", sla.getUser().getId(), sla.getId(), LogSeverity.INFO));

//        try {
//            startworkflow= new StartWorkflowRequest(request.getWorkflow());
//        
//            // Starts it
//            workflowR.startWorkflow(startworkflow);
//
//            // Sets its status as EXECUTING
//      //      request.getWorkflow().setStatus(WorkflowStatus.EXECUTING);
//             
//            // If it gets started with success, persists it on database
//  
//
//        } catch (Exception e) {
//            // Logs
//            loggerDao.log(new Log("Um erro ocorreu na execução de seu Workflow", request.getSla().getUser().getId(), request.getSla().getId(), LogSeverity.ERROR));
//            LOGGER.error("[Exception] " + e.getMessage());
//
//            return Response.status(200).entity(false).build();
//        }

//        return Response.status(200).entity(true).build();
    return sla;
    }
    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
