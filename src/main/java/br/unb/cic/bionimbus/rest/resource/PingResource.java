package br.unb.cic.bionimbus.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Resource used basically just to receive ping request from the web application
 * @author Vinicius
 */
@Path("/rest")
public class PingResource {

    /**
     * Resource used to update application server status flag
     * @return
     */
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.status(Response.Status.OK).build();
    }
}
