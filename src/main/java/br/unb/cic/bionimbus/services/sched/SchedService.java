/*
 BioNimbuZ is a federated cloud platform.
 Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
 Department of Computer Science, University of Brasilia, Brazil

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbus.services.sched;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.model.Job;
import br.unb.cic.bionimbus.model.Log;
import br.unb.cic.bionimbus.model.LogSeverity;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskState;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.RepositoryService;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.services.storage.bucket.BioBucket;
import br.unb.cic.bionimbus.services.storage.bucket.CloudStorageMethods;
import br.unb.cic.bionimbus.services.storage.bucket.CloudStorageService;
import br.unb.cic.bionimbus.services.storage.bucket.methods.CloudMethodsAmazonGoogle;
import br.unb.cic.bionimbus.toSort.Listeners;
import br.unb.cic.bionimbus.utils.Get;
import br.unb.cic.bionimbus.utils.Pair;

@Singleton
public class SchedService extends AbstractBioService implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedService.class);
    private final ConcurrentHashMap<String, PluginInfo> cloudMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService schedExecService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("SchedService-%d").build());
    private final Queue<PluginTask> relocateTasks = new ConcurrentLinkedQueue<>();
    private final List<Job> pendingJobs = new ArrayList<>();
    private final List<Job> dependentJobs = new ArrayList<>();
    private final Map<String, Pair<PluginInfo, PluginTask>> waitingTask = new ConcurrentHashMap<>();
    private Map<String, PluginFile> mapFilesPlugin;
    private final Map<String, Job> jobsWithNoService = new ConcurrentHashMap<>();
//    private final Queue<PluginTask> runningJobs = new ConcurrentLinkedQueue<PluginTask>();
    private final Map<String, PluginInfo> cancelingJobs = new ConcurrentHashMap<>();
    private RpcClient rpcClient;

    // change this to select scheduling policy
    private final SchedPolicy.Policy policy = SchedPolicy.Policy.C99SUPERCOLIDER;
    private String idPlugin;

    private LinuxPlugin myLinuxPlugin;
    private SchedPolicy schedPolicy;

    // Workflow information logger
    private final WorkflowLoggerDao workflowLogger;

    // Workflow
    private Workflow workflow;

    private boolean isClient = true;

    @Inject
    public SchedService(final CloudMessageService cms, final RepositoryService rs) {
        Preconditions.checkNotNull(cms);
        Preconditions.checkNotNull(rs);
        this.cms = cms;
        this.rs = rs;

        // Initializes workflow logger
        workflowLogger = new WorkflowLoggerDao();
    }

    public synchronized SchedPolicy getPolicy() {
        if (schedPolicy == null) {
            schedPolicy = SchedPolicy.getInstance(policy, cloudMap);
        }

        schedPolicy.setCloudMap(cloudMap);
        return schedPolicy;
    }

    /**
     * Altera a política de escalonamento para executar os jobs.
     */
    private void setPolicy() {
//        try {
//        String dataPolicy = cms.getData(JOBS.toString(), null);
//        if (dataPolicy != null && !dataPolicy.isEmpty()) {
//            schedPolicy = SchedPolicy.getInstance(new Integer(dataPolicy), cloudMap);
//        }
//        } catch (KeeperException ex) {
//            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//        }

        throw new UnsupportedOperationException("METHOD COMMENTED");
    }

    @Override
    public void run() {
        checkTasks();

//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            PluginFile pfile = mapper.readValue(cms.getData("/bionimbuz/buckets/bionimbuz-g-us/files/mclovin.png", null), PluginFile.class);
//            
//            FileInfo ifile = new FileInfo();
//            ifile.setId(pfile.getId());
//            ifile.setName(pfile.getName());
//            List<FileInfo> request = new ArrayList<>();
//            request.add(ifile);
//            
//            checkFilesPlugin();
//            requestFile(request);
//            
//        } catch (Throwable t) {
//            LOGGER.error("[SchedService] Exception(run): " + t.getMessage());
//            t.printStackTrace();
//        }
//        LOGGER.debug("[SchedService] File requested");
    }

    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {

        this.isClient = config.isClient();
        this.config = config;
        this.listeners = listeners;
//        if (listeners != null) {
        listeners.add(this);
//        }
        idPlugin = this.config.getId();

        getPolicy().setRs(rs);

        //inicia o valor do zk na politica de escalonamento
        getPolicy().setCms(cms);

        if (!cms.getZNodeExist(Path.PIPELINES.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.PIPELINES.getFullPath(), policy.toString());
        }
        cms.createZNode(CreateMode.PERSISTENT, Path.SCHED.getFullPath(idPlugin), null);
        cms.createZNode(CreateMode.PERSISTENT, Path.TASKS.getFullPath(idPlugin), null);
        cms.createZNode(CreateMode.PERSISTENT, Path.SIZE_JOBS.getFullPath(idPlugin), null);

        cloudMap.putAll(getPeers());
        try {
            //adicona watchers para receber um alerta quando um novo job for criado para ser escalonado, e uma nova requisição de latência existir
            cms.getChildren(Path.PIPELINES.getFullPath(), new UpdatePeerData(cms, this));
            cms.getData(Path.PIPELINES.getFullPath(), new UpdatePeerData(cms, this));
            cms.getChildren(Path.PEERS.getFullPath(), new UpdatePeerData(cms, this));

            checkMyPlugin();
            checkWaitingTasks();
            checkPeers();

        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }

        schedExecService.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
        //schedExecService.scheduleAtFixedRate(this, 1, 5, TimeUnit.MINUTES);
    }

    /**
     * Executa a rotina de escalonamento, após o zookeeper disparar um aviso que
     * um novo job foi criado para ser escalonado.
     */
    private synchronized void scheduleJobs() throws InterruptedException, KeeperException {
        HashMap<Job, PluginInfo> schedMap;

        // Caso nao exista nenhum pipeline pendente da a chance para o escalonador
        // realocar as tarefas.
        if (!pendingJobs.isEmpty()) {
            // Log if workflow not null
            if (workflow != null) {
                workflowLogger.log(new Log("Job recebido pelo Serviço de Ecalonamento", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));
            }

            cloudMap.clear();
            cloudMap.putAll(getPeers());
            //realiza a requisicao dos valores da lantência antes de escalonar um job

            // sched all pending jobs
            schedMap = getPolicy().schedule(pendingJobs);

            for (Map.Entry<Job, PluginInfo> entry : schedMap.entrySet()) {
                Job jobInfo = entry.getKey();

                PluginInfo pluginInfo = entry.getValue();
                PluginTask task = new PluginTask();
                task.setJobInfo(jobInfo);
                if (pluginInfo != null) {

                    task.setState(PluginTaskState.PENDING);
                    task.setPluginExec(pluginInfo.getId());

                    //adiciona o job na lista de execução do servidor zookeeper
                    cms.createZNode(CreateMode.PERSISTENT, Path.NODE_TASK.getFullPath(task.getPluginExec(), jobInfo.getId()), task.toString());

                    //retira o pipeline da lista de pipelines para escanolamento no zookeeper
                    // cms.delete(cms.getPath().NODE_PIPELINE.getFullPath(pipeline.getId()));
                    //retira o pipelineda lista de jobs para escalonamento
                    pendingJobs.remove(jobInfo);

                    //adiciona a lista de jobs que aguardam execução
                    waitingTask.put(task.getId(), new Pair<>(pluginInfo, task));

                    // Log it
                    workflowLogger.log(new Log("Job <b>" + jobInfo.getId() + "</b> com arquivo de saida <b>"
                            + jobInfo.getOutputs() + "</b> enviado para nó de processamento do BioNimbuZ",
                            workflow.getUserId(), workflow.getId(), LogSeverity.INFO));

                    // Log all output files of a given workflow id
                    workflowLogger.logOutputFile(workflow.getId(), jobInfo.getOutputs());
                } else {
                    LOGGER.info("JobID: " + jobInfo.getId() + " não escalonado");
                }
            }
            //chamada recursiva para escalonar todos os jobs enviados, só é chamada após um
            scheduleJobs();

        }
    }

    /**
     *
     * @param job
     */
    public void setLatencyPlugins(Job job) {

        HashMap<String, Double> pluginIdLatency = new HashMap<>();
        try {
//            if(!cms.getZNodeExist(cms.getPath().PREFIX_JOB.getFullPath("", "", job.getId())+LATENCY, false)) {
//                for (PluginInfo plugin : getPeers().values()) {
//                    //calcula a latencia e regula para segundos
//                    pluginIdLatency.put(plugin.getId(),Ping.calculo(plugin.getHost().getAddress())/1000);
//
//                }
//                cms.createZNode(CreateMode.PERSISTENT, cms.getPath().PREFIX_JOB.getFullPath("", "", job.getId())+LATENCY, new ObjectMapper().writeValueAsString(pluginIdLatency));
//            }
            throw new IOException("REMOVE COMMENTS");

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Rotinas para auxiliar escalonamento,chamado caso seja necessário cancelar
     * um job. Não remove job em execução.
     *
     * OBS: não utilizado
     *
     * @param jobId
     */
    public void cancelJob(String jobId) {
//        if (getPendingJobs().containsKey(jobId)) {
//            getPendingJobs().remove(jobId);
//            //excluir o job do zookeeper TO DO
//        } else if (waitingTask.containsKey(jobId)) {
//            waitingTask.remove(jobId);
//        }
////        try {
//        cms.delete(JOBS.toString() + PREFIX_JOB.toString() + jobId);
//        
////        } catch (KeeperException ex) {
////            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (InterruptedException ex) {
////            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
////        }
    }

    /**
     * Remove a tarefa da lista de jobs cancelados.Job permanece na lista de
     * jobs a serem escalonados.
     *
     * @param task
     */
    private synchronized void finishCancelJob(PluginTask task) {
//        getCancelingJobs().remove(task.getId());
//        System.out.println("Task canceled " + task.getId());
//        
//        relocateTasks.add(task);
//        // Verifica se todas as requisicoes de cancelamento foram realizadas.
//        // TODO: Provavelmente se o usuario cancelar o job na mao essa
//        // funcao vai buggar. Mas dado o tempo que temos acho que eh a melhor
//        // solucao.
    }

    /**
     * Realiza a requisição do(s) arquivo(s) que não existe(m) no plugin.
     *
     * @param listFiles lista de arquivos que devem conter no plugin
     */
    private void requestFile(List<FileInfo> listFiles) {
        
        for (FileInfo info : listFiles) {

            if (!mapFilesPlugin.containsKey(info.getName())) {

                LOGGER.debug("[SchedService] Requesting file: " + info.getName());
                LOGGER.debug("[SchedService] Trying on the CloudStorage Buckets");
                
                if (config.getStorageMode().equalsIgnoreCase("1")) {
                
                    CloudStorageService cloud_service = new CloudStorageService(cms);
                    BioBucket bucket = cloud_service.findFile(info);

                    if (bucket != null) {
                        LOGGER.debug("[SchedService] File found on bucket: " + bucket.getName());

                        if (CloudStorageService.checkMode(bucket)) {

                            LOGGER.debug("[SchedService] Will execute on mounted-mode");
                            PluginFile file = new PluginFile();

                            file.setId(info.getId());
                            file.setName(info.getName());

                            String path = bucket.getMountPoint() + "/data-folder/" + info.getName();
                            file.setPath(path);

                            mapFilesPlugin.put(info.getName(), file);

                            info.setBucket(bucket.getName());

                        } else {

                            LOGGER.debug("[SchedService] Will execute on normal-mode (download file first)");
                            CloudStorageMethods cloud_methods = new CloudMethodsAmazonGoogle();

                            try {
                                cloud_methods.StorageDownloadFile(bucket, "/data-folder/", config.getDataFolder(), info.getName());
                            } catch (Throwable t) {
                                LOGGER.error("[SchedService] Exception(requestFile): " + t.getMessage());
                                t.printStackTrace();
                            }
                        }

                    }
                    
                } else { // Try old storage method
                    LOGGER.debug("[SchedService] Trying on the instances");
                    String ipContainsFile = getFilesIP(info.getName());

                    LOGGER.debug("[SchedService] ipContainsFile: " + ipContainsFile);

                    Get conexao = new Get();
                    try {
                        conexao.startSession(info.getName(), ipContainsFile);
                    } catch (JSchException ex) {
                        java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SftpException ex) {
                        java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }
        checkFilesPlugin();
    }

    /**
     * Recebe uma lista de PluginsTasks para serem relançadas ao escalonamento.
     *
     * @param tasks lista das tarefas
     * @throws org.apache.zookeeper.KeeperException
     * @throws java.lang.InterruptedException
     */
    public void relocateTasks(Collection<PluginTask> tasks) throws KeeperException, InterruptedException {
        relocateTasks.addAll(tasks);

        //Adiciona os jobs cancelados a lista de jobs a serem escalonados no servidor zookeeper
        while (!relocateTasks.isEmpty()) {
            PluginTask task = relocateTasks.remove();
            try {
//                cms.createZNode(CreateMode.PERSISTENT, JOBS + PREFIX_JOB + task.getJobInfo().getId(), task.getJobInfo().toString());
                throw new KeeperException.UnimplementedException();

            } catch (Exception ex) {

//                StringBuilder datas = new StringBuilder(cms.getData(cms.getPath().STATUSWAITING.getFullPath(task.getPluginExec(), "", ""), null));
//                cms.setData(cms.getPath().STATUSWAITING.getFullPath(task.getPluginExec(), "", ""), datas.append("E").toString());
                LOGGER.info("[SchedService] COMMENTED cms.setData");
                tasks.add(task);
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Realiza a recuperação das tarefas que estavam em execução no peer que
     * ficou off-line, colocando-as para serem reescalonadas. Ao final exclui o
     * zNode STATUSWAITING para que o peer possa ser excluído do servidor
     * zookeeper
     *
     * @param peerPath
     */
    private void repairTask(String peerPath) {
        Collection<PluginTask> repairTasks = new LinkedList<>();
        try {
            StringBuilder dataStatus = new StringBuilder();

            if (!cms.getZNodeExist(peerPath + Path.STATUSWAITING, null)) {
                cms.createZNode(CreateMode.PERSISTENT, peerPath + Path.STATUSWAITING, "");
            }

            dataStatus.append(cms.getData(peerPath + Path.STATUSWAITING, null));

            //verifica se recurso já foi recuperado ou está sendo recuperado por outro recurso
            if (dataStatus.toString().contains("E") || dataStatus.toString().contains("B")) {
                return;
            }

            //bloqueio para recuperar tarefas sem que outros recursos realizem a mesma operação
            cms.setData(peerPath + Path.STATUSWAITING, dataStatus.append("B").toString());

            List<String> tasksChildren = cms.getChildren(peerPath + Path.SCHED + Path.TASKS, null);

            for (String taskChild : tasksChildren) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(cms.getData(peerPath + Path.SCHED + Path.TASKS + "/" + taskChild, null), PluginTask.class);
                if (pluginTask.getState() != PluginTaskState.DONE && pluginTask.getState() != PluginTaskState.ERRO) {
                    repairTasks.add(pluginTask);
                }
            }
            relocateTasks(repairTasks);

            //retira bloqueio de uso e sinaliza que as tarefas do plugin foram recuperadas
            dataStatus.deleteCharAt(dataStatus.indexOf("B")).toString();
            cms.setData(peerPath + Path.STATUSWAITING, dataStatus.append("E").toString());

        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Verifica qual é o Plugin referente ao mesmo do recurso.
     */
    private void checkMyPlugin() {
        for (Listeners listener : listeners) {
            if (listener instanceof LinuxPlugin) {
                this.myLinuxPlugin = (LinuxPlugin) listener;
            }
        }
    }

    /**
     * Realiza a verificação dos peers existentes identificando se existe algum
     * peer aguardando recuperação, se o peer estiver off-line e a recuperação
     * já estiver sido feito, realiza a limpeza do peer. Realizada apenas quando
     * o módulo inicia.
     */
    private void checkPeers() {
//        try {
        List<String> listPeers = cms.getChildren(Path.PEERS.getFullPath(), null);
        for (String peerId : listPeers) {

            if (!cms.getZNodeExist(Path.STATUS.getFullPath(peerId), null)
                    && !cms.getZNodeExist(Path.STATUSWAITING.getFullPath(peerId), null)) {
                cms.createZNode(CreateMode.PERSISTENT, Path.STATUSWAITING.getFullPath(peerId), "");
            }
            if (cms.getZNodeExist(Path.STATUSWAITING.getFullPath(peerId), null)) {
                if (!cms.getData(Path.STATUSWAITING.getFullPath(peerId), null).contains("E")) {
                    repairTask(Path.NODE_PEER.getFullPath(peerId));
                }
            }

        }
//        } catch (KeeperException ex) {
//            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * Verifica quais são as tarefas que já estão escanoladas e adiciona um
     * watcher em cada conjunto de tarefas dos plugins e nas tarefas caso
     * existam. Método é chamado ao iniciar o módulo de escalonamento.
     *
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    private void checkWaitingTasks() throws KeeperException, InterruptedException, IOException {
        List<PluginInfo> plgs = new ArrayList<>(getPeers().values());
        List<String> listTasks;
        Watcher watcher;
        LOGGER.info("[SchedService] Checking waiting tasks");

        for (PluginInfo plugin : plgs) {

            //cria watch para ser adicionado no znode que contém as tarefas escanoladas desse plugin
            if (myLinuxPlugin.getMyInfo().getId().equals(plugin.getId())) {
                LOGGER.info("[SchedService] checkWaitingTasks : watcher adicionado no plugin " + plugin.getId());
                watcher = new UpdatePeerData(cms, this);
            } else {
                watcher = null;
            }
            listTasks = cms.getChildren(Path.TASKS.getFullPath(plugin.getId()), watcher);

            for (String task : listTasks) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(cms.getData(Path.NODE_TASK.getFullPath(plugin.getId(), task), null), PluginTask.class);

                waitingTask.put(pluginTask.getId(), new Pair<>(plugin, pluginTask));
                if (pluginTask.getState() == PluginTaskState.DONE) {
                    finalizeTask(pluginTask);
                }

            }

            //adiconando watch para cada peer, realizará recuperação de task escalonadas caso o plugin fique off-line
            cms.getData(Path.STATUS.getFullPath(plugin.getId()), new UpdatePeerData(cms, this));
        }

    }

    /**
     * Verifica no zookeeper qual foi a nova tarefa colocada para execução,
     * adiciona um watcher na tarefa e adiciona a tarefa na lista de tarefas que
     * estão aguardando execução.
     *
     * @param peerPath caminho do local que foi criado a tarefa no zookeeper.
     * @return a pluginTask da tarefas adicionada para execução
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    private PluginTask getNewTask(String taskPath) throws KeeperException, InterruptedException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        PluginTask pluginTask = null;
        List<String> tasksChildren = cms.getChildren(taskPath, null);

        for (String taskChild : tasksChildren) {
            String datasTask = cms.getData(taskPath + "/" + taskChild, null);

            PluginTask task = mapper.readValue(datasTask, PluginTask.class);
            //verifica se a tarefa já estava na lista para não adicionar mais de um watcher
            if (!waitingTask.containsKey(task.getId())) {
                //adiciona um watcher na task que foi escanolada
                cms.getData(taskPath + "/" + taskChild, new UpdatePeerData(cms, this));
                if (task.getState() == PluginTaskState.PENDING) {
                    waitingTask.put(task.getId(), new Pair<>(cloudMap.get(task.getPluginExec()), task));
                    pluginTask = task;
                }
            }
        }
        return pluginTask;
    }

    private void decryptFiles(List<FileInfo> inputs) throws Exception {
        try {
            //realiza uma chama rpc para decriptografar os arquivos que serao usados pela task
            rpcClient = new AvroClient(config.getRpcProtocol(), myLinuxPlugin.getMyInfo().getHost().getAddress(), myLinuxPlugin.getMyInfo().getHost().getPort());
            for (FileInfo info : inputs) {
                rpcClient.getProxy().decryptPluginFile(info.getName());
            }
            rpcClient.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Método que realiza a verificação dos arquivos existentes no plugin para
     * possíveis utilizações durante a execução das tarefas.
     *
     */
    // TODO ajustar a Storage para realizar alguma foram de atualização dos arquivos antes e depois de uma execução e então inutilizar esse método
    private void checkFilesPlugin() {
        try {
            //realiza uma chama rpc para atualizar a lista de arquivos no zookeeper
            rpcClient = new AvroClient(config.getRpcProtocol(), myLinuxPlugin.getMyInfo().getHost().getAddress(), myLinuxPlugin.getMyInfo().getHost().getPort());
            rpcClient.getProxy().listFilesName();
            rpcClient.close();
            if (cms.getZNodeExist(Path.FILES.getFullPath(myLinuxPlugin.getMyInfo().getId()), null)) {
                List<String> filesChildren;
                //verifica se é a primeira vez que é executado e então cria o watcher e inicia a lista
                if (mapFilesPlugin == null) {
                    mapFilesPlugin = new HashMap<>();
                    filesChildren = cms.getChildren(Path.FILES.getFullPath(myLinuxPlugin.getMyInfo().getId()), new UpdatePeerData(cms, this));
                } else {
                    filesChildren = cms.getChildren(Path.FILES.getFullPath(myLinuxPlugin.getMyInfo().getId()), null);
                }
                ObjectMapper mapper = new ObjectMapper();

                for (String fileChild : filesChildren) {
                    String datasFile = cms.getData(Path.NODE_FILE.getFullPath(myLinuxPlugin.getMyInfo().getId(), fileChild), null);
                    PluginFile file = mapper.readValue(datasFile, PluginFile.class);
                    //Verificar o que é esse LONG TO DO
                    Pair<String, Long> pair = new Pair<>(file.getName(), file.getSize());
                    //verifica se o arquivo já estava na lista
                    if (!mapFilesPlugin.containsKey(pair.first)) {
                        mapFilesPlugin.put(file.getName(), file);
                    }
                }
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Verifica o status da tarefa e se ela pertence ao plugin e executa se
     * satisfizer essas condições.
     */
    private void checkStatusTask() {
        LOGGER.info("[SchedService] checkStatusTask");
        for (Pair<PluginInfo, PluginTask> pair : waitingTask.values()) {
            if (myLinuxPlugin.getMyInfo().getId().equals(pair.first.getId()) && pair.second.getState() == PluginTaskState.PENDING) {
                try {
                    executeTasks(pair.second);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    /**
     * Verifica se as tarefas que estavam aguardando algum arquivo já foram
     * executadas, se não solicita execução.
     */
    private void checkTasks() {
        try {

            LOGGER.info("Checking Tasks...");

            // Check if there are any pipelines left to add
            updatePipelines();

            if (waitingTask != null && !waitingTask.isEmpty()) {
                for (Pair<PluginInfo, PluginTask> pair : waitingTask.values()) {
                    if (pair.first.getHost().getAddress().equals(myLinuxPlugin.getMyInfo().getHost().getAddress())) {
                        if (pair.second.getState() == PluginTaskState.WAITING) {
                            executeTasks(pair.second);
                        }
                    }
                    if (pair.second.getState() == PluginTaskState.DONE) {
                        finalizeTask(pair.second);
                    }
                }
            }

            // check if any dependent task can be executed
            if (dependentJobs != null && !dependentJobs.isEmpty()) {
                List<String> finishedJobs = cms.getChildren(Path.FINISHED_TASKS.getFullPath(), null);
                for (Iterator<Job> it = dependentJobs.iterator(); it.hasNext();) {
                    Job j = it.next();
                    // remove finished dependencies
                    for (Iterator<String> it2 = j.getDependencies().iterator(); it2.hasNext();) {
                        String d = it2.next();
                        for (String f : finishedJobs) {
                            if (f.equals(d)) {
                                it2.remove();
                            }
                        }
                    }
                    // if there are no more dependencies put j on pendingJobs
                    if (j.getDependencies().isEmpty()) {
                        pendingJobs.add(j);
                        it.remove();
                    }
                }
            }

            // schedule jobs if there are any
            if (pendingJobs != null && !pendingJobs.isEmpty()) {
                LOGGER.info("Tamanho da lista de JOBS para execução :  " + pendingJobs.size());
                scheduleJobs();
            }

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Recebe a última tarefa enviada para execução
     *
     * @param task
     */
    private void executeTasks(PluginTask task) throws Exception {
        LOGGER.info("Recebimento do pedido de execução da tarefa!");
        //TODO otimiza chamada de checagem dos arquivos
        checkFilesPlugin();

        // CORREÇÂO: CRIAR NÓ FILES E REFAZER ESSA FUNÇÃO
        //        //verifica se o arquivo existe no plugin se não cria a solicitação de transfêrencia do arquivo
        //        if (!existFilesCloud(task.getJobInfo().getInputs())) {
        //            task.setState(PluginTaskState.ERRO);
        //            System.out.println("[SchedService] executeTasks: task " + task.getId() + " error.");
        //            return;
        //        }
        if (!existFiles(task.getJobInfo().getInputFiles())) {
            LOGGER.info("Files from JobInfo(id=" + task.getJobInfo().getId() + ") not found. Requesting file...");

            for (FileInfo f : task.getJobInfo().getInputFiles()) {
                LOGGER.info("Arquivo: " + f.getName());
            }

            requestFile(task.getJobInfo().getInputFiles());

            LOGGER.info("Task " + task.getId() + " files not present.");
        }
        if (existFiles(task.getJobInfo().getInputFiles())) {
            decryptFiles(task.getJobInfo().getInputFiles());

            // Executes the command line and upload it to ZooKeeper
            myLinuxPlugin.startTask(task, cms, workflow);

            LOGGER.info("Task " + task.getId() + " started.");
        } else {
            task.setState(PluginTaskState.WAITING);
            LOGGER.info("Task " + task.getId() + " waiting.");
        }
    }

    /**
     * Verifica a existência dos arquivos de entrada no plugin, lista de
     * arquivos é atualizada ao plugin ser iniciado e quando um novo arquivo é
     * adicionado ao plugin.
     *
     * @param listInputFiles
     * @return
     */
    private boolean existFiles(List<FileInfo> listInputFiles) {

        int toFind = listInputFiles.size();
        
        if (listInputFiles.isEmpty()) {
            return true;
        }

        for (FileInfo fileInput : listInputFiles) {
            if (mapFilesPlugin.containsKey(fileInput.getName())) {
                toFind--;
            }
        }
        
        if (toFind == 0)
            return true;
        
        return false;
    }

    /**
     * Verifica a existência dos arquivos de entrada na federação, caso não
     * exista retorna false.
     *
     * @param listInputFiles
     * @return false se não existir algum arquivo no zoonimbus
     */
    private boolean existFilesCloud(List<Pair<String, Long>> listInputFiles) {

        for (Pair<String, Long> fileInput : listInputFiles) {
            if (getFilesIP(fileInput.first) == null) {
                LOGGER.info("Arquivo :" + fileInput.first + " não existe no Zoonimbus !!!");
                return false;
            }
        }

        return true;
    }

    /**
     * Realiza a chamada dos métodos para a finalização da execução do job.
     *
     * @param pathTask endereço do znode, task executada.
     */
    private void finalizeTask(PluginTask task) {
        if (waitingTask.containsKey(task.getId())) {
            waitingTask.remove(task.getId());
        }

        //        try {
        getPolicy().jobDone(task);
        cms.delete(Path.NODE_TASK.getFullPath(task.getPluginExec(), task.getJobInfo().getId()));

        //chamada de método para atualizar a lista de arquivos
        // TODO ajustar a Storage para realizar alguma foram de atualização dos arquivos após uma execução.
        checkFilesPlugin();

        //cria um znode efêmero para exibir jobs finalizados, é efêmero para poder ser apagado quando peer ficar off-line
        cms.createZNode(CreateMode.PERSISTENT, Path.NODE_FINISHED_TASK.getFullPath(task.getPluginExec(), task.getJobInfo().getId()), task.toString());

//        } catch (KeeperException ex) {
        //            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        //        } catch (InterruptedException ex) {
        //            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        LOGGER.info("[SchedService] task finished: " + task.getId());

    }

    /**
     * Metodo para pegar o Ip de cada peer na federação e verificar se um
     * arquivo está com este peer, se o arquivo for encontrado retorna o Ip do
     * peer, caso contrário retorna null.
     *
     * @param file
     * @return
     */
    public String getFilesIP(String file) {
        List<String> listFiles;
        // Map<String,List<String>> mapFiles = new HashMap<String, List<String>>();
//        try {
        for (Iterator<PluginInfo> it = getPeers().values().iterator(); it.hasNext();) {
            PluginInfo plugin = it.next();
            listFiles = cms.getChildren(Path.FILES.getFullPath(plugin.getId()), null);
            for (String checkfile : listFiles) {
                //atualizar

//                String idfile = checkfile.substring(5, checkfile.length());
                if (file.equals(checkfile)) {
                    return plugin.getHost().getAddress();
                }
            }
        }//        } catch (KeeperException ex) {
//            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//        }

        return null;

    }

    /**
     * Trata os watchers enviados pelas mudanças realizadas no zookeeper.
     *
     * @param eventType evento recebido do zookeeper
     */
    @Override
    public void event(WatchedEvent eventType) {

        try {
            switch (eventType.getType()) {

                case NodeChildrenChanged:

                    String datas;
                    //reconhece um alerta de um novo pipeline

                    if (eventType.getPath().contains(Path.PIPELINES.toString()) && !isClient) {
                        LOGGER.info("[SchedService] Recebimento de um alerta para um pipeline, NodeChildrenChanged");
                        // checking moved to checkTasks in order to solve racing condition
//                        updatePipelines();

                    } else if (eventType.getPath().contains(Path.SCHED.toString() + Path.TASKS)) {
                        LOGGER.info("[SchedService] Recebimento de um alerta para uma TAREFA");

                        //verifica qual foi o job colocado para ser executado
                        PluginTask pluginTask = getNewTask(eventType.getPath());

                        //verifica se um existe algum novo pluginTask
                        if (pluginTask != null) {
                            if (pluginTask.getState() == PluginTaskState.PENDING) {
                                executeTasks(pluginTask);
                            }
                        } else if (!waitingTask.isEmpty()) {
                            checkStatusTask();
                        }

                    } else if (eventType.getPath().contains(Path.FILES.toString())) {
                        checkFilesPlugin();

                    } else if (eventType.getPath().equals(Path.PEERS.toString())) {
                        if (cloudMap.size() < getPeers().size()) {
                            verifyPlugins();
                        }
                    }

                    break;
                case NodeDataChanged:
                    //reconhece o alerta como uma mudança na política de escalonamento
//                    if (eventType.getPath().contains(JOBS.getCodigo()) && !eventType.getPath().contains(PREFIX_JOB.toString())) {
//                        setPolicy();

                    //reconhece a mudança no estado da tarefa
//                    } else 
                    if (cms.getZNodeExist(eventType.getPath(), null)) {
                        datas = cms.getData(eventType.getPath(), null);
                        PluginTask pluginTask = (PluginTask) convertString(PluginTask.class, datas);

                        //retirar depois testes, exibi a tarefa que está em execução
                        if (pluginTask.getState() == PluginTaskState.RUNNING) {
                            LOGGER.info("Task está rodando: " + Path.NODE_TASK.getFullPath(pluginTask.getPluginExec(), pluginTask.getId()));
                        }
                        if (pluginTask.getState() == PluginTaskState.DONE) {
                            finalizeTask(pluginTask);
                        }
                    }
                    break;

                case NodeDeleted:

                    //Trata o evento de quando o zNode status for apagado, ou seja, quando um peer estiver off-line, deve recuperar os jobs
                    //que haviam sido escalonados para esse peer
                    if (eventType.getPath().contains(Path.STATUS.toString())) {
//                        cloudMap.clear();
//                        cloudMap.putAll(getPeers());
//                        schedPolicy.setCloudMap(cloudMap);
                        repairTask(eventType.getPath().subSequence(0, eventType.getPath().indexOf("STATUS") - 1).toString());
                    }

                    break;
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updatePipelines() throws IOException, InterruptedException, KeeperException {
        // get all pipelines
        List<String> pipelinesId = cms.getChildren(Path.PIPELINES.getFullPath(), null);
        String datas;

        if (!pipelinesId.isEmpty()) {

            // get pipelines and add them to pendingPipelines
            for (String pipelineReady : pipelinesId) {
                ObjectMapper mapper = new ObjectMapper();
                datas = cms.getData(Path.NODE_PIPELINE.getFullPath(pipelineReady), null);

                // Sets it workflow
                workflow = mapper.readValue(datas, Workflow.class);

                workflowLogger.log(new Log("Iniciando serviço de escalonamento...", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));

                // add independent jobs to pendingJobs list and jobs with 
                // any dependency to the dependentJobs list
                int i = 0;
                for (Job j : workflow.getJobs()) {
                    if (j.getDependencies().isEmpty()) {
                        pendingJobs.add(j);
                        i++;
                    } else {
                        dependentJobs.add(j);
                    }
                }

                LOGGER.info("Workflow is compound by: " + i + " independent jobs and " + (workflow.getJobs().size() - i) + " jobs with dependency");

                // Log it
                workflowLogger.log(new Log(" Job(s) independente(s): <b>" + i + "</b>", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));
                workflowLogger.log(new Log("Job(s) com dependência(s): <b>" + (workflow.getJobs().size() - i) + "</b>", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));

                // remove pipeline from zookeeper
                cms.delete(Path.NODE_PIPELINE.getFullPath(pipelineReady));
            }

            if (!pendingJobs.isEmpty()) {
                scheduleJobs();
            }
        }
    }

    @Override
    public void verifyPlugins() {
        Collection<PluginInfo> temp = getPeers().values();
        temp.removeAll(cloudMap.values());
        for (PluginInfo plugin : temp) {
//            try {
            if (cms.getZNodeExist(Path.STATUS.getFullPath(plugin.getId()), null)) {
                cms.getData(Path.STATUS.getFullPath(plugin.getId()), new UpdatePeerData(cms, this));
            }
//            } catch (KeeperException ex) {
//                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (InterruptedException ex) {
//                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }

    }

    private Object convertString(Class classe, String datas) {
        Object object = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            object = mapper.readValue(datas, classe);

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return object;

    }

    public synchronized Map<String, PluginInfo> getCancelingJobs() {
        return cancelingJobs;
    }

    public synchronized Map<String, Job> getJobsWithNoService() {
        return jobsWithNoService;
    }

    @Override
    public void shutdown() {
        listeners.remove(this);
        schedExecService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub
    }
}
