/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Class that handle sent pipeline via REST request
 *
 * @author Vinicius
 */
@Path("/rest")
public class PipelineResource extends AbstractResource {

    public PipelineResource(JobController jobController) {
        this.jobController = jobController;
    }

    
    
    @POST
    @Path("/pipeline")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void handlePipeline() {

    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
