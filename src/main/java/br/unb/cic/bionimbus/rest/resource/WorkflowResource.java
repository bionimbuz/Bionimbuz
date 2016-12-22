package br.unb.cic.bionimbus.rest.resource;

import static br.unb.cic.bionimbus.config.BioNimbusConfigLoader.loadHostConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.model.Log;
import br.unb.cic.bionimbus.model.LogSeverity;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.model.WorkflowOutputFile;
import br.unb.cic.bionimbus.model.WorkflowStatus;
import br.unb.cic.bionimbus.persistence.dao.WorkflowDao;
import br.unb.cic.bionimbus.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbus.rest.request.GetWorkflowHistoryRequest;
import br.unb.cic.bionimbus.rest.request.GetWorkflowStatusRequest;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.request.StartWorkflowRequest;
import br.unb.cic.bionimbus.rest.response.GetWorkflowHistoryResponse;
import br.unb.cic.bionimbus.rest.response.GetWorkflowStatusResponse;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;

/**
 * Class that handle sent workflow via REST request
 *
 * @author Vinicius
 */
@Path("/rest")
public class WorkflowResource extends AbstractResource {

    private final WorkflowDao workflowDao;
    private final WorkflowLoggerDao loggerDao;

    public WorkflowResource(JobController jobController) {
        // Creates a RPC Client
        try {
            rpcClient = new AvroClient("http", loadHostConfig(System.getProperty("config.file", "conf/node.yaml")).getAddress(), 8080);
        } catch (IOException ex) {
            LOGGER.error("Error creating RPC Client for PluginTaskRunner");
            ex.printStackTrace();
        }

        this.jobController = jobController;
        this.workflowDao = new WorkflowDao();
        this.loggerDao = new WorkflowLoggerDao();
    }

    /**
     * Handles StartWorkflowRequests by submiting them to the Core
     *
     * @param request
     * @return
     */
    @POST
    @Path("/workflow/start/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startWorkflow(StartWorkflowRequest request) {
        LOGGER.info("New workflow received {id=" + request.getWorkflow().getId()
                + ",jobs=" + request.getWorkflow().getJobs().size()
                + ",userId=" + request.getWorkflow().getUserId()
                + "}");
        LOGGER.info(" INTANSCIAS"+request.getWorkflow().getIntancesWorkflow().toString());
        // Logs
        loggerDao.log(new Log("Workflow chegou no servidor do BioNimbuZ", request.getWorkflow().getUserId(), request.getWorkflow().getId(), LogSeverity.INFO));

        try {
            // Starts it
            jobController.startWorkflow(request.getWorkflow());

            // Sets its status as EXECUTING
            request.getWorkflow().setStatus(WorkflowStatus.EXECUTING);

            // If it gets started with success, persists it on database
            workflowDao.persist(request.getWorkflow());

        } catch (Exception e) {
            // Logs
            loggerDao.log(new Log("Um erro ocorreu na execução de seu Workflow", request.getWorkflow().getUserId(), request.getWorkflow().getId(), LogSeverity.ERROR));
            LOGGER.error("[Exception] " + e.getMessage());

            return Response.status(200).entity(false).build();
        }

        return Response.status(200).entity(true).build();
    }

    /**
     * Verifies the status of an user's workflow
     *
     * @param request
     * @return
     */
    @POST
    @Path("/workflow/status/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GetWorkflowStatusResponse getWorkflowStatus(GetWorkflowStatusRequest request) {
        LOGGER.info("Received GetWorkflowStatus request from userId=" + request.getUserId());

        // Get workflow status by Id
        List<Workflow> workflowList = workflowDao.listByUserId(request.getUserId());

        // Sort it by timestamp
        Collections.sort(workflowList, Workflow.comparator);

        return new GetWorkflowStatusResponse(workflowList);
    }

    @POST
    @Path("/workflow/history/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GetWorkflowHistoryResponse getWorkflowHistory(GetWorkflowHistoryRequest request) {
        LOGGER.info("Received GetWorkflowHistory request for workflowId=" + request.getWorkflowId());

        try {
            // Retrieves from database
            List<Log> logs = loggerDao.listByWorkflowId(request.getWorkflowId());

            // Sort it by timestamp
            Collections.sort(logs, Log.comparator);

            // Gets workflow output files the request workflow id
            List<WorkflowOutputFile> results = loggerDao.listAllOutputFilesByWorkflowId(request.getWorkflowId());

            // Output files
            List<FileInfo> outputs = new ArrayList<>();

            // Verifify if results is empty
//            if (!results.isEmpty()) {
//                for (WorkflowOutputFile result : results) {
//                    PluginFile pluginFile = rpcClient.getProxy().getFileFromPeers(result.getOutputFilename());
//
//                    if (pluginFile != null) {
//                        FileInfo fileInfo = new br.unb.cic.bionimbus.model.FileInfo();
//                        fileInfo.setId(pluginFile.getId());
//                        fileInfo.setName(pluginFile.getName());
//                        fileInfo.setHash(pluginFile.getHash());
//                        fileInfo.setSize(pluginFile.getSize());
//                        fileInfo.setUploadTimestamp("");
//
//                        outputs.add(fileInfo);
//                    }
//                }
//            }

            // Returns to application
            return new GetWorkflowHistoryResponse(logs, results);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new GetWorkflowHistoryResponse();
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
