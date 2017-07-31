/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest.resource;

import br.unb.cic.bionimbuz.controller.elasticitycontroller.ElasticityController;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbuz.rest.request.RequestInfo;
import br.unb.cic.bionimbuz.rest.request.CreateElasticityRequest;
import br.unb.cic.bionimbuz.rest.response.ResponseInfo;
import br.unb.cic.bionimbuz.rest.response.CreateElasticityResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author guilherme
 */

@Path("/rest")
public class ElasticityResource extends AbstractResource {
    
    private final WorkflowLoggerDao loggerDao;
    
    public ElasticityResource() {
        this.loggerDao = null;
    }    
    
    public ElasticityResource(ElasticityController elasticityController) {
        this.elasticityController = elasticityController;
        this.loggerDao = null;
    } 
   
    @POST
    @Path("/elasticity/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CreateElasticityResponse createElasticity(CreateElasticityRequest request){
        
        String ip = null;
        boolean flag =false;
        String provider=request.getProvider();
        String type =request.getType();
        String instanceName=request.getInstanceName();
        String operation=request.getOperation();
        String idInstance=request.getInstanceName();
        LOGGER.info("New VM to be created received with type: " + type + "and provider: " + request.getProvider()+ "and name" + request.getInstanceName());
        switch (operation) {
            case "create": {
        
            try {
                LOGGER.info("New VM to be created received with type: " + request.getType() + "and provider: " + request.getProvider()+ "and name" + request.getInstanceName());
                
                ip = elasticityController.createInstance(request.getProvider(), request.getType(), request.getInstanceName());
                flag =true;
                break;
            } catch (InterruptedException ex) {
                Logger.getLogger(ElasticityResource.class.getName()).log(Level.SEVERE, null, ex);
                flag =false;
            }
            }
            case "delete": {
                
                LOGGER.info("New VM to be deleted received with type: " + request.getType() + "and provider: " + request.getProvider()+ "and name" + request.getInstanceName());
                break;
            }
        }
        CreateElasticityResponse response =new CreateElasticityResponse (ip, flag);
        return response;
    }
    

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
