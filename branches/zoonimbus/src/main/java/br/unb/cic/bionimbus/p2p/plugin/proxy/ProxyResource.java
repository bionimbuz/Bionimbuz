package br.unb.cic.bionimbus.p2p.plugin.proxy;

import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.utils.JsonCodec;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: edward
 * Date: 5/25/12
 * Time: 12:23 AM
 * To change this template use File | Settings | File Templates.
 */
@Path("/proxy")
public class ProxyResource {

    private ProxyServerStub server;

//    public ProxyResource() {
//        server = ProxyServerStub.getInstance();
//    }

    //TODO reativar este sub-projeto
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getCommands() throws IOException {
//        List<RequestMessage> out = server.getCommands();
//        return serialize(out);
//    }

    //TODO reativar este sub-projeto
//    @POST
//    @Path("/info")
//    public String retrieveInfo(@FormParam("data") String data) throws IOException, InterruptedException {
//        System.out.println(data);
//        if (data != null && data.length() > 0){
//            ObjectMapper mapper = new ObjectMapper();
//            ResponseMessage<PluginInfo> response = mapper.readValue(data, new TypeReference<ResponseMessage<PluginInfo>>() {});
//            server.setResponse(response);
//            return "Info received";
//        }
//        return null;
//    }

    String serialize(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }
}
