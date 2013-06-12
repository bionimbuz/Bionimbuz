package br.unb.cic.bionimbus.services.sched;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.services.messaging.Message;
import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PEventType;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PMessageEvent;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.p2p.messages.AbstractMessage;
import br.unb.cic.bionimbus.p2p.messages.CancelReqMessage;
import br.unb.cic.bionimbus.p2p.messages.CancelRespMessage;
import br.unb.cic.bionimbus.p2p.messages.CloudRespMessage;
import br.unb.cic.bionimbus.p2p.messages.EndMessage;
import br.unb.cic.bionimbus.p2p.messages.ErrorMessage;
import br.unb.cic.bionimbus.p2p.messages.JobCancelReqMessage;
import br.unb.cic.bionimbus.p2p.messages.JobCancelRespMessage;
import br.unb.cic.bionimbus.p2p.messages.JobReqMessage;
import br.unb.cic.bionimbus.p2p.messages.JobRespMessage;
import br.unb.cic.bionimbus.p2p.messages.ListReqMessage;
import br.unb.cic.bionimbus.p2p.messages.ListRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StartReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StartRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StatusReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StatusRespMessage;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskState;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.utils.Pair;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import com.google.inject.Singleton;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class SchedService extends AbstractBioService implements Service, P2PListener, Runnable {
    private static final Logger LOG = LoggerFactory
            .getLogger(SchedService.class);

    private final ConcurrentHashMap<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();

    private final ScheduledExecutorService schedExecService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("SchedService-%d").build());

    // private final ExecutorService executorService = Executors
    // .newCachedThreadPool(new BasicThreadFactory.Builder()
    // .namingPattern("SchedService-sched-%d").build());

    private final Queue<PluginTask> relocateTasks = new ConcurrentLinkedQueue<PluginTask>();

    private final Map<String, JobInfo> pendingJobs = new ConcurrentHashMap<String, JobInfo>();

    private final Map<String, JobInfo> jobsWithNoService = new ConcurrentHashMap<String, JobInfo>();

    private final Map<String, Pair<PluginInfo, PluginTask>> runningJobs = new ConcurrentHashMap<String, Pair<PluginInfo, PluginTask>>();
//    private final Map<String,  PluginTask> runningJobs = new ConcurrentHashMap<String, PluginTask>();

    private final Map<String, PluginInfo> cancelingJobs = new ConcurrentHashMap<String, PluginInfo>();

    private P2PService p2p = null;

    private SchedPolicy schedPolicy;

    private boolean isScheduling = false;

    private int isAcquiringStatus = 0;

    private int isCanceling = 0;

    private int isPending = 0;
    
    private static final String ROOT_PEER = "/peers";
    private static final String SCHED = "/sched";
    private static final String JOBS = "/jobs";
    private static final String JOB = "/job_";
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
        //Verificar o pq não funciona TO DO pode ser excluido?
        schedPolicy.setCloudMap((ConcurrentHashMap<String, PluginInfo>)getPeers());
        return schedPolicy;
    }

    @Override
    public void run() {
        System.out.println("running SchedService...");
        
        //TO DO essa chamada deve ficar aqui?
        
        
        
//		onSchedEvent();
//		Message msg = new CloudReqMessage(p2p.getPeerNode());
//		p2p.broadcast(msg);
        
        //TO DO verificar se criação de pastas pode ser aqui
    }

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
        checkPendingJobs();
        checkRunningJobs();

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
     * Executa a rotina de escalonamento.
     * @param sender
     * @param receiver 
     */
    private synchronized void scheduleJobs() {
        HashMap<JobInfo, PluginInfo> schedMap = null;

        // Caso nao exista nenhum job pendente da a chance do escalonador
        // realocar as tarefas.
        if (getPendingJobs().isEmpty()) {
            //o que o relocate faz?
            List<PluginTask> tasksToCancel = getPolicy().relocate(null);

            // Se a lista de jobs a serem realocados for vazia define o fim do
            // escalonamento e para.
            if (tasksToCancel.isEmpty()) {
                isScheduling = false;
            }

            for (PluginTask task : tasksToCancel) {
                // Cancela os jobs definidos pela politica de escalonamento.
                cancelJob(task.getJobInfo().getId());
            }
        // Caso exista algum job pendente. Escalona-os.
        } else {
            schedMap = getPolicy().schedule(getPendingJobs().values(), zkService);

            for (Map.Entry<JobInfo, PluginInfo> entry : schedMap.entrySet()) {
                JobInfo jobInfo = entry.getKey();
                PluginInfo pluginInfo = entry.getValue();
                PluginTask task = new PluginTask();
                task.setJobInfo(jobInfo);
                
                if (pluginInfo == null) {
                    // Soh entra aqui caso o servico nao esteja disponivel.
//                    sendJobResp(sender, receiver, jobInfo.getId(), null);
                    task.setState(PluginTaskState.PENDING);
                    //o que o relocate faz?
                    relocateTasks.add(task);
                } else {
                    System.out.println("SCHEDULE MSG: " + jobInfo.getId()
                            + " escalonado para " + pluginInfo.getId());

                    
                    
                    // Envia requisicao de inicio de tarefa.
//                    sendStartReq(sender, pluginInfo.getHost(), jobInfo);
                    // AJUSTAR EXECUÇÃO DE JOB          TO DO como realizar pedido de execução
                    
                    task.setState(PluginTaskState.WAITING);
                    //adiciona o job na lista de execução do servidor zookeeper
                    zkService.createEphemeralZNode(pluginInfo.getPath_zk()+SCHED+TASKS+TASK+task.getId(), task.toString());
                    //adicona o job escalonado no map de jobs em execução
                        runningJobs.put(task.getJobInfo().getId(),new Pair<PluginInfo, PluginTask>(pluginInfo, task));
                    
                    

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
    private synchronized void cancelJob(String jobId) {
        // Apenas remove dos jobs pendentes (ou seja, ainda nem foi escalonado)
        if (getPendingJobs().containsKey(jobId)) {
            getPendingJobs().remove(jobId);
            //excluir o job do zookeeper TO DO
        }
        try {
            zkService.delete(JOBS+JOB+jobId);
        
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
       // Percorre todos os jobs rodando em busca do job a ser cancelado.
//        for (Pair<PluginInfo, PluginTask> pair : getRunningJobs().values()) {
//            if (pair.second.getJobInfo().getId().equals(jobId)) {
//                
//                try {
//                    //deleta a job da lista de jobs a serem executados no plugin
//                    // TO DO verificar se é necessário conferir se o job esta em execução 
//                    zkService.delete(pair.first.getPath_zk()+SCHED+TASKS+TASK+jobId);
//                    
//                } catch (KeeperException ex) {
//                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                // Remove-o dos jobs rodando.
//                getRunningJobs().remove(pair.second.getJobInfo().getId());
//                // adiciona na lista de jobs para escalonar
//                getPendingJobs().put(pair.second.getJobInfo().getId(),pair.second.getJobInfo());
//                
//                // Lanca um evento para ser tratado pela politica de
//                // escalonamento.  TO DO  retirar?
//                getPolicy().cancelJobEvent(pair.second);
//
//                // Adiciona a lista de jobs em fase de cancelamento.
//                getCancelingJobs().put(pair.second.getJobInfo().getId(), pair.first);
                isCanceling++;
                
                // Cria e envia a requisicao de cancelamento de jobs para o plugin.  TO DO como realizar isso
//                CancelReqMessage msg = new CancelReqMessage(p2p.getPeerNode(),
//                        pair.second.getId());
                
//                p2p.broadcast(msg);
//                return;
//            }
//        }
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
     * Realiza o início do pedido de execução de um Job no recurso escanolado.
     * @param sender TO DO para que usar esse sender???
     * @param dest, endereço host do plugin de destino para execução.
     * @param jobInfo, job que será executado no plugin.
     */
    private void sendStartReq(PeerNode sender, Host dest, JobInfo jobInfo) {
        isPending++;
        
        //criar chamada para método de chamada de requisição de execução do job
        
//        StartReqMessage startMsg = new StartReqMessage(sender, jobInfo);
//        p2p.sendMessage(dest, startMsg);
        
    }

    //TO DO Excluir método...
    /* Faz a requisicao de listagem de arquivos e seus tamanhos */
    private void sendListReqEvent(PeerNode sender) {
        ListReqMessage listReqMsg = new ListReqMessage(sender);
        p2p.broadcast(listReqMsg);
    }

    //TO DO NÃO É NECESSÁRIO ESSE MÉTODO JÁ QUE QUANDO A TAREFA FOR INICIADA A EXECUÇÃO O STATUS DO JOB SERA ALTERADO NO
    // ZOOKEEPER E TODOS OS OUVINTES DO JOB SERÃO INFORMADOS.
    /* Envia resposta de inicio de job */
    private synchronized void sendJobResp(PeerNode sender, PeerNode receiver,
                                          String jobId, PluginTask task) {

        // Remove jobs da lista de jobs a serem escalonados.
        JobInfo jobInfo = getPendingJobs().remove(jobId);

        if (task == null) {
            // Jobs que nao possuem servico.
            // TODO: Sao simplesmente ignorados pelo escalonador por enquanto
            // Eh simples readiciona-los na rotina de escalonamento sempre que o
            // escalonador for
            // requisitado. Mas nao farei isso por enquanto, para evitar
            // possiveis
            // erros.
            getJobsWithNoService().put(jobInfo.getId(), jobInfo);

            // Cria e envia a mensagem de resposta.
            JobRespMessage jobRespMsg = new JobRespMessage(sender, null);
            p2p.broadcast(jobRespMsg);
        } else {
            // Adiciona job na lista de jobs "rodando" (ou seja, enviados para o
            // hadoop)
//            getRunningJobs().put(task.getJobInfo().getId(),new Pair<JobInfo, PluginTask>(jobInfo, task));

            // Cria e envia a mensagem de resposta.
            JobRespMessage jobRespMsg = new JobRespMessage(sender, jobInfo);
            p2p.broadcast(jobRespMsg);
        }

        // Define que o escalonamento acabou somente se todas os jobs ja foram
        // escalonados.
        isPending--;
        System.out.println("isPending: " + isPending);
        if (isPending == 0) {
            isScheduling = false;
        }
    }

    // TO DO retirar serviço P2P?
    @Override
    public void start(P2PService p2p) {
        //atualiza os peers da cloud
        // TO DO retirar getpolicy?c
    getPolicy();
    List<PluginInfo> listPlugin = new ArrayList<PluginInfo>(getPeers().values());
    for(PluginInfo pluginInfo : listPlugin){
            zkService.createPersistentZNode(JOBS, null);
            zkService.createPersistentZNode(pluginInfo.getPath_zk()+SCHED, null);
            zkService.createPersistentZNode(pluginInfo.getPath_zk()+SCHED+TASKS, null);
            zkService.createPersistentZNode(pluginInfo.getPath_zk()+SCHED +"/size_jobs", null);
        }
        this.p2p = p2p;
        if (p2p != null)
            p2p.addListener(this);
        
        //adicona um watcher para receber um alerta quando um novo job for criado para ser escalonado
        try {
            zkService.getData(JOBS, new SchedUpdatePeerData(zkService, this));
            zkService.getChildren(JOBS, new SchedUpdatePeerData(zkService, this));

        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
        schedExecService.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
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

    /**
     * Verifica se existe jobs na lista de jobs cancelados e adiciona eles a lista no servidor zookeeper dos JOBS a serem escalonados.
     */
    private synchronized void checkPendingJobs() {
        // Adiciona os jobs cancelados a lista de escalonamento para que conclua
        // a realocacao.
//        while (!relocateTasks.isEmpty()) {
//            PluginTask task = relocateTasks.remove();
//            getPendingJobs().put(task.getJobInfo().getId(), task.getJobInfo());
//        }
        
        
        //Adiciona os jobs cancelados a lista de jobs a serem escalonados no servidor zookeeper
        while (!relocateTasks.isEmpty()) {
            PluginTask task = relocateTasks.remove();
            try {
                
                zkService.createEphemeralZNode(JOBS+JOB+task.getId(), task.toString());
                
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    //TO DO não há necessidade de realizar requisição de informação do job, zookeeper coordena essas informações
    private synchronized void checkRunningJobs() {
        PeerNode peer = p2p.getPeerNode();
        for (String taskId : getRunningJobs().keySet()) {
            sendStatusReq(peer, taskId);
        }
        
    }
    
    /** TO DO
     * Escuta as mudanças de status dos jobs no zookeeper.
     */
    private synchronized void checkWaittingJobs() {
       
//        updateJobStatus(null, PluginTaskState.DONE);
        
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
            } catch (KeeperException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
            scheduleJobs();
        }
    }
            

    //TO DO não sei o faz esse método, faz tudo?
    @Override
    public synchronized void onEvent(P2PEvent event) {
        if (!event.getType().equals(P2PEventType.MESSAGE))
            return;

        P2PMessageEvent msgEvent = (P2PMessageEvent) event;
        Message msg = msgEvent.getMessage();
        if (msg == null)
            return;

        PeerNode sender = p2p.getPeerNode();
        PeerNode receiver = null;
        if (msg instanceof AbstractMessage) {
            receiver = ((AbstractMessage) msg).getPeer();
        }

        switch (P2PMessageType.of(msg.getType())) {
            case CLOUDRESP:
                CloudRespMessage cloudMsg = (CloudRespMessage) msg;
                for (PluginInfo info : cloudMsg.values())
                    cloudMap.put(info.getId(), info);
                break;
            case JOBREQ:
                JobReqMessage jobMsg = (JobReqMessage) msg;
                for (JobInfo jobInfo : jobMsg.values()) {
                    jobInfo.setId(UUID.randomUUID().toString());
                    jobInfo.setTimestamp(System.currentTimeMillis());
                    getPendingJobs().put(jobInfo.getId(), jobInfo);
                }
                break;
            case STARTRESP:
                StartRespMessage respMsg = (StartRespMessage) msg;
                sendJobResp(sender, receiver, respMsg.getJobId(),
                        respMsg.getPluginTask());
                break;
            case STATUSRESP:
                StatusRespMessage status = (StatusRespMessage) msg;
                updateJobStatus(status.getPluginTask());
                break;
            case JOBCANCELREQ:
                JobCancelReqMessage cancel = (JobCancelReqMessage) msg;
//                cancelJob(cancel.getPeerNode().getHost(), cancel.getJobId());
                break;
            case CANCELRESP:
                CancelRespMessage cancelResp = (CancelRespMessage) msg;
//                Pair<String, Host> pair = getCancelingJobs().get(
//                        cancelResp.getPluginTask().getId());
//                p2p.sendMessage(pair.second,
//                        new JobCancelRespMessage(p2p.getPeerNode(), pair.first));
//                finishCancelJob(cancelResp.getPluginTask());
                break;
            case LISTRESP:
                ListRespMessage listResp = (ListRespMessage) msg;
                onListRespEvent(sender, receiver, listResp);
                break;
            case END:
                EndMessage end = (EndMessage) msg;
                finalizeJob(end.getTask());
                break;
            case ERROR:
                ErrorMessage errMsg = (ErrorMessage) msg;
                LOG.warn("SCHED ERROR: type=" + errMsg.getErrorType().toString()
                        + ";msg=" + errMsg.getError());
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

    // TO DO ESCLUIR MÉTODO ???
    private synchronized void sendStatusReq(PeerNode sender, String taskId) {
        isAcquiringStatus++;
        StatusReqMessage msg = new StatusReqMessage(sender, taskId);
        p2p.broadcast(msg); // TODO: isto é realmente um broadcast?
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

        isAcquiringStatus--;
    }

    /**
     * Realiza a atualização de finalização da tarefa enviando pedido de atualização do statud no zookeeper e
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
}
