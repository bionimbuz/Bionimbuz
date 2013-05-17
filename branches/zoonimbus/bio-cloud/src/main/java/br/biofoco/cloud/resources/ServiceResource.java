/**
 * Copyright (C) 2011 University of Brasilia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.biofoco.cloud.resources;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import br.biofoco.cloud.services.ServiceInvocation;
import br.biofoco.cloud.services.ServiceManager;
import br.biofoco.cloud.utils.JsonUtil;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.multipart.MultiPart;

@Path("/")
public class ServiceResource {
	
	private final ServiceManager serviceManager = ServiceManager.getInstance();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String greetings(){
		return this.toString();
	}

	@GET
	@Path("/services")
	@Produces(MediaType.TEXT_PLAIN)
	public String listServices() throws IOException {
		return JsonUtil.toString(serviceManager.listServices());
	}
	
	@POST
	@Path("/services/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String invokeService(@PathParam("id") String serviceID, MultiPart multiPart) {
		
	    ServiceInvocation invocation = multiPart.getBodyParts().get(0).getEntityAs(ServiceInvocation.class);
	    System.out.println("name : " + invocation.getFilename());
		
		return serviceManager.invokeService(serviceID);		
	}
	
	@GET
	@Path("/service/result/{task}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getStatus(@PathParam("task") String taskID) {		
		return serviceManager.getTaskResult(taskID);
	}
		
	@POST
	@Path("/upload")
	@Consumes("multipart/mixed")
	public Response upload(MultiPart multiPart){
		// get the second part which is the project logo
	    BodyPartEntity bpe = (BodyPartEntity) multiPart.getBodyParts().get(0).getEntity();
	    String id = UUID.randomUUID().toString();
	    boolean isProcessed = false;
	    String message = null;
	    try {
	      InputStream source = bpe.getInputStream();
	      FileOutputStream fos = new FileOutputStream("/tmp/" + id);
	      
	      byte[] buf = new byte[1024 * 60];
	      int c;
	      
	      while ((c = source.read(buf)) != -1) {
	    	  fos.write(buf, 0, c);
	      }
	      fos.flush();
	      fos.close();
	   
	      isProcessed = true;
	 
	    } catch (Exception e) {
	      message = e.getMessage();
	    }
	    
	    if (isProcessed) {
	      return Response.status(Response.Status.ACCEPTED)
	                     .entity("Attachements processed successfully.")
	                     .type(MediaType.TEXT_PLAIN)
	                     .build();
	    }
	    
	    return Response.status(Response.Status.BAD_REQUEST)
	                   .entity("Failed to process attachments. Reason : " + message)
	                   .type(MediaType.TEXT_PLAIN)
	                   .build();
	}
	
    @POST
    @Path("/upload2")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response handleUpload(@FormDataParam("file") InputStream stream) throws Exception {
        return Response.ok(IOUtils.toString(stream)).build();
    }	
	
	@POST
	@Path("/upload3")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public String create(@FormDataParam("file") InputStream file
						,@FormDataParam("file") FormDataContentDisposition fileInfo
						,@FormDataParam("name") String name
						,@FormDataParam("description") String description) throws IOException {
		
		
	    System.out.println(name);
	    System.out.println(description);
	    System.out.println(fileInfo.getFileName());
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader(file));
	    String line;
	    
	    while ((line = reader.readLine()) != null) {
	    	System.out.println(line);
	    }
		
		return "OK";
	}
	
}
