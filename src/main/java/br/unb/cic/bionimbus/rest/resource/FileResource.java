package br.unb.cic.bionimbus.rest.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.filechooser.FileSystemView;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import br.unb.cic.bionimbus.persistence.dao.FileDao;
import br.unb.cic.bionimbus.rest.request.UploadRequest;
import br.unb.cic.bionimbus.rest.response.UploadResponse;

@Path("/rest/file/")
public class FileResource extends BaseResource {
    private static final String UPLOADED_FILES_DIRECTORY = FileSystemView.getFileSystemView().getHomeDirectory() + "/zoonimbusProject/data-folder/uploaded-files/";
    private final FileDao fileDao;

    public FileResource() {
        fileDao = new FileDao();
    }

    /**
     * Handles uploaded file from client
     * @param request
     * @return
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public UploadResponse handleUploadedFile(@MultipartForm UploadRequest request) {
//		if (!isLogged(request.getLogin())) {
//			return new UploadResponse(false);
//		}

        try {
            LOGGER.info("Upload request received [filename=" + request.getUploadedFileInfo().getName() + "]");

            // Writes file on disk
            writeFile(request.getData(), request.getUploadedFileInfo().getName());
            
        } catch (IOException e) {
            LOGGER.error("[IOException - " + e.getMessage() + "] FileResource.handleUploadedFile()");
            
        } catch (Exception e) {
            LOGGER.error("[Exception - " + e.getMessage() + "] FileResource.handleUploadedFile()");
        }

        // Creates an UserFile using UploadadeFileInfo from request and persists on Database
        fileDao.persist(request.getUploadedFileInfo());

        return new UploadResponse();
    }

    @DELETE
    @Path("/{fileID}")
    public void deleteFile(@PathParam("fileID") Long id) {
        LOGGER.info("Delete File Request received. Id: " + id);
    }

    /**
     * Save file in disk
     * @param file
     * @throws IOException
     */
    private void writeFile(byte[] content, String filename) throws IOException {
        File file = new File(UPLOADED_FILES_DIRECTORY + filename);

        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fop = new FileOutputStream(file);

        LOGGER.info("File created. [path=" + UPLOADED_FILES_DIRECTORY + filename + "]");

        fop.write(content);
        fop.flush();
        fop.close();

    }

}
