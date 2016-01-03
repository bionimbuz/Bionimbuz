package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
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
import br.unb.cic.bionimbus.security.AESEncryptor;
import br.unb.cic.bionimbus.security.Hash;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.utils.Nmap;
import br.unb.cic.bionimbus.utils.Put;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.Response;

@Path("/rest/file/")
public class FileResource extends AbstractResource {

    private static final String UPLOADED_FILES_DIRECTORY = FileSystemView.getFileSystemView().getHomeDirectory() + "/zoonimbusProject/uploaded-files/";
    private final FileDao fileDao;
    private List<NodeInfo> pluginList;
    private final Double MAXCAPACITY = 0.9;
    private List<NodeInfo> nodesdisp = new ArrayList<>();

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
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleUploadedFile(@MultipartForm UploadRequest request) {

        try {
            LOGGER.info("Upload request received [filename=" + request.getUploadedFileInfo().getName() + "]");

            // Writes file on disk
            String filepath = writeFile(request.getData(), request.getUploadedFileInfo().getName(), request.getUploadedFileInfo().getUserId().toString());

            // Tries to write file to Zookeeper
            //if (writeFileToZookeeper(filepath)) {

                // Creates an UserFile using UploadadeFileInfo from request and persists on Database
                fileDao.persist(request.getUploadedFileInfo());

            //}

        } catch (IOException e) {
            LOGGER.error("[IOException] " + e.getMessage());
            e.printStackTrace();
        }
//        } catch (InterruptedException | JSchException | SftpException e) {
//            LOGGER.error("[Exception] " + e.getMessage());
//            e.printStackTrace();
//        }

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
    }

    /**
     * Save file in disk
     *
     * @param file
     * @throws IOException
     */
    private String writeFile(byte[] content, String filename, String userId) throws IOException {
        // zoonimbusProject/uploaded-files/{user-id}
        String folderPath = UPLOADED_FILES_DIRECTORY + userId + "/";
        File folder = new File(folderPath);

        // Verifies if the user folder already exists
        if (!folder.exists()) {
            // If not, creates it
            folder.mkdir();
        }

        // zoonimbusProject/uploaded-files/{user-id}/{filename}
        String filepath = folderPath + filename;
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
     * Sends a file to ZooKeeper
     *
     * @param filepath
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws JSchException
     * @throws SftpException
     */
    public boolean writeFileToZookeeper(String filepath) throws IOException, InterruptedException, JSchException, SftpException {
        //Verifica se o arquivo existe         
        File file = new File(filepath);
        AESEncryptor aes = new AESEncryptor();

        if (file.exists()) {
            br.unb.cic.bionimbus.avro.gen.FileInfo info = new br.unb.cic.bionimbus.avro.gen.FileInfo();

            //if (!file.getPath().contains("inputfiles.txt")) {
            //TO-DO: Remove comment after William Final Commit
            //aes.encrypt(path);
            //}
            String hashFile = Hash.calculateSha3(filepath);
            info.setHash(hashFile);
            info.setFileId(file.getName());
            info.setName(file.getName());
            info.setSize(file.length());

            LOGGER.info("Calculating latency");
            pluginList = rpcClient.getProxy().getPeersNode();

            //Insere o arquivo na pasta PENDING SAVE do Zookeeper
            rpcClient.getProxy().setFileInfo(info, "upload!");
            for (Iterator<NodeInfo> it = pluginList.iterator(); it.hasNext();) {
                NodeInfo plugin = it.next();

                //Adiciona na lista de possiveis peers de destino somente os que possuem espaço livre para receber o arquivo
                if ((long) (plugin.getFreesize() * MAXCAPACITY) > info.getSize()) {
                    plugin.setLatency(Ping.calculo(plugin.getAddress()));
                    if (plugin.getLatency().equals(Double.MAX_VALUE)) {
                        plugin.setLatency(Nmap.nmap(plugin.getAddress()));
                    }
                    nodesdisp.add(plugin);
                }
            }

            //Retorna a lista dos nos ordenados como melhores, passando a latência calculada
            nodesdisp = new ArrayList<>(rpcClient.getProxy().callStorage(nodesdisp));

            NodeInfo no = null;
            Iterator<NodeInfo> it = nodesdisp.iterator();
            while (it.hasNext() && no == null) {
                NodeInfo node = (NodeInfo) it.next();

                //Tenta enviar o arquivo a partir do melhor peer que está na lista
                Put conexao = new Put(node.getAddress(), filepath);
                if (conexao.startSession()) {
                    no = node;
                }
            }
            //Conserta o nome do arquivo encriptado
            //TO-DO: Remove comment after William Final Commit
            //aes.setCorrectFilePath(path);
            if (no != null) {
                List<String> dest = new ArrayList<>();
                dest.add(no.getPeerId());
                nodesdisp.remove(no);

                //Envia RPC para o peer em que está conectado, para que ele sete no Zookeeper os dados do arquivo que foi upado.
                rpcClient.getProxy().fileSent(info, dest);

                // File uploaded
                LOGGER.info("File uploaded!");
                return true;
            }

        }

        // Upload error
        LOGGER.error("File not found");
        return false;
    }

}
