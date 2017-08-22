package br.unb.cic.bionimbuz.controller.jobcontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import br.unb.cic.bionimbuz.avro.rpc.AvroClient;
import br.unb.cic.bionimbuz.avro.rpc.RpcClient;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.Controller;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.model.Log;
import br.unb.cic.bionimbuz.model.LogSeverity;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;

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
    public void start() {
        // Initializes AvroClient
        rpcClient = new AvroClient("http", BioNimbusConfig.get().getAddress(), AVRO_PORT);

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
        LOGGER.info("Iniciando a execução do Workflow Userid: " + workflow.getUserId() + " workflowId: " + workflow.getId());

        List<br.unb.cic.bionimbuz.avro.gen.Job> listjob = new ArrayList<>();
        List<br.unb.cic.bionimbuz.avro.gen.Instance> listIntanceMachine = new ArrayList<>();
        List<br.unb.cic.bionimbuz.avro.gen.Prediction> predict = new ArrayList<>();
        br.unb.cic.bionimbuz.avro.gen.Sla slaworkflow = new br.unb.cic.bionimbuz.avro.gen.Sla();

        //Iterates over WorkflowInstances, and add on list Avro Instance        
        for (Instance i : workflow.getIntancesWorkflow()) {
            br.unb.cic.bionimbuz.avro.gen.Instance ins = new br.unb.cic.bionimbuz.avro.gen.Instance();
            ins.setId(i.getId());
            ins.setType(i.getType());
            ins.setCostPerHour(i.getCostPerHour());
            ins.setLocality(i.getLocality());
            ins.setMemoryTotal(i.getMemoryTotal());
            ins.setCpuHtz(i.getCpuHtz());
            ins.setCpuType(i.getCpuType());
            ins.setNumCores(i.getNumCores());
            ins.setDescription(i.getDescription());
            ins.setProvider(i.getProvider());
            ins.setIdProgramas(i.getidProgramas());
            ins.setCreationTimer(i.getCreationTimer());
            ins.setDelay(i.getDelay());
            ins.setTimetocreate(i.getTimetocreate());
            ins.setIdUser(i.getIdUser());
            ins.setIp(i.getIp());
            listIntanceMachine.add(ins);
        }

        //Set Avro User
        br.unb.cic.bionimbuz.avro.gen.User userAvro = new br.unb.cic.bionimbuz.avro.gen.User();
        userAvro.setId(workflow.getUserWorkflow().getId());
        userAvro.setLogin(workflow.getUserWorkflow().getLogin());
        userAvro.setNome(workflow.getUserWorkflow().getNome());
        userAvro.setCpf(workflow.getUserWorkflow().getCpf());
        userAvro.setEmail(workflow.getUserWorkflow().getEmail());
        userAvro.setCelphone(workflow.getUserWorkflow().getCelphone());
        //set avro instance list to user
        userAvro.setInstances(listIntanceMachine);

        // Iterates over the list of jobs
        for (Job jobInfo : workflow.getJobs()) {
            // Create a new Avro Job
            br.unb.cic.bionimbuz.avro.gen.Job job = new br.unb.cic.bionimbuz.avro.gen.Job();
            // Sets its fields
            job.setArgs(jobInfo.getArgs());
            job.setId(jobInfo.getId());
            job.setLocalId(BioNimbusConfig.get().getHost().getAddress());
            job.setServiceId(jobInfo.getServiceId());
            job.setTimestamp(jobInfo.getTimestamp());
            job.setOutputs(jobInfo.getOutputs());
            job.setDependencies(jobInfo.getDependencies());
            job.setReferenceFile(jobInfo.getReferenceFile());
            job.setIpjob(jobInfo.getIpjob());

            // Avro File Info
            ArrayList<br.unb.cic.bionimbuz.avro.gen.FileInfo> avroFiles = new ArrayList<>();

            // Iterate over the inputFile list of the job to create AVRO File Info
            for (FileInfo f : jobInfo.getInputFiles()) {
                br.unb.cic.bionimbuz.avro.gen.FileInfo file = new br.unb.cic.bionimbuz.avro.gen.FileInfo();
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
        if (workflow.getSla().getPrediction()) {
            for (br.unb.cic.bionimbuz.model.Prediction p : workflow.getSla().getSolutions()) {
                br.unb.cic.bionimbuz.avro.gen.Prediction pAvro = new br.unb.cic.bionimbuz.avro.gen.Prediction();
                pAvro.setCustoService(p.getCustoService());
                pAvro.setId(p.getId());
                pAvro.setIdService(p.getIdService());
                br.unb.cic.bionimbuz.avro.gen.Instance ins = new br.unb.cic.bionimbuz.avro.gen.Instance();
                ins.setId(p.getInstance().getId());
                ins.setType(p.getInstance().getType());
                ins.setCostPerHour(p.getInstance().getCostPerHour());
                ins.setLocality(p.getInstance().getLocality());
                ins.setMemoryTotal(p.getInstance().getMemoryTotal());
                ins.setCpuHtz(p.getInstance().getCpuHtz());
                ins.setCpuType(p.getInstance().getCpuType());
                ins.setNumCores(p.getInstance().getNumCores());
                ins.setDescription(p.getInstance().getDescription());
                ins.setProvider(p.getInstance().getProvider());
                ins.setIdProgramas(p.getInstance().getidProgramas());
                ins.setCreationTimer(p.getInstance().getCreationTimer());
                ins.setDelay(p.getInstance().getDelay());
                ins.setTimetocreate(p.getInstance().getTimetocreate());
                ins.setIdUser(p.getInstance().getIdUser());
                ins.setIp(p.getInstance().getIp());
                pAvro.setInstance(ins);
                pAvro.setTimeService(p.getTimeService());
                predict.add(pAvro);
            }
        } else {
            br.unb.cic.bionimbuz.avro.gen.Prediction pAvro = new br.unb.cic.bionimbuz.avro.gen.Prediction();
            pAvro.setCustoService(0d);
            pAvro.setId("");
            pAvro.setIdService("");
            br.unb.cic.bionimbuz.avro.gen.Instance ins = new br.unb.cic.bionimbuz.avro.gen.Instance();
            ins.setId("");
            ins.setType("");
            ins.setCostPerHour(0d);
            ins.setLocality("");
            ins.setMemoryTotal(0d);
            ins.setCpuHtz(0d);
            ins.setCpuType("");
            ins.setNumCores(0);
            ins.setDescription("");
            ins.setProvider("");
            ins.setIdProgramas(new ArrayList<>(Arrays.asList("")));
            ins.setCreationTimer(0l);
            ins.setDelay(0);
            ins.setTimetocreate(0l);
            ins.setIdUser(0l);
            ins.setIp("");
            pAvro.setInstance(ins);
            pAvro.setTimeService(0l);
            predict.add(pAvro);
        }

        //Create Avro Sla 
        slaworkflow.setId(workflow.getSla().getId());
        slaworkflow.setIdWorkflow(workflow.getId());
        slaworkflow.setProvider(workflow.getSla().getProvider());
        slaworkflow.setPrediction(workflow.getSla().getPrediction());
        slaworkflow.setSolutions(predict);
  
        //prevents null
        slaworkflow.setObjective(-1);
        slaworkflow.setLimitationValueExecutionCost(-1.0);
        slaworkflow.setLimitationValueExecutionTime(-1l);
        slaworkflow.setLimitationType(-1);
        
        if (workflow.getSla().getObjective() != null) 
            slaworkflow.setObjective(workflow.getSla().getObjective());
        if (workflow.getSla().getLimitationExecution()) {
            slaworkflow.setLimitationType(workflow.getSla().getLimitationType());
            //0 = time | 1 = cust
            if (workflow.getSla().getLimitationType() == 0) {
                slaworkflow.setLimitationValueExecutionTime(workflow.getSla().getLimitationValueExecutionTime());
            } else if (workflow.getSla().getLimitationType() == 1) {
                slaworkflow.setLimitationValueExecutionCost(workflow.getSla().getLimitationValueExecutionCost());
            }
        }
        slaworkflow.setPeriod(workflow.getSla().getPeriod());
        slaworkflow.setValue(workflow.getSla().getValue());
        if(workflow.getSla().getExeceedValueExecutionCost()==null)
            workflow.getSla().setExeceedValueExecutionCost(0D);
        slaworkflow.setExeceedValueExecutionCost(workflow.getSla().getExeceedValueExecutionCost());
        slaworkflow.setLimitationExecution(workflow.getSla().getLimitationExecution());

        // Creates Avro Workflow
        br.unb.cic.bionimbuz.avro.gen.Workflow avroWorkflow = new br.unb.cic.bionimbuz.avro.gen.Workflow();
        avroWorkflow.setId(workflow.getId());
        avroWorkflow.setJobs(listjob);
        avroWorkflow.setCreationDatestamp(workflow.getCreationDatestamp());
        avroWorkflow.setDescription(workflow.getDescription());
        avroWorkflow.setIntancesWorkflow(listIntanceMachine);
        avroWorkflow.setUserWorkflow(userAvro);
        avroWorkflow.setSla(slaworkflow);

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

    public RepositoryService getRepositoryService() {
        return this.repositoryService;
    }

}
