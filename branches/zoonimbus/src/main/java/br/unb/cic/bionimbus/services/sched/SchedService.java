package br.unb.cic.bionimbus.services.sched;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.p2p.messages.ListReqMessage;
import br.unb.cic.bionimbus.p2p.messages.ListRespMessage;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskState;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.utils.Pair;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

    private final Map<String, JobInfo> jobsWithNoService = new ConcurrentHashMap<String, JobInfo>();

    private final Map<String, Pair<PluginInfo, PluginTask>> runningJobs = new ConcurrentHashMap<String, Pair<PluginInfo, PluginTask>>();

    private final Map<String, PluginInfo> cancelingJobs = new ConcurrentHashMap<String, PluginInfo>();

    private P2PService p2p = null;

    private SchedPolicy schedPolicy;

    private boolean isScheduling = false;

    private int isAcquiringStatus = 0;

    private int isCanceling = 0;

    private static final String ROOT_PEER = "/peers";
    private static final String SCHED = "/sched";
    private static final String JOBS = "/jobs";
    private static final String PREFIX_JOB = "/job_";
    private static final String TASKS = "/tasks";
    private static final String TASK = "/task_";

    
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
                    
                    task.setState(PluginTaskState.WAITING);
                    updateJobStatus(task);
                    //adiciona o job na lista de execução do servidor zookeeper
                    zkService.createPersistentZNode(pluginInfo.getPath_zk()+SCHED+TASKS+TASK+task.getId(), task.toString());
                    //retira o job da lista de jobs para escanolamento no zookeeper
                    zkService.delete(JOBS+PREFIX_JOB+task.getJobInfo().getId());
                    //retira o job da lista de jobs para escalonamento
                    getPendingJobs().remove(task.getJobInfo().getId());
                    //adicona o job escalonado no map de jobs em execução
                    getRunningJobs().put(task.getJobInfo().getId(),new Pair<PluginInfo, PluginTask>(pluginInfo, task));
                    
                    
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

        //adicona um watcher para receber um alerta quando um novo job for criado para ser escalonado
        try {
            zkService.getChildren(JOBS, new SchedUpdatePeerData(zkService, this));

        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
        schedExecService.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    
    /**
     * Recebe uma lista de PluginsTasks para serem relançadas ao escalonamento.
     * @param tasks 
     */
    public void relocateTasksJobs(Collection<PluginTask> tasks) {
        relocateTasks.addAll(tasks);
//            getPendingJobs().put(task.getJobInfo().getId(), task.getJobInfo());
        
        //Adiciona os jobs cancelados a lista de jobs a serem escalonados no servidor zookeeper
        while (!relocateTasks.isEmpty()) {
            PluginTask task = relocateTasks.remove();
            try {
                
                zkService.createEphemeralZNode(JOBS+PREFIX_JOB+task.getId(), task.getJobInfo().toString());
                
            } catch (Exception ex) {
                tasks.add(task);
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
  
    /**
     * Trata os watchers enviados da implementação da classe Watcher que recebe uma notificação do zookeeper
     * @param eventType evento recebido do zookeeper
     */
    @Override
    public void event(WatchedEvent eventType) {
        if(eventType.getType() == Watcher.Event.EventType.NodeChildrenChanged){
            String datas;
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


    /**
     * Realiza as alterações do status do Job.
     * @param task, pluginTask com o status atualizado da tarefa.
     */
    private synchronized void updateJobStatus(PluginTask task) {
        // DEBUG
        // System.out.println("Old Task Info: ");
        // System.out.println(task.getId() + ": " +
        // runningJobs.get(task.getId()).second.getState());
//      if (getRunningJobs().containsKey(task.getId())) {
//		getRunningJobs().get(task.getId()).second.setState(task.getState());
//	}
        if (getRunningJobs().containsKey(task.getId())) {
            Pair<PluginInfo, PluginTask> pair = getRunningJobs().get(task.getId());
            pair.second.setState(task.getState());
            try {
                zkService.setData(pair.first.getPath_zk()+SCHED+TASKS+TASK+task.getId(), task.toString());
                
            } catch (KeeperException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }

    }

    private synchronized Map<String, Pair<PluginInfo, PluginTask>> getRunningJobs() {
        return runningJobs;
    }

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
    private void onSchedEvent() {
        if (isScheduling)
            return;
        if (isAcquiringStatus > 0)
            return;
        isScheduling = true;

        // Atualiza os estados das tarefas.
        while (isAcquiringStatus > 0) {
            try {
                Thread.sleep(100);
            } catch (Exception ex) {

            }
        }

        // Antes de escalonar verifica o tamanho dos arquivos.
        //TO DO porque?
        sendListReqEvent(p2p.getPeerNode());
    }

    //TO DO para que serve esse método?
    /* Recebe a resposta da requisicao da lista de arquivos */
    private void onListRespEvent(PeerNode sender, PeerNode receiver,
                                 ListRespMessage listResp) {
        fillJobFileSize(listResp.values());

        // Com os arquivos preenchidos, executa o escalonador.
//        scheduleJobs(sender, receiver);
    }

    /* Preenche cada job com o tamanho dos arquivos associados */
    private void fillJobFileSize(Collection<PluginFile> pluginFiles) {
        for (JobInfo job : getPendingJobs().values()) {
            List<Pair<String, Long>> pairList = new ArrayList<Pair<String, Long>>(
                    job.getInputs());
            for (Pair<String, Long> pair : pairList) {
                String fileId = pair.first;
                PluginFile file = getFileById(fileId, pluginFiles);

                if (file != null) {
                    job.addInput(file.getId(), file.getSize());
                } else {
                    LOG.debug("File returned null.");
                }
            }
        }
    }
    
    /**
     * Realiza a atualização de finalização da tarefa enviando pedido de atualização do status no zookeeper e
     * removendo o job da lista de tarefas na fila para execução.
     * @param task, tarefa que foi executada.
     */
    private synchronized void finalizeJob(PluginTask task) {
        Pair<PluginInfo, PluginTask> pair = getRunningJobs().get(task.getId());

        JobInfo job = pair.second.getJobInfo();
        float timeExec = (((float) System.currentTimeMillis() - job.getTimestamp()) / 1000);

        task.setState(PluginTaskState.DONE);
        task.setTimeExec(timeExec);
        task.setPluginExec(pair.first.getId());
        
        //atualiza o status do job no zookeeper.
        updateJobStatus(task);
        //retira o job da lista
        getRunningJobs().remove(task.getId());
        getPolicy().jobDone(task);
        
        
        
        // p2p.sendMessage(new EndJobMessage(job));
    }
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
    private void sendListReqEvent(PeerNode sender) {
        ListReqMessage listReqMsg = new ListReqMessage(sender);
        p2p.broadcast(listReqMsg);
    }

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
