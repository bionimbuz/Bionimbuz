package br.unb.cic.bionimbus.controller.jobcontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.controller.Controller;
import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.model.Job;
import br.unb.cic.bionimbus.model.Log;
import br.unb.cic.bionimbus.model.LogSeverity;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.RepositoryService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;

/**
 * Class that links the User Interface (Web) with the BioNimbuZ Application
 * Core. Controls user access to execute core functions and manages workflows
 * execution
 *
 * @author Vinicius
 */
@Singleton
public class JobController implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);
    private static final int AVRO_PORT = 8080;
    private static RpcClient rpcClient;
    private boolean isConnected = false;

    protected CloudMessageService cms;
    protected BioNimbusConfig config;
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<>();
    private final RepositoryService repositoryService;

    private final WorkflowLoggerDao loggerDao;

    /**
     * Starts JobController execution
     *
     * @param cms
     * @param rs
     */
    @Inject
    public JobController(CloudMessageService cms, RepositoryService rs) {
        Preconditions.checkNotNull(cms);
        this.repositoryService = rs;
        this.cms = cms;
        this.loggerDao = new WorkflowLoggerDao();

        LOGGER.info("JobController started");
    }

    /**
     * Starts JobController
     *
     * @param config
     */
    @Override
    public void start(BioNimbusConfig config) {
        // Sets configuration
        this.config = config;

        // Initializes AvroClient
        rpcClient = new AvroClient("http", config.getAddress(), AVRO_PORT);

        try {
            // Test to see if hostname is reachable
            if (rpcClient.getProxy().ping()) {
                isConnected = true;
            }
        } catch (IOException ex) {
            LOGGER.error("[Exception] " + ex.getMessage());
        }
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verifyPlugins() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void event(WatchedEvent eventType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        LOGGER.info("JobController");
    }

    /**
     * Calls RPC Client to execute a Workflow. It substitutes Java
     * implementation by AVRO implementation
     *
     * @param workflow
     * @throws java.lang.Exception
     */
    public void startWorkflow(Workflow workflow) throws Exception {
        // Logs
        loggerDao.log(new Log("Iniciando a execução do Workflow", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));
        
        List<br.unb.cic.bionimbus.avro.gen.Job> listjob = new ArrayList<>();

        // Iterates over the list of jobs
        for (Job jobInfo : workflow.getJobs()) {

            // Create a new Avro Job
            br.unb.cic.bionimbus.avro.gen.Job job = new br.unb.cic.bionimbus.avro.gen.Job();

            // Sets its fields
            job.setArgs(jobInfo.getArgs());
            job.setId(jobInfo.getId());
            job.setLocalId(config.getHost().getAddress());
            job.setServiceId(jobInfo.getServiceId());
            job.setTimestamp(jobInfo.getTimestamp());
            job.setOutputs(jobInfo.getOutputs());
            job.setDependencies(jobInfo.getDependencies());
            job.setReferenceFile(jobInfo.getReferenceFile());

            // Avro File Info
            ArrayList<br.unb.cic.bionimbus.avro.gen.FileInfo> avroFiles = new ArrayList<>();

            // Iterate over the inputFile list of the job to create AVRO File Info
            for (FileInfo f : jobInfo.getInputFiles()) {
                br.unb.cic.bionimbus.avro.gen.FileInfo file = new br.unb.cic.bionimbus.avro.gen.FileInfo();
                file.setHash(f.getHash());
                file.setId(f.getId());
                file.setName(f.getName());
                file.setUploadTimestamp(f.getUploadTimestamp());
                file.setUserId(f.getUserId());

                // Adds it to the avro file list
                avroFiles.add(file);
            }

            // Sets is input files
            job.setInputFiles(avroFiles);

            // Adds this avro job
            listjob.add(job);
        }

        // Creates Avro Workflow
        br.unb.cic.bionimbus.avro.gen.Workflow avroWorkflow = new br.unb.cic.bionimbus.avro.gen.Workflow();
        avroWorkflow.setId(workflow.getId());
        avroWorkflow.setJobs(listjob);
        avroWorkflow.setCreationDatestamp(workflow.getCreationDatestamp());
        avroWorkflow.setDescription(workflow.getDescription());

        // Logs
        loggerDao.log(new Log("Enviando Workflow para o serviço de Escalonamento do BioNimbuZ", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));
        
        rpcClient.getProxy().startWorkflow(avroWorkflow);

    }

    public void pauseWorkflow(String workflowId) {

    }

    public void workflowStatus(String workflowStatus) {

    }

    /*
     * Methods to implement 
     * - User control: 
     * o void logUser (User user): Keeps a HashMap<id, User> with logged user 
     * o void loggoutUser (User user): Deletes the loggedUsers list 
     * 
     * - Job Control:
     * o ArrayList<JobInfo> listJobs (long userId); 
     * o ArrayList<JobInfo> listJobsByUserId (long userId); 
     * o JobInfo findJobById (String jobId); 
     * o boolean cancelJob (String jobId);
     *
     */
    /**
     * Return BioNimbus configuration
     *
     * @return BioNimbusConfig
     */
    public BioNimbusConfig getConfig() {
        return this.config;
    }

    public RepositoryService getRepositoryService() {
        return this.repositoryService;
    }

}