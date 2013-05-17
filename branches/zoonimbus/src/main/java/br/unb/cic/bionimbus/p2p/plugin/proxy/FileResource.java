package br.unb.cic.bionimbus.p2p.plugin.proxy;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.sun.jersey.multipart.FormDataParam;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.*;

/**
 * Created by IntelliJ IDEA.
 * User: edward
 * Time: 7:46 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("upload")
public class FileResource {

    public static final String UPLOAD_DIR = "/home/edward/tmp/";

    @GET
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    public StreamingOutput downloadFile(@QueryParam("filename") final String filename) throws IOException {

            return new StreamingOutput() {
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        File file = new File(UPLOAD_DIR + File.separator + filename);
                        Files.copy(file, output);
                    } catch (Exception e) {
                        throw new WebApplicationException(e);
                    }
                }
            };
    }

    @POST
    @Produces(TEXT_PLAIN)
    @Consumes(MULTIPART_FORM_DATA)
    public String uploadFile(@FormDataParam("file") final InputStream stream) throws Exception {

        String tempname = UUID.randomUUID().toString();
        final String outputPath = UPLOAD_DIR + File.separator + tempname;
        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return stream;                
            }
        }, new File(outputPath));

        return tempname;
    }
    public static class Message {

        private int code;
        private String message;

        private Message() {
        }

        private Message(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (IOException e) {
                return null;
            }
        }
    }
}

/**
 client.setChunkedEncodingSize(10000);
 WebResource webResource = client.resource(url);
 // file to multi part Request
 MultiPart multiPart = new MultiPart();
 FileDataBodyPart filePart = new FileDataBodyPart("file", file,
 MediaType.APPLICATION_OCTET_STREAM_TYPE);
 multiPart.bodyPart(filePart);
 multiPart.bodyPart(new FormDataBodyPart("code",code));

 ClientResponse getResponse = null;
 getResponse = webResource.type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
 multiPart);

 Now the file are easily send in chunked but the problem is i am not able to receive value of code now..

 On the other server where i am sending this file along code is:

 public void upload(@RequestParam("file") MultipartFile uploadedFile,
 @RequestParam("code") String code,
 Writer responseWriter) throws IOException
 {
 */
