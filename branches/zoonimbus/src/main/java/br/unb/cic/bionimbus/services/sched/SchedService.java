package br.unb.cic.bionimbus.services.sched;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.*;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.monitor.MonitoringService;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.utils.Pair;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SchedService extends AbstractBioService implements Service, P2PListener, Runnable {
    private static final Logger LOG = LoggerFactory
            .getLogger(SchedService.class);

    private final ConcurrentHashMap<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();

    private final ScheduledExecutorService schedExecService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("SchedService-%d").build());
    
    private final Queue<PluginTask> relocateTasks = new ConcurrentLinkedQueue<PluginTask>();
    
    private final Map<String, JobInfo> pendingJobs = new ConcurrentHashMap<String, JobInfo>();
    
    Map<String, Pair<PluginInfo, PluginTask>> waitingTask  = new ConcurrentHashMap<String, Pair<PluginInfo, PluginTask>>();

    private final Map<String, JobInfo> jobsWithNoService = new ConcurrentHashMap<String, JobInfo>();

//    private final Queue<PluginTask> runningJobs = new ConcurrentLinkedQueue<PluginTask>();

    private final Map<String, PluginInfo> cancelingJobs = new ConcurrentHashMap<String, PluginInfo>();
    
    private SchedExecute schedExecute = new SchedExecute();

    private P2PService p2p = null;
    
    private  String idPlugin;

    private SchedPolicy schedPolicy;

    private boolean isScheduling = false;

    private int isAcquiringStatus = 0;

    private int isCanceling = 0;

    private static final String STATUS = "/STATUS";
    private static final String STATUSWAITING = "/STATUSWAITING";
    private static final String ROOT_PEER = "/peers";
    private static final String SCHED = "/sched";
    private static final String JOBS = "/jobs";
    private static final String TASKS = "/tasks";
    private static final String PREFIX_JOB = "/job_";
    private static final String PREFIX_TASK = "/task_";
    private static final String SEPARATOR = "/";
    
    
    @Inject
    public SchedService(final ZooKeeperService service) {
        Preconditions.checkNotNull(service);
        this.zkService = service;
        
        
    }

    public synchronized SchedPolicy getPolicy() {
        if (schedPolicy == null) {
            schedPolicy = SchedPolicy.getInstance();
        }

        schedPolicy.setCloudMap(cloudMap);
        return schedPolicy;
    }

    @Override
    public void run() {
        System.out.println("running SchedService...");
        
    }
    // TO DO retirar serviço P2P?
    @Override
    public void start(P2PService p2p) {
        //atualiza os peers da cloud
        List<PluginInfo> listPlugin = new ArrayList<PluginInfo>(getPeers().values());

        for(PluginInfo pluginInfo : listPlugin){
            zkService.createPersistentZNode(JOBS, null);
            zkService.createPersistentZNode(pluginInfo.getPath_zk()+SCHED, null);
            zkService.createPersistentZNode(pluginInfo.getPath_zk()+SCHED+TASKS, null);
            zkService.createPersistentZNode(pluginInfo.getPath_zk()+SCHED +"/size_jobs", null);
            cloudMap.put(pluginInfo.getId(), pluginInfo);
        }
        this.p2p = p2p;
        if (p2p != null)
            p2p.addListener(this);
        idPlugin = this.p2p.getConfig().getId();
        
        try {
            //adicona um watcher para receber um alerta quando um novo job for criado para ser escalonado
            zkService.getChildren(JOBS, new SchedUpdatePeerData(zkService, this));
            
            checkWaitingJobs();
            checkPeers();

        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        schedExecute.start();
        
        schedExecService.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    

    /**
     * Executa a rotina de escalonamento, após o zookeeper disparar um aviso que um novo job foi criado para ser escalonado.
     */
    private void scheduleJobs() throws InterruptedException,KeeperException{
        HashMap<JobInfo, PluginInfo> schedMap = null;

        // Caso nao exista nenhum job pendente da a chance do escalonador
        // realocar as tarefas.
        if (!getPendingJobs().isEmpty()) {

            schedMap = getPolicy().schedule(getPendingJobs().values(), zkService);

            for (Map.Entry<JobInfo, PluginInfo> entry : schedMap.entrySet()) {
                JobInfo jobInfo = entry.getKey();
                PluginInfo pluginInfo = entry.getValue();
                PluginTask task = new PluginTask();
                task.setJobInfo(jobInfo);
                if (pluginInfo == null) {
                    // Soh entra aqui caso o servico nao esteja disponivel.
                    task.setState(PluginTaskState.PENDING);
                } else {
                    System.out.println("SCHEDULE JobID: " + jobInfo.getId()
                            + " escalonado para peer_" + pluginInfo.getId());
                    
                    task.setPluginExec(pluginInfo.getId());
                    task.setState(PluginTaskState.WAITING);
                    //adiciona o job na lista de execução do servidor zookeeper
                    zkService.createPersistentZNode(pluginInfo.getPath_zk()+SCHED+TASKS+PREFIX_TASK+task.getId(), task.toString());
                    //retira o job da lista de jobs para escanolamento no zookeeper
                    zkService.delete(JOBS+PREFIX_JOB+task.getJobInfo().getId());
                    //retira o job da lista de jobs para escalonamento
                    getPendingJobs().remove(task.getJobInfo().getId());
//                    //adiciona a lista de jobs que aguardam execução
//                    waitingTask.put(task.getJobInfo().getId(), new Pair<PluginInfo, PluginTask>(pluginInfo,task));
                    
                }
            }
        }
    }

    /**
     * Rotinas para auxiliar escalonamento,chamado caso seja necessário cancelar um job.
     * 
     * @param origin
     * @param jobId 
     */
    private void cancelJob(String jobId) {
        // Apenas remove dos jobs pendentes (ou seja, ainda nem foi escalonado)
        if (getPendingJobs().containsKey(jobId)) {
            getPendingJobs().remove(jobId);
            //excluir o job do zookeeper TO DO
        }
        try {
            zkService.delete(JOBS+PREFIX_JOB+jobId);
        
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Remove a tarefa da lista de jobs cancelados.Job permanece na lista de jobs a serem escalonados.
     * @param task 
     */
    private synchronized void finishCancelJob(PluginTask task) {
        getCancelingJobs().remove(task.getId());
        System.out.println("Task canceled " + task.getId());
        isCanceling--;

        relocateTasks.add(task);
        // Verifica se todas as requisicoes de cancelamento foram realizadas.
        // TODO: Provavelmente se o usuario cancelar o job na mao essa
        // funcao vai buggar. Mas dado o tempo que temos acho que eh a melhor
        // solucao.
        if (isCanceling == 0) {
            isScheduling = false;
        }
    }

    /**
     * Recebe uma lista de PluginsTasks para serem relançadas ao escalonamento.
     * @param tasks lista das tarefas
     */
    public void relocateTasks(Collection<PluginTask> tasks) {
        relocateTasks.addAll(tasks);
        
        //Adiciona os jobs cancelados a lista de jobs a serem escalonados no servidor zookeeper
        while (!relocateTasks.isEmpty()) {
            PluginTask task = relocateTasks.remove();
            try {
                zkService.createPersistentZNode(JOBS+PREFIX_JOB+task.getJobInfo().getId(), task.getJobInfo().toString());
                
            } catch (Exception ex) {
                tasks.add(task);
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    
    /**
     * Realiza a recuperação das tarefas que estavam em execução no peer que ficou off-line, colocando-as para 
     * serem reescalonadas. Ao final exclui o zNode STATUSWAITING para que o peer possa ser excluído do servidor zookeeper
     * @param peerPath 
     */
    private void repairTask(String peerPath){
        Collection<PluginTask> repairTasks = new LinkedList<PluginTask>();
        try {
            StringBuilder dataStatus=new StringBuilder();
            
            if(!zkService.getZNodeExist(peerPath+STATUSWAITING, false))
                zkService.createPersistentZNode(peerPath+STATUSWAITING, "");
            
            dataStatus.append(zkService.getData(peerPath+STATUSWAITING, null));
            if(dataStatus.toString().contains("E"))
                return;
                    
            List<String> tasksChildren = zkService.getChildren(peerPath+SCHED+TASKS, null);

            for (String taskChild : tasksChildren) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(zkService.getData(peerPath+SCHED+TASKS+SEPARATOR+taskChild, null), PluginTask.class);
                repairTasks.add(pluginTask);
            }
            
            relocateTasks(repairTasks);
            zkService.setData(peerPath+STATUSWAITING, dataStatus.append("E").toString());
            
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Realiza a verificação dos peers existentes identificando se existe algum peer aguardando recuperação,
     * se o peer estiver off-line e a recuperação já estiver sido feito, realiza a limpeza do peer.
     * Realizada apenas quando o módulo inicia.
     */
    private void checkPeers(){
        try {
            List<String> listPeers = zkService.getChildren(ROOT_PEER, null);
            for (String peerPath : listPeers) {

                if(!zkService.getZNodeExist(ROOT_PEER+SEPARATOR+peerPath+STATUS, false) 
                        && !zkService.getZNodeExist(ROOT_PEER+SEPARATOR+peerPath+STATUSWAITING, false)){
                        zkService.createPersistentZNode(ROOT_PEER+SEPARATOR+peerPath+STATUSWAITING, "");
                }
                if(zkService.getZNodeExist(ROOT_PEER+SEPARATOR+peerPath+STATUSWAITING, false)){
                    if(!zkService.getData(ROOT_PEER+SEPARATOR+peerPath+STATUSWAITING, null).contains("E")){
                        repairTask(ROOT_PEER+SEPARATOR+peerPath);
                    }
                }
                
            }   
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Verifica quais são as tarefas que já estão escanoladas e adiciona um watcher em cada conjunto de tarefas dos plugins e nas
     * tarefas caso existam. Método é chamado ao iniciar o módulo de escalonamento.
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException 
     */
    private void checkWaitingJobs() throws KeeperException,InterruptedException, IOException{
        List<PluginInfo> plgs = new ArrayList<PluginInfo> (getPeers().values());
        List<String> listTasks;
        
        
        for (PluginInfo plugin : plgs) {
            //adiconando watch para cada o znode que contém as tarefas escanoladas
            listTasks = zkService.getChildren(plugin.getPath_zk()+SCHED+TASKS, new SchedUpdatePeerData(zkService,this));
            
            for (String task : listTasks) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(zkService.getData(plugin.getPath_zk()+SCHED+TASKS+SEPARATOR+task, new SchedUpdatePeerData(zkService,this)), PluginTask.class);
                
                waitingTask.put(pluginTask.getJobInfo().getId(), new Pair<PluginInfo, PluginTask>(plugin,pluginTask));
                
            }

            //adiconando watch para cada peer, realizará recuperação de task escalonadas caso o plugin fique off-line
            zkService.getData(plugin.getPath_zk()+STATUS, new SchedUpdatePeerData(zkService,this));
        }
        
    }
    
    /**
     * Verifica no zookeeper qual foi a nova tarefa colocada para execução, adiciona um watcher na tarefa e adiciona a tarefa na 
     * lista de tarefas que estão aguardando execução. Método é chamado a cada novo escalonamento realizado.
     * @param peerPath caminho do local que foi criado a tarefa no zookeeper.
     * @return a pluginTask da tarefas adicionada para execução
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException 
     */
    private PluginTask checkWattingJobs(String taskPath) throws KeeperException,InterruptedException,IOException{
        ObjectMapper mapper = new ObjectMapper();
        PluginTask pluginTask =null;
        List<String> tasksChildren = zkService.getChildren(taskPath, null);
        
        for (String taskChild : tasksChildren) {
            String datasTask = zkService.getData(taskPath+SEPARATOR+taskChild, null);
            
            PluginTask task = mapper.readValue(datasTask, PluginTask.class);
            //verifica se a tarefa já estava na lista para não adicionar mais de um watcher
            if(!waitingTask.containsKey(task.getJobInfo().getId())){
                // adiciona um watcher na task que foi escanolada
                zkService.getData(taskPath+SEPARATOR+taskChild, new SchedUpdatePeerData(zkService, this));
                waitingTask.put(task.getJobInfo().getId(), new Pair<PluginInfo, PluginTask>(cloudMap.get(task.getPluginExec()),task));
                pluginTask = task;
            }
        }
        return pluginTask;
    }
    
    /**
     * Recebe a última tarefa enviada para execução 
     * @param task 
     */
    private void executeTasks(PluginTask task) throws IOException{
        boolean taskExecuted = false;
        //TO DO descobrir como identificar o plugin desse recurso
        if(task.getPluginExec().equals(idPlugin)){
            //adicona o job escalonado na fila de jobs para execução
            String serviceName = waitingTask.get(task.getJobInfo().getId()).first.getService(task.getJobInfo().getServiceId()).getName();
            taskExecuted = schedExecute.executeTask(task,serviceName);
            if(taskExecuted){
                finishedTask(task);
            }
        }
    }
    
    /**
     * Realiza a chamada dos métodos para a finalização da execução do job.
     * @param pathTask endereço do znode, task executada.
     */
    private void finishedTask(PluginTask task){
        Pair<PluginInfo, PluginTask> pair  = waitingTask.remove(task.getJobInfo().getId());
        try {
            zkService.delete(pair.first.getPath_zk());
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
        float timeExec = (((float) System.currentTimeMillis() - task.getJobInfo().getTimestamp()) / 1000);
        
        task.setTimeExec(timeExec);
        System.out.println("Tempo de execução job: "+timeExec);
        this.schedPolicy.jobDone(pair.second);
    }
    
    /**
     * Trata os watchers enviados pelas mudanças realizadas no zookeeper.
     * @param eventType evento recebido do zookeeper
     */
    @Override
    public void event(WatchedEvent eventType) {
         System.out.println("SchedService event path:"+eventType.getPath().toString());
        System.out.println("SchedService event type"+eventType.getType().toString());
        switch(eventType.getType()){
        
            case NodeChildrenChanged:
                
            String datas;
            if(eventType.getPath().contains(JOBS)){
                List<String> children; 
                try {
                    children = zkService.getChildren(eventType.getPath(), null);
                    for(String child: children){
                        ObjectMapper mapper = new ObjectMapper();
                        datas =  zkService.getData(eventType.getPath()+"/"+child, null);
                        JobInfo job = mapper.readValue(datas, JobInfo.class);
                        if(!getPendingJobs().containsKey(job.getId())){
                            getPendingJobs().put(job.getId(), job);
                        }
                    }   

                    scheduleJobs();
                } catch (KeeperException ex) {
                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                }
                    /*
                    * Criar variável para diferenciar tarefa realizada de tarefa para executar
                    */
                
            }else if(eventType.getPath().contains(SCHED+TASKS)){
                    try {
                        //verifica qual foi o job colocado para ser executado 
                        PluginTask pluginTask = checkWattingJobs(eventType.getPath());
                        //verifica se um existe algum novo pluginTask
                        if(pluginTask !=null){
                            executeTasks(pluginTask);
                            //como retornar para executar ? TO DO
                        }

                    } catch (KeeperException ex) {
                        java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                break;
            case NodeDataChanged:

                
                
                break;
                
            
            case NodeDeleted:
                //Trata o evento de quando o zNode status for apagado, ou seja, quando um peer estiver off-line, deve recuperar os jobs
                //que haviam sido escalonados para esse peer
                if(eventType.getPath().contains(STATUS)){
                    repairTask(eventType.getPath().subSequence(0, eventType.getPath().indexOf("STATUS")-1).toString());
                }
                    
                
            break;
        }
    }
            
    private PluginFile getFileById(String fileId,
                                   Collection<PluginFile> pluginFiles) {
        for (PluginFile file : pluginFiles) {
            if (file.getId().equals(fileId)) {
                return file;
            }
        }
        return null;
    }



//    private synchronized Queue<PluginTask> getRunningJobs() {
//        return runningJobs;
//    }

    public synchronized Map<String, JobInfo> getPendingJobs() {
        return pendingJobs;
    }

    public synchronized Map<String, PluginInfo> getCancelingJobs() {
        return cancelingJobs;
    }

    public synchronized Map<String, JobInfo> getJobsWithNoService() {
        return jobsWithNoService;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //Retirar tudo abaixo
    
    
    /**
     * PASSOS DO ESCALONADOR *
     */
    /* Prepara as rotinas de escalonamento */
//    private void onSchedEvent() {
//        if (isScheduling)
//            return;
//        if (isAcquiringStatus > 0)
//            return;
//        isScheduling = true;
//
//        // Atualiza os estados das tarefas.
//        while (isAcquiringStatus > 0) {
//            try {
//                Thread.sleep(100);
//            } catch (Exception ex) {
//
//            }
//        }
//
//        // Antes de escalonar verifica o tamanho dos arquivos.
//        //TO DO porque?
//        sendListReqEvent(p2p.getPeerNode());
//    }
//
//    //TO DO para que serve esse método?
//    /* Recebe a resposta da requisicao da lista de arquivos */
//    private void onListRespEvent(PeerNode sender, PeerNode receiver,
//                                 ListRespMessage listResp) {
//        fillJobFileSize(listResp.values());
//
//        // Com os arquivos preenchidos, executa o escalonador.
////        scheduleJobs(sender, receiver);
//    }
//
//    /* Preenche cada job com o tamanho dos arquivos associados */
//    private void fillJobFileSize(Collection<PluginFile> pluginFiles) {
//        for (JobInfo job : getPendingJobs().values()) {
//            List<Pair<String, Long>> pairList = new ArrayList<Pair<String, Long>>(
//                    job.getInputs());
//            for (Pair<String, Long> pair : pairList) {
//                String fileId = pair.first;
//                PluginFile file = getFileById(fileId, pluginFiles);
//
//                if (file != null) {
//                    job.addInput(file.getId(), file.getSize());
//                } else {
//                    LOG.debug("File returned null.");
//                }
//            }
//        }
//    }
//    
//    /**
//     * Realiza a atualização de finalização da tarefa enviando pedido de atualização do status no zookeeper e
//     * removendo o job da lista de tarefas na fila para execução.
//     * @param task, tarefa que foi executada.
//     */
//    private synchronized void finalizeJob(PluginTask task) {
//        Pair<PluginInfo, PluginTask> pair = getRunningJobs().get(task.getId());
//
//        JobInfo job = pair.second.getJobInfo();
//        float timeExec = (((float) System.currentTimeMillis() - job.getTimestamp()) / 1000);
//
//        task.setTimeExec(timeExec);
//        task.setPluginExec(pair.first.getId());
//        
//        //atualiza o status do job no zookeeper.
//        updateJobStatus(task,PluginTaskState.DONE);
//        //retira o job da lista
//        getRunningJobs().remove(task.getId());
//        getPolicy().jobDone(task);
//        
//        
        
        // p2p.sendMessage(new EndJobMessage(job));
//    }
    //TO DO retirar serviço P2P?
    @Override
    public void shutdown() {
        p2p.remove(this);
        schedExecService.shutdownNow();
    }
    
    @Override
    public void getStatus() {
        // TODO Auto-generated method stub

    }
    //TO DO não sei o faz esse método, faz tudo?
    @Override
    public synchronized void onEvent(P2PEvent event) {
//        if (!event.getType().equals(P2PEventType.MESSAGE))
//            return;
//
//        P2PMessageEvent msgEvent = (P2PMessageEvent) event;
//        Message msg = msgEvent.getMessage();
//        if (msg == null)
//            return;
//
//        PeerNode sender = p2p.getPeerNode();
//        PeerNode receiver = null;
//        if (msg instanceof AbstractMessage) {
//            receiver = ((AbstractMessage) msg).getPeer();
//        }
//
//        switch (P2PMessageType.of(msg.getType())) {
//            case CLOUDRESP:
//                CloudRespMessage cloudMsg = (CloudRespMessage) msg;
//                for (PluginInfo info : cloudMsg.values())
//                    cloudMap.put(info.getId(), info);
//                break;
//            case JOBREQ:
//                JobReqMessage jobMsg = (JobReqMessage) msg;
//                for (JobInfo jobInfo : jobMsg.values()) {
//                    jobInfo.setId(UUID.randomUUID().toString());
//                    jobInfo.setTimestamp(System.currentTimeMillis());
//                    getPendingJobs().put(jobInfo.getId(), jobInfo);
//                }
//                break;
//            case STARTRESP:
//                StartRespMessage respMsg = (StartRespMessage) msg;
//                sendJobResp(sender, receiver, respMsg.getJobId(),
//                        respMsg.getPluginTask());
//                break;
//            case STATUSRESP:
//                StatusRespMessage status = (StatusRespMessage) msg;
//                updateJobStatus(status.getPluginTask());
//                break;
//            case JOBCANCELREQ:
//                JobCancelReqMessage cancel = (JobCancelReqMessage) msg;
////                cancelJob(cancel.getPeerNode().getHost(), cancel.getJobId());
//                break;
//            case CANCELRESP:
//                CancelRespMessage cancelResp = (CancelRespMessage) msg;
////                Pair<String, Host> pair = getCancelingJobs().get(
////                        cancelResp.getPluginTask().getId());
////                p2p.sendMessage(pair.second,
////                        new JobCancelRespMessage(p2p.getPeerNode(), pair.first));
////                finishCancelJob(cancelResp.getPluginTask());
//                break;
//            case LISTRESP:
//                ListRespMessage listResp = (ListRespMessage) msg;
//                onListRespEvent(sender, receiver, listResp);
//                break;
//            case END:
//                EndMessage end = (EndMessage) msg;
//                finalizeJob(end.getTask());
//                break;
//            case ERROR:
//                ErrorMessage errMsg = (ErrorMessage) msg;
//                LOG.warn("SCHED ERROR: type=" + errMsg.getErrorType().toString()
//                        + ";msg=" + errMsg.getError());
//                break;
//        }
    }
        // TO DO ESCLUIR MÉTODO ???
//    private synchronized void sendStatusReq(PeerNode sender, String taskId) {
//        isAcquiringStatus++;
//        StatusReqMessage msg = new StatusReqMessage(sender, taskId);
//        p2p.broadcast(msg); // TODO: isto é realmente um broadcast?
//    }
      //TO DO não há necessidade de realizar requisição de informação do job, zookeeper coordena essas informações
//    private synchronized void checkRunningJobs() {
//        PeerNode peer = p2p.getPeerNode();
//        for (String taskId : getRunningJobs().keySet()) {
//            sendStatusReq(peer, taskId);
//        }
//        
//    }
    
//TO DO Excluir método...
    /* Faz a requisicao de listagem de arquivos e seus tamanhos */
//    private void sendListReqEvent(PeerNode sender) {
//        ListReqMessage listReqMsg = new ListReqMessage(sender);
//        p2p.broadcast(listReqMsg);
//    }

    //TO DO NÃO É NECESSÁRIO ESSE MÉTODO JÁ QUE QUANDO A TAREFA FOR INICIADA A EXECUÇÃO O STATUS DO PREFIX_JOB SERA ALTERADO NO
    // ZOOKEEPER E TODOS OS OUVINTES DO PREFIX_JOB SERÃO INFORMADOS.
    /* Envia resposta de inicio de job */
//    private synchronized void sendJobResp(PeerNode sender, PeerNode receiver,
//                                          String jobId, PluginTask task) {
//
//        // Remove jobs da lista de jobs a serem escalonados.
//        JobInfo jobInfo = getPendingJobs().remove(jobId);
//
//        if (task == null) {
//            // Jobs que nao possuem servico.
//            // TODO: Sao simplesmente ignorados pelo escalonador por enquanto
//            // Eh simples readiciona-los na rotina de escalonamento sempre que o
//            // escalonador for
//            // requisitado. Mas nao farei isso por enquanto, para evitar
//            // possiveis
//            // erros.
//            getJobsWithNoService().put(jobInfo.getId(), jobInfo);
//
//            // Cria e envia a mensagem de resposta.
//            JobRespMessage jobRespMsg = new JobRespMessage(sender, null);
//            p2p.broadcast(jobRespMsg);
//        } else {
//            // Adiciona job na lista de jobs "rodando" (ou seja, enviados para o
//            // hadoop)
////            getRunningJobs().put(task.getJobInfo().getId(),new Pair<JobInfo, PluginTask>(jobInfo, task));
//
//            // Cria e envia a mensagem de resposta.
//            JobRespMessage jobRespMsg = new JobRespMessage(sender, jobInfo);
//            p2p.broadcast(jobRespMsg);
//        }
//
//        // Define que o escalonamento acabou somente se todas os jobs ja foram
//        // escalonados.
//        isPending--;
//        System.out.println("isPending: " + isPending);
//        if (isPending == 0) {
//            isScheduling = false;
//        }
//    }
/**
     * Realiza o início do pedido de execução de um Job no recurso escanolado.
     * @param sender TO DO para que usar esse sender???
     * @param dest, endereço host do plugin de destino para execução.
     * @param jobInfo, job que será executado no plugin.
     */
//    private void sendStartReq(PeerNode sender, Host dest, JobInfo jobInfo) {
//        isPending++;
        
        //criar chamada para método de chamada de requisição de execução do job
        
//        StartReqMessage startMsg = new StartReqMessage(sender, jobInfo);
//        p2p.sendMessage(dest, startMsg);
        
//    }

}
