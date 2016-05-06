package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.config.ConfigurationRepository;
import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.model.FileInfo;
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
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.request.UploadRequest;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import br.unb.cic.bionimbus.security.Hash;
import br.unb.cic.bionimbus.toSort.BioBucket;
import br.unb.cic.bionimbus.toSort.CloudStorageMethods;
import br.unb.cic.bionimbus.toSort.CloudStorageMethodsV1;
import br.unb.cic.bionimbus.toSort.CloudStorageService;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/rest/file/")
public class FileResource extends AbstractResource {

    private static final String UPLOADED_FILES_DIRECTORY = ConfigurationRepository.getTemporaryUplodadedFiles();
    private final FileDao fileDao;
    private final Double MAXCAPACITY = 0.9;

    public FileResource(JobController jobController) {
        this.fileDao = new FileDao();
        this.jobController = jobController;
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Handles uploaded file from client
     *
     * @param request
     * @return
     * @throws java.lang.InterruptedException
     * @throws com.jcraft.jsch.JSchException
     * @throws com.jcraft.jsch.SftpException
     * @throws java.security.NoSuchAlgorithmException
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleUploadedFile(@MultipartForm UploadRequest request) throws InterruptedException, JSchException, SftpException, NoSuchAlgorithmException {

//        try {
//            LOGGER.info("Upload request received [filename=" + request.getFileInfo().getName() + "]");
//
//            // Writes file on disk
//            String filepath = writeFile(request.getData(), request.getFileInfo().getName(), request.getFileInfo().getUserId());
//
//            // Verify integrity
//            String hashedFile = verifyIntegrity(request.getFileInfo(), filepath);
//
//            // Verify file integrity and tries to write file to Zookeeper
//            if (rpcClient.getProxy().uploadFile(filepath, convertToAvroObject(hashedFile, request.getFileInfo()))) {
//
//                // Copy to data-folder
//                copyFileToDataFolder(filepath, request.getFileInfo().getName());
//
//                // Creates an UserFile using UploadadeFileInfo from request and persists on Database
//                fileDao.persist(request.getFileInfo());
//
//                return Response.status(200).entity(true).build();
//            }
//
//        } catch (IOException e) {
//            LOGGER.error("[IOException] " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        return Response.status(500).entity(false).build();
        LOGGER.info("Upload request received [filename=" + request.getFileInfo().getName() + "]");

        fileDao.persist(request.getFileInfo());

        return Response.status(200).entity(true).build();

    }

    /**
     * Delete a file
     *
     * @param id
     */
    @DELETE
    @Path("/{fileID}")
    public void deleteFile(@PathParam("fileID") String id) {
        LOGGER.info("Delete File Request received. Id=" + id);

        try {
            FileInfo file = fileDao.findByStringId(id);
            BioBucket bucket = CloudStorageService.getBucket(file.getBucket());

            LOGGER.info("File " + file.getName() + " found on Bucket " + file.getBucket());
            
            fileDao.delete(file);
            
            CloudStorageMethods methods_instance = new CloudStorageMethodsV1();

            methods_instance.DeleteFile(bucket, file.getName());

        } catch (Throwable t) {
            LOGGER.error("Exception caught: " + t.getMessage());
            t.printStackTrace();
        }

    }

    /**
     * Used to download a file to the user
     *
     * @param workflowId
     * @param filename
     * @return
     */
    @GET
    @Path("/download/{workflow-id}/{filename}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getFile(@PathParam("workflow-id") String workflowId, @PathParam("filename") String filename) {
        LOGGER.info("Requested donwload of file: " + workflowId + "/" + filename);

        try {
            File file = new File(ConfigurationRepository.getWorkflowOutputFolder(workflowId) + filename);

            ResponseBuilder response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            return response.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return Internal Error (500)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * Save file in disk
     *
     * @param file
     * @throws IOException
     */
    private String writeFile(byte[] content, String filename, long userId) throws IOException {
        String filepath = UPLOADED_FILES_DIRECTORY + filename;
        File file = new File(filepath);

        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fop = new FileOutputStream(file);

        LOGGER.info("File created. [path=" + filepath + "]");

        fop.write(content);
        fop.flush();
        fop.close();

        return filepath;
    }

    /**
     * Verifies a file integrity.
     *
     * @param fileInfo
     * @param filepath
     * @return
     */
    public String verifyIntegrity(FileInfo fileInfo, String filepath) {
        String hashFile = null;

        try {
            hashFile = Hash.calculateSha3(filepath);

            // Verifies generated Hash from server with the hash that came from client
            if (!hashFile.equals(fileInfo.getHash())) {
                return null;
            }
        } catch (IOException ex) {
            LOGGER.error("Error verifing file integrity");
        }

        return hashFile;
    }

    /**
     * Convert from FileInfo to Avro FileInfo.
     *
     * @param hashedFile
     * @param fileInfo
     * @return
     */
    public br.unb.cic.bionimbus.avro.gen.FileInfo convertToAvroObject(String hashedFile, FileInfo fileInfo) {
        try {
            br.unb.cic.bionimbus.avro.gen.FileInfo info = new br.unb.cic.bionimbus.avro.gen.FileInfo();

            info.setHash(hashedFile);
            info.setId(fileInfo.getName());
            info.setName(fileInfo.getName());
            info.setSize(fileInfo.getSize());
            info.setUploadTimestamp(fileInfo.getUploadTimestamp());

            return info;

        } catch (Exception ex) {
            LOGGER.error("Error converting objects");
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * It's needed because next job may need it.
     *
     * @param from
     */
    private String copyFileToDataFolder(String fromPath, String filename) {
        InputStream inStream = null;
        OutputStream outStream = null;

        String outputFilePath = ConfigurationRepository.getDataFolder() + filename;

        File from = null;

        try {

            from = new File(fromPath);
            File to = new File(outputFilePath);

            inStream = new FileInputStream(from);
            outStream = new FileOutputStream(to);

            byte[] buffer = new byte[1024];

            int length;

            // Copy the file content in bytes 
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                inStream.close();
                outStream.close();

                // Delete from tmp/ folder
                from.delete();
            } catch (IOException ex) {
                LOGGER.error("Error closing streams");
                ex.printStackTrace();
            }
        }

        return outputFilePath;
    }
}
