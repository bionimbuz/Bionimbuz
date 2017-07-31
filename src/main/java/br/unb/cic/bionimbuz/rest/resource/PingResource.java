package br.unb.cic.bionimbuz.rest.resource;

import br.unb.cic.bionimbuz.rest.request.RequestInfo;
import br.unb.cic.bionimbuz.rest.response.ResponseInfo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Resource used basically to receive ping request from the web application
 * @author Vinicius
 */
@Path("/rest")
public class PingResource extends AbstractResource {

    /**
     * Resource used to update application server status flag
     * @return
     */
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
