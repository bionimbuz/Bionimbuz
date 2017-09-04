/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2017 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.rest.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.unb.cic.bionimbuz.avro.rpc.AvroClient;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.model.Log;
import br.unb.cic.bionimbuz.model.LogSeverity;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.model.WorkflowOutputFile;
import br.unb.cic.bionimbuz.model.WorkflowStatus;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowDao;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbuz.rest.request.GetWorkflowHistoryRequest;
import br.unb.cic.bionimbuz.rest.request.GetWorkflowStatusRequest;
import br.unb.cic.bionimbuz.rest.request.RequestInfo;
import br.unb.cic.bionimbuz.rest.request.StartWorkflowRequest;
import br.unb.cic.bionimbuz.rest.response.GetWorkflowHistoryResponse;
import br.unb.cic.bionimbuz.rest.response.GetWorkflowStatusResponse;
import br.unb.cic.bionimbuz.rest.response.ResponseInfo;

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
        rpcClient = new AvroClient("http", BioNimbusConfig.get().getAddress(), 8080);
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
        LOGGER.info("New workflow received {id = " + request.getWorkflow().getId() + ", jobs = " + request.getWorkflow().getJobs().size() + ", userId = " + request.getWorkflow().getUserId() + "}");
        LOGGER.info(" INSTANCES = " + request.getWorkflow().getIntancesWorkflow().toString());
        LOGGER.info(" USER = " + request.getWorkflow().getUserWorkflow().getNome());
        // Logs
        this.loggerDao.log(new Log("Workflow chegou no servidor do BioNimbuZ", request.getWorkflow().getUserId(), request.getWorkflow().getId(), LogSeverity.INFO));

        // request.getWorkflow().getSla().setIdWorkflow(request.getWorkflow().getId());

        try {
            // Starts it
            this.jobController.startWorkflow(request.getWorkflow());
            // slaController.startSla(request.getSla(),request.getWorkflow());
            // Sets its status as EXECUTING
            request.getWorkflow().setStatus(WorkflowStatus.EXECUTING);

            // If it gets started with success, persists it on database
            this.workflowDao.persist(request.getWorkflow());

        } catch (final Exception e) {
            // Logs
            this.loggerDao.log(new Log("Um erro ocorreu na execução de seu Workflow", request.getWorkflow().getUserId(), request.getWorkflow().getId(), LogSeverity.ERROR));
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
        final List<Workflow> workflowList = this.workflowDao.listByUserId(request.getUserId());

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
            final List<Log> logs = this.loggerDao.listByWorkflowId(request.getWorkflowId());

            // Sort it by timestamp
            Collections.sort(logs, Log.comparator);

            // Gets workflow output files the request workflow id
            final List<WorkflowOutputFile> results = this.loggerDao.listAllOutputFilesByWorkflowId(request.getWorkflowId());

            // Output files
            final List<FileInfo> outputs = new ArrayList<>();

            // Verifify if results is empty
            // if (!results.isEmpty()) {
            // for (WorkflowOutputFile result : results) {
            // PluginFile pluginFile = rpcClient.getProxy().getFileFromPeers(result.getOutputFilename());
            //
            // if (pluginFile != null) {
            // FileInfo fileInfo = new br.unb.cic.bionimbus.model.FileInfo();
            // fileInfo.setId(pluginFile.getId());
            // fileInfo.setName(pluginFile.getName());
            // fileInfo.setHash(pluginFile.getHash());
            // fileInfo.setSize(pluginFile.getSize());
            // fileInfo.setUploadTimestamp("");
            //
            // outputs.add(fileInfo);
            // }
            // }
            // }

            // Returns to application
            return new GetWorkflowHistoryResponse(logs, results);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return new GetWorkflowHistoryResponse();
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
    }

}
