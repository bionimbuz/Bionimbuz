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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handle sent pipeline via REST request
 * @author Vinicius
 */
@Path("/rest")
public class PipelineResource extends BaseResource {
    
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);
    
    @POST
    @Path("/pipeline")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void handlePipeline() {
        
    }
}
