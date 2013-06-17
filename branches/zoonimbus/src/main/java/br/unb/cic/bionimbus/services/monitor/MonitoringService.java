package br.unb.cic.bionimbus.services.monitor;

import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.sched.SchedUpdatePeerData;
import br.unb.cic.bionimbus.services.sched.policy.impl.AcoSched;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class MonitoringService extends AbstractBioService implements Service, P2PListener, Runnable {

    private final ScheduledExecutorService schedExecService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("MonitorService-%d").build());

    private final Map<String, PluginTask> waitingTask = new ConcurrentHashMap<String, PluginTask>();
    
    private P2PService p2p = null;
    
    private static final String ROOT_PEER = "/peers";
    private static final String STATUS = "/STATUS";
    private static final String STATUSWAITING = "/STATUSWAITING";
    private static final String SEPARATOR = "/";
    private static final String SCHED = "/sched";
    private static final String JOBS = "/jobs";
    private static final String PREFIX_JOB = "/job_";
    private static final String PREFIX_TASK = "/task_";
    private static final String TASKS = "/tasks";

    @Inject
    public MonitoringService(final ZooKeeperService zKService) {
        this.zkService = zKService;
    }

    
    @Override
    public void run() {
        System.out.println("running MonitorService...");

        
    }

    @Override
    public void start(P2PService p2p) {
        try {
            checkRunningJobs();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        this.p2p = p2p;
//        if (p2p != null)
//            p2p.addListener(this);
        schedExecService.scheduleAtFixedRate(this, 0, 2, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
        p2p.remove(this);
        schedExecService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void event(WatchedEvent eventType) {
        System.out.print(eventType.getType().toString());
        switch(eventType.getType()){
        
            case NodeChildrenChanged:
                
                /*
                 * Criar variável para diferenciar tarefa realizada de tarefa para executar
                 */
                
            String datas;
            
            try {
                //verifica qual foi o job colocado para ser executado 
                PluginTask pluginTask = checkWattingJobs(eventType.getPath());
                
                //como retornar para executar ? TO DO
                
            } catch (KeeperException ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            }

                break;
            case NodeDataChanged:
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    datas = zkService.getData(eventType.getPath(), null);
                    PluginTask task = mapper.readValue(datas, PluginTask.class);

                    switch(task.getState()){
                        case RUNNING:

                            break;
                        case DONE:
                            
                            waitingTask.remove(task.getJobInfo().getId());
                            zkService.delete(eventType.getPath());
                            
                            break;
                        case PENDING:

                            break;
                        case CANCELLED:
                            

                            break;

                    }

                } catch (KeeperException ex) {
                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
                } 

                break;
                
            
            case NodeDeleted:
                //Trata o evento de quando o zNode status for apagado, ou seja, quando um peer estiver off-line, deve recuperar os jobs
                //que haviam sido escalonados para esse peer
                if(eventType.getPath().contains(STATUS)){
                    relocateTasks(eventType.getPath().subSequence(0, eventType.getPath().indexOf("STATUS")-1).toString());
                }
                //Trata o evento de quando o zNode referente a tarefa em execução for apagado, a tarefa já foi executada 
                if(eventType.getPath().contains(SCHED+TASKS+PREFIX_TASK)){
                    finishedTask(eventType.getPath());
                }
                    
                
            break;
        }
    }
    
    /**
     * Realiza a recuperação das tarefas que estavam em execução do peer que ficou off-line, colocando-as para 
     * serem reescalonadas. Ao final exclui o zNode STATUSWAITING para que o peer possa ser excluído do servidor zookeeper
     * @param peerPath 
     */
    private void relocateTasks(String peerPath){
        
        try {
            List<String> tasksChildren = zkService.getChildren(peerPath+SCHED+TASKS, null);

            for (String taskChild : tasksChildren) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(zkService.getData(peerPath+SCHED+TASKS+SEPARATOR+taskChild, null), PluginTask.class);
                zkService.createPersistentZNode(JOBS+PREFIX_JOB+pluginTask.getJobInfo().getId(), pluginTask.getJobInfo().toString());
            }
            
                
        
            zkService.delete(peerPath+STATUSWAITING);
        } catch (KeeperException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Verifica no zookeeper qual foi a nova tarefa colocada para execução, adiciona um watcher na tarefa e adiciona a tarefa na 
     * lista de tarefas que estão aguardando execução.
     * @param path caminho do local que foi criado a tarefa no zookeeper.
     * @return a pluginTask da tarefas adicionada para execução
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException 
     */
    private PluginTask checkWattingJobs(String path) throws KeeperException,InterruptedException,IOException{
        ObjectMapper mapper = new ObjectMapper();
        PluginTask pluginTask =null;
        List<String> tasksChildren = zkService.getChildren(path, null);
        
        for (String taskChild : tasksChildren) {
            String datasTask = zkService.getData(path+SEPARATOR+taskChild, null);
            
            PluginTask task = mapper.readValue(datasTask, PluginTask.class);
            
            //verifica se a tarefa já estava na lista para não adicionar mais de um watcher
            if(!waitingTask.containsKey(task.getJobInfo().getId())){
                // adiciona um watcher na task que foi escanolada
                zkService.getData(path+SEPARATOR+taskChild, new SchedUpdatePeerData(zkService, this));
                waitingTask.put(task.getJobInfo().getId(), task);
                pluginTask = task;
            }
        }
        return pluginTask;
    }

    /**
     * Verifica quais são as tarefas que já estão escanolada e adiciona um watcher em cada conjunto de tarefas dos plugins e nas
     * tarefas caso existam.
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException 
     */
    private void checkRunningJobs() throws KeeperException,InterruptedException, IOException{
        List<PluginInfo> plgs = new ArrayList<PluginInfo> (getPeers().values());
        List<String> listTasks;
        
        
        for (PluginInfo plugin : plgs) {
            //adiconando watch para cada peer
            //adiconando watch para cada o znode que contém as tarefas escanoladas
            listTasks = zkService.getChildren(plugin.getPath_zk()+SCHED+TASKS, new SchedUpdatePeerData(zkService,this));
            
            for (String task : listTasks) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(zkService.getData(plugin.getPath_zk()+SCHED+TASKS+SEPARATOR+task, new SchedUpdatePeerData(zkService,this)), PluginTask.class);
                
                waitingTask.put(pluginTask.getJobInfo().getId(), pluginTask);
                
            }
            //adiciona um watcher na zNode Status do peer
            zkService.getChildren(plugin.getPath_zk()+STATUS, new SchedUpdatePeerData(zkService,this));

        }
        
    }
    
    /**
     * Realiza a chamada dos métodos para a finalização da execução do job.
     * @param pathTask endereço do znode, task executada.
     */
    private void finishedTask(String pathTask){
        String idPluginTask    = pathTask.substring(pathTask.indexOf("task_"), pathTask.length());
        PluginTask pluginTaskFinish = waitingTask.remove(idPluginTask);
        
        
        //realiza a atualização do tamanho total de tarefas executadas no plugin
        new AcoSched().upDateSizeOfJobsSchedCloud(getPeers().get(pluginTaskFinish.getPluginExec()), pluginTaskFinish.getJobInfo());
    
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    @Override
    public void onEvent(P2PEvent event) {
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
//
//        if (msg instanceof AbstractMessage) {
//            receiver = ((AbstractMessage) msg).getPeer();
//        }
//
//        switch (P2PMessageType.of(msg.getType())) {
//            case JOBREQ:
//                JobReqMessage jobMsg = (JobReqMessage) msg;
//                sendSchedReq(sender, jobMsg.values());
//                break;
//            case SCHEDRESP:
//                SchedRespMessage schedMsg = (SchedRespMessage) msg;
//                JobInfo schedJob = pendingJobs.get(schedMsg.getJobId());
//                sendStartReq(sender, schedMsg.getPluginInfo().getHost(), schedJob);
//                break;
//            case STARTRESP:
//                StartRespMessage respMsg = (StartRespMessage) msg;
//                sendJobResp(sender, receiver, respMsg.getJobId(), respMsg.getPluginTask());
//                break;
//            case STATUSRESP:
//                StatusRespMessage status = (StatusRespMessage) msg;
//                updateJobStatus(status.getPluginTask());
//                break;
//            case END:
//                EndMessage end = (EndMessage) msg;
//                finalizeJob(end.getTask());
//                break;
//            case ERROR:
//                ErrorMessage errMsg = (ErrorMessage) msg;
//                System.out.println("SCHED ERROR: type="
//                        + errMsg.getErrorType().toString() + ";msg="
//                        + errMsg.getError());
//                break;
//        }
    }

//    private void sendSchedReq(PeerNode sender, Collection<JobInfo> jobList) {
//        for (JobInfo jobInfo : jobList) {
//            jobInfo.setId(UUID.randomUUID().toString());
//            pendingJobs.put(jobInfo.getId(), jobInfo);
//        }
//        SchedReqMessage newMsg = new SchedReqMessage(sender, jobList);
//        p2p.broadcast(newMsg);
//    }
//
//    private void sendJobResp(PeerNode sender, PeerNode receiver, String jobId, PluginTask task) {
//        JobInfo jobInfo = pendingJobs.remove(jobId);
////        runningJobs.put(task.getId(), new Pair<JobInfo, PluginTask>(jobInfo, task));
//        JobRespMessage jobRespMsg = new JobRespMessage(sender, jobInfo);
//        p2p.broadcast(jobRespMsg); // mandar direto pro cliente
//    }
//
//    private void sendStartReq(PeerNode sender, Host dest, JobInfo jobInfo) {
//        StartReqMessage startMsg = new StartReqMessage(sender, jobInfo);
//        p2p.sendMessage(dest, startMsg);
//    }
//
//    private void sendStatusReq(PeerNode sender, String taskId) {
//        StatusReqMessage msg = new StatusReqMessage(sender, taskId);
//        p2p.broadcast(msg); //TODO: isto é realmente um broadcast?
//    }
//
//    private void updateJobStatus(PluginTask task) {
////        Pair<JobInfo, PluginTask> pair = runningJobs.get(task.getId());
////        JobInfo job = pair.first;
//        
////        runningJobs.put(task.getId(), new Pair<JobInfo, PluginTask>(job, task));
//    }
//
//    private void finalizeJob(PluginTask task) {
//    //        Pair<JobInfo, PluginTask> pair = runningJobs.remove(task.getId());
//    //        JobInfo job = pair.first;
//        //p2p.sendMessage(new EndJobMessage(job));
//    }

    

    
}
