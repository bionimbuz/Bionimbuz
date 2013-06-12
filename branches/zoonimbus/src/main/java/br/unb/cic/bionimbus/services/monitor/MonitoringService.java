package br.unb.cic.bionimbus.services.monitor;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.google.inject.Singleton;

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
import br.unb.cic.bionimbus.p2p.messages.EndMessage;
import br.unb.cic.bionimbus.p2p.messages.ErrorMessage;
import br.unb.cic.bionimbus.p2p.messages.JobReqMessage;
import br.unb.cic.bionimbus.p2p.messages.JobRespMessage;
import br.unb.cic.bionimbus.p2p.messages.SchedReqMessage;
import br.unb.cic.bionimbus.p2p.messages.SchedRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StartReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StartRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StatusReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StatusRespMessage;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.sched.SchedUpdatePeerData;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class MonitoringService extends AbstractBioService implements Service, P2PListener, Runnable {

    private final ScheduledExecutorService schedExecService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("MonitorService-%d").build());

    private final Map<String, JobInfo> pendingJobs = new ConcurrentHashMap<String, JobInfo>();

//    private final Map<String, Pair<JobInfo, PluginTask>> runningJobs = new ConcurrentHashMap<String, Pair<JobInfo, PluginTask>>();
    private final Map<String, PluginTask> runningJobs = new ConcurrentHashMap<String, PluginTask>();
    
    private P2PService p2p = null;
    
    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private static final String SCHED = "/sched";
    private static final String JOBS = "/jobs";
    private static final String JOB = "/job_";
    private static final String TASK = "/task_";
    private static final String TASKS = "/tasks";
    private int jobsPendentes = 0;

    @Inject
    public MonitoringService(final ZooKeeperService zKService) {
        this.zkService = zKService;
    }

    
//    @Override
    public void run() {
        System.out.println("running MonitorService...");

                
        
    }

    @Override
    public void start(P2PService p2p) {
        try {
            checkPendingJobs();
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
            String datas;
            try {
                //verifica qual foi o job colocado para ser executado e adiciona um watcher nele
                datas = zkService.getData(eventType.getPath(), new SchedUpdatePeerData(zkService, this));
                ObjectMapper mapper = new ObjectMapper();
                PluginTask task = mapper.readValue(datas, PluginTask.class);
                runningJobs.put(task.getId(), task);
                //apaga o job do job a ser escalonado
                zkService.delete(JOBS+SEPARATOR+task.getJobInfo().getId());
            } catch (KeeperException ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            }

            //como realizar o pedido de execução???? TO DO
//            scheduleJobs();
                break;
            case NodeDataChanged:
            //                String datas;
//                try {
//                    Watcher watch=null;
//                    ObjectMapper mapper = new ObjectMapper();
//                    datas = zkService.getData(eventType.getPath(), watch);
//                    PluginTask task = mapper.readValue(datas, PluginTask.class);
//
//                    switch(task.getState()){
//                        case RUNNING:
//
//                            break;
//                        case DONE:
//
//                            zkService.delete(eventType.getPath());
//                            watch=new SchedUpdatePeerData(zkService, this);
//
//                            break;
//                        case PENDING:
//
//                            break;
//                        case CANCELLED:
//
//
//                            break;
//
//                    }
//                    //adiciona um watcher 
//                    zkService.getData(eventType.getPath(), watch);
//
//                } catch (KeeperException ex) {
//                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (Exception ex) {
//                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
//                } 

            //como realizar o pedido de execução???? TO DO
//            scheduleJobs();
                break;
            case NodeDeleted:
//                String datas;
//                try {
//                    Watcher watch=null;
//                    ObjectMapper mapper = new ObjectMapper();
//                    datas = zkService.getData(eventType.getPath(), watch);
//                    PluginTask task = mapper.readValue(datas, PluginTask.class);
//
//                    switch(task.getState()){
//                        case RUNNING:
//
//                            break;
//                        case DONE:
//
//                            zkService.delete(eventType.getPath());
//                            watch=new SchedUpdatePeerData(zkService, this);
//
//                            break;
//                        case PENDING:
//
//                            break;
//                        case CANCELLED:
//
//
//                            break;
//
//                    }
//                    //adiciona um watcher 
//                    zkService.getData(eventType.getPath(), watch);
//
//                } catch (KeeperException ex) {
//                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (Exception ex) {
//                    java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                //como realizar o pedido de execução???? TO DO
//    //            scheduleJobs();
                break;
        }
    }

    /**
     * Verifica no zookeeper quais são os jobs pendentes, jobs que ainda não foram escalonados.
     */
    private void checkPendingJobs() throws KeeperException,InterruptedException{
        Watcher watch = new SchedUpdatePeerData(zkService,this);
        List<String> jobsPending;
        
//        jobsPending = zkService.getChildren(ROOT_PEER+JOBS, watch);

    }

    private void checkRunningJobs() throws KeeperException,InterruptedException, IOException{
        List<PluginInfo> plgs;
        List<String> listTasks;
        
        plgs = (List)getPeers().values();

        for (PluginInfo plugin : plgs) {
            
            listTasks = zkService.getChildren(plugin.getPath_zk()+SCHED+TASKS, new SchedUpdatePeerData(zkService,this));
            
            for (String task : listTasks) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(zkService.getData(plugin.getPath_zk()+SCHED+TASKS+SEPARATOR+task, new SchedUpdatePeerData(zkService,this)), PluginTask.class);
                
                runningJobs.put(plugin.getId(), pluginTask);
                
            }

        }
        
//        PeerNode peer = p2p.getPeerNode();
//        for (String taskId : runningJobs.keySet()) {
//            sendStatusReq(peer, taskId);
//        }
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
