/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.controller.elasticitycontroller.ElasticityController;
import br.unb.cic.bionimbus.controller.slacontroller.SlaController;
import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.model.SLA;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.request.StartSlaRequest;
import br.unb.cic.bionimbus.rest.request.StartWorkflowRequest;
import br.unb.cic.bionimbus.rest.resource.AbstractResource;
import static br.unb.cic.bionimbus.rest.resource.AbstractResource.LOGGER;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author guilherme
 */

@Path("/rest")
public class StartElasticityResource extends AbstractResource {
    
    private final WorkflowLoggerDao loggerDao;
    private StartWorkflowRequest startworkflow;
    private WorkflowResource workflowR;
    
    public StartElasticityResource() {
        this.loggerDao = null;
    }    
    
    public StartElasticityResource(ElasticityController elasticityController) {
        this.elasticityController = elasticityController;
        this.loggerDao = null;
    } 
   
//    @POST
//    @Path("/sla/start")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public SLA startWorkflow(StartSlaRequest request) {
//        SLA sla = request.getSla();
//        Workflow workflow = request.getWorkflow();
//        LOGGER.info("New SLA received {id= " + sla.getId()
//                + ",userId= " + sla.getUser().getId()
//                + ",workflowId= " + workflow.getId()
//                + ",workflowdescription= " + workflow.getDescription()
//                + ",limitationType: " + sla.getLimitationType()
//                + ",limitationValueExecutionCost: "+sla.getLimitationValueExecutionCost()
//                + ",limitationValueExecutionTime: "+sla.getLimitationValueExecutionTime()
//                + ",objective: "+sla.getObjective()
//                + "}");
//                LOGGER.info("Instances: ");
//                sla.getInstances().stream().forEach((Instance f) -> {
//                    LOGGER.info(f.toString()+"\n");
//                });
//                
//                slaController.createSlaTemplate(sla, workflow);
//        
//        // Logs
////        loggerDao.log(new Log("QoS chegou no servidor do BioNimbuZ", sla.getUser().getId(), sla.getId(), LogSeverity.INFO));
//
////        try {
////            startworkflow= new StartWorkflowRequest(request.getWorkflow());
////        
////            // Starts it
////            workflowR.startWorkflow(startworkflow);
////
////            // Sets its status as EXECUTING
////      //      request.getWorkflow().setStatus(WorkflowStatus.EXECUTING);
////             
////            // If it gets started with success, persists it on database
////  
////
////        } catch (Exception e) {
////            // Logs
////            loggerDao.log(new Log("Um erro ocorreu na execução de seu Workflow", request.getSla().getUser().getId(), request.getSla().getId(), LogSeverity.ERROR));
////            LOGGER.error("[Exception] " + e.getMessage());
////
////            return Response.status(200).entity(false).build();
////        }
//
////        return Response.status(200).entity(true).build();
//    return sla;
//    }
    

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
