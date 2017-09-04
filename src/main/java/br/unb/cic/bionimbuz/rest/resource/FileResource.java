package br.unb.cic.bionimbuz.rest.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.config.ConfigurationRepository;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.persistence.dao.FileDao;
import br.unb.cic.bionimbuz.rest.request.RequestInfo;
import br.unb.cic.bionimbuz.rest.request.UploadRequest;
import br.unb.cic.bionimbuz.rest.response.ResponseInfo;
import br.unb.cic.bionimbuz.security.HashUtil;
import br.unb.cic.bionimbuz.services.storage.bucket.BioBucket;
import br.unb.cic.bionimbuz.services.storage.bucket.CloudStorageMethods;
import br.unb.cic.bionimbuz.services.storage.bucket.CloudStorageService;
import br.unb.cic.bionimbuz.services.storage.bucket.methods.CloudMethodsAmazonGoogle;

@Path("/rest/file/")
public class FileResource extends AbstractResource {

    private final FileDao fileDao;

    public FileResource(final JobController jobController) {
        this.fileDao = new FileDao();
        this.jobController = jobController;
    }

    @Override
    public ResponseInfo handleIncoming(final RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Handles uploaded file from client
     *
     * @param form
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
    public Response handleUploadedFile(@MultipartForm UploadRequest form) throws InterruptedException, JSchException, SftpException, NoSuchAlgorithmException {

        LOGGER.info("Upload request received [filename=" + form.getFileInfo().getName() + "]");

        try {
            // Writes file on disk

            if (BioNimbusConfig.get().getStorageMode().equalsIgnoreCase("0")) {
                final String filepath = this.writeFile(form.getData(), form.getFileInfo().getName(), form.getFileInfo().getUserId());
                // Verify integrity
                final String hashedFile = verifyIntegrity(form.getFileInfo(), filepath);
                // Verify file integrity and tries to write file to Zookeeper
                if (rpcClient.getProxy().uploadFile(filepath, this.convertToAvroObject(hashedFile, form.getFileInfo()))) {
                    // Copy to data-folder
                    this.copyFileToDataFolder(filepath, form.getFileInfo().getName());
                }

            } else {
                // final CloudStorageMethods methodsInstance = new CloudMethodsAmazonGoogle();
                final BioBucket dest = CloudStorageService.getBestBucket(CloudStorageService.getBucketList());
                Response.accepted(dest.getName());
                form.setBucketName(dest.getName());
                // System.out.println("nome:"+dest.getName());
                this.fileDao.persist(form.getFileInfo());
                return Response.ok(form.getBucketName(), MediaType.TEXT_PLAIN).build();
                // temp.delete();
            }
            // Creates an UserFile using UploadadeFileInfo from request and persists on Database
            this.fileDao.persist(form.getFileInfo());
            return Response.status(HttpStatus.SC_OK).entity(true).build();
            // JSONObject n = new JSONObject().put("name", "testandoBucket");
            // return Response.ok(n, MediaType.APPLICATION_JSON).build();
            // return Response.ok("testandoBucket", MediaType.TEXT_PLAIN).build();
        } catch (final Throwable t) {
            LOGGER.error("[Exception] ", t.getMessage());
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(false).build();
        }
    }

    /**
     * Delete a file
     *
     * @param id
     */
    @DELETE
    @Path("/{fileID}")
    public void deleteFile(@PathParam("fileID") final String id) {
        LOGGER.info("Delete File Request received. Id=" + id);

        try {
            final FileInfo file = this.fileDao.findByStringId(id);

            if (BioNimbusConfig.get().getStorageMode().equalsIgnoreCase("1")) {
                final BioBucket bucket = CloudStorageService.getBucket(file.getBucket());

                LOGGER.info("File " + file.getName() + " found on Bucket " + file.getBucket());

                final CloudStorageMethods methods_instance = new CloudMethodsAmazonGoogle();

                methods_instance.DeleteFile(bucket, file.getName());

                // TODO also delete from data-folder
            }

            this.fileDao.delete(file);

        } catch (final Throwable t) {
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
    public Response getFile(@PathParam("workflow-id") final String workflowId, @PathParam("filename") final String filename) {
        LOGGER.info("Requested donwload of file: " + workflowId + "/" + filename);

        try {
            final File file = new File(ConfigurationRepository.getWorkflowOutputFolder(workflowId) + filename);

            final ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            return response.build();
        } catch (final Exception e) {
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
    private String writeFile(final InputStream inputStream, final String filename, final long userId) throws IOException {
        final File file = new File(BioNimbusConfig.get().getDataFolder() + filename);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();

        final String absolutePath = file.getAbsolutePath();
        try (
             final FileOutputStream fileOutputStream = new FileOutputStream(file);) {
            int read = 0;
            final byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
            LOGGER.info("File created. [path=" + absolutePath + "]");
        }
        return absolutePath;
    }

    /**
     * Verifies a file integrity.
     *
     * @param fileInfo
     * @param filepath
     * @return
     * @throws InterruptedException
     */
    public static String verifyIntegrity(final FileInfo fileInfo, final String filepath) {
        try {
            final String computedHash = HashUtil.computeNativeSHA3(filepath);
            // Verifies generated Hash from server with the hash that came from client
            if (computedHash.equals(fileInfo.getHash())) {
                return computedHash;
            }
        } catch (final InterruptedException | IOException e) {
            LOGGER.error("Error verifing file hash integrity", e);
        }
        return null;
    }

    /**
     * Convert from FileInfo to Avro FileInfo.
     *
     * @param hashedFile
     * @param fileInfo
     * @return
     */
    public br.unb.cic.bionimbuz.avro.gen.FileInfo convertToAvroObject(final String hashedFile, final FileInfo fileInfo) {
        try {
            final br.unb.cic.bionimbuz.avro.gen.FileInfo info = new br.unb.cic.bionimbuz.avro.gen.FileInfo();

            info.setHash(hashedFile);
            info.setId(fileInfo.getName());
            info.setName(fileInfo.getName());
            info.setSize(fileInfo.getSize());
            info.setUploadTimestamp(fileInfo.getUploadTimestamp());

            return info;

        } catch (final Exception e) {
            LOGGER.error("Error converting objects", e);
        }

        return null;
    }

    /**
     * It's needed because next job may need it.
     *
     * @param from
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void copyFileToDataFolder(final String fromPath, final String filename) throws FileNotFoundException, IOException {

        final String toPath = BioNimbusConfig.get().getDataFolder() + filename;
        final File to = new File(toPath + ".part");
        final File from = new File(fromPath);
        try (
             InputStream inStream = new FileInputStream(from);
             OutputStream outStream = new FileOutputStream(to);) {

            final byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
        } finally {
            from.delete();
            to.renameTo(new File(toPath));
        }
    }
}
