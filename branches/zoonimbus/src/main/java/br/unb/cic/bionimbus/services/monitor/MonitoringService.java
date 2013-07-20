package br.unb.cic.bionimbus.services.monitor;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskState;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.hsqldb.lib.Storage;

@Singleton
public class MonitoringService extends AbstractBioService implements Service, P2PListener, Runnable {

    private final ScheduledExecutorService schedExecService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("MonitorService-%d").build());
    private final Map<String, PluginTask> waitingTask = new ConcurrentHashMap<String, PluginTask>();
    private final List<String> waitingJobs = new ArrayList<String>();
    private final List<String> waitingFiles = new ArrayList<String>();
    
    private final Collection<String> plugins = new ArrayList<String>();
    private P2PService p2p = null;
    private static final String ROOT_PEER = "/peers";
    private static final String TASKS = "/tasks";
    private static final String SCHED = "/sched";
    private static final String STATUS = "/STATUS";
    private static final String STATUSWAITING = "/STATUSWAITING";
    private static final String SEPARATOR = "/";
    private static final int PORT = 8080;

    @Inject
    public MonitoringService(final ZooKeeperService zKService) {
        this.zkService = zKService;
    }

//    @Override
    public void run() {
        checkPeersStatus();
        checkJobsTasks();
        checkPendingSave();
    }

    @Override
    public void start(P2PService p2p) {
        try {
            checkPeers();
//            checkPendingSave();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        this.p2p = p2p;
//        if (p2p != null)
//            p2p.addListener(this);
        schedExecService.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
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
        String path = eventType.getPath();
        try {
            switch (eventType.getType()) {

                case NodeCreated:

                    System.out.print(path + "= NodeCreated");
                    break;
                case NodeChildrenChanged:
                        if(eventType.getPath().equals(ROOT_PEER))
                            if(plugins.size()<getPeers().size()){
                                verifyPlugins();
                            }
                    System.out.print(path + "= NodeChildrenChanged");
                    break;
                case NodeDeleted:
                    String peerPath = path.subSequence(0, path.indexOf("STATUS") - 1).toString();
                    if (path.contains(STATUSWAITING)) {
                        deletePeer(peerPath);
                    }
                    break;
            }
        } catch (KeeperException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void verifyPlugins() {
        Collection<PluginInfo> temp  = getPeers().values();
        temp.removeAll(plugins);
        for(PluginInfo plugin : temp){
            try {
                if(zkService.getZNodeExist(zkService.getPath().STATUS.getFullPath(plugin.getId(), null, null), false))
                    zkService.getData(zkService.getPath().STATUS.getFullPath(plugin.getId(), "", ""), new UpdatePeerData(zkService, this));
            } catch (KeeperException ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }

    /**
     * Verifica se os jobs que estava aguardando escalonamento e as tarefas que
     * já foram escalonadas ainda estão com o mesmo status da última leitura.
     */
    private void checkJobsTasks() {
        try {

            for (String job : zkService.getChildren(zkService.getPath().JOBS.toString(), null)) {
                //verifica se o job já estava na lista, recupera e lança novamente os dados para disparar watchers
                if (waitingJobs.contains(job)) {
                    String datas = zkService.getData(zkService.getPath().JOBS.toString() + SEPARATOR + job, null);
                    // remove e cria task novamente para que os watchers sejam disparados e execute essa tarefa
                    zkService.delete(zkService.getPath().JOBS.toString() + SEPARATOR + job);
                    zkService.createPersistentZNode(zkService.getPath().JOBS.toString() + SEPARATOR + job, datas);
                    waitingJobs.remove(job);
                } else {
                    waitingJobs.add(job);
                }

            }

            for (PluginInfo peer : getPeers().values()) {
                for (String task : zkService.getChildren(zkService.getPath().TASKS.getFullPath(peer.getId(), "", ""), null)) {
                    String datas =  zkService.getData(zkService.getPath().PREFIX_TASK.getFullPath(peer.getId(), "", task.substring(5, task.length())), null);
                        
                    if(datas!=null && datas.isEmpty()){
                        PluginTask pluginTask = new ObjectMapper().readValue(datas, PluginTask.class);
                        //verifica se o job já estava na lista, recupera e lança novamente os dados para disparar watchers                    if(count ==1){
                        if (pluginTask.getState() == PluginTaskState.PENDING) {
                            if (waitingTask.containsKey(task)) {
                                //condição para verificar se a tarefa está sendo utilizada
                                if(zkService.getZNodeExist(zkService.getPath().PREFIX_TASK.getFullPath(peer.getId(), "", task.substring(5, task.length())), false)){
                                    zkService.delete(zkService.getPath().PREFIX_TASK.getFullPath(peer.getId(), "", task.substring(5, task.length())));
                                    zkService.createPersistentZNode(zkService.getPath().PREFIX_TASK.getFullPath(peer.getId(), "", task.substring(5, task.length())), pluginTask.toString());
                                }
                                waitingJobs.remove(task);
                            } else {
                                waitingTask.put(task, pluginTask);
                            }
                        }
                    }
                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Realiza a verificação dos peers existentes identificando se existe algum
     * peer aguardando recuperação, se o peer estiver off-line e a recuperação
     * já estiver sido feito, realiza a limpeza do peer. Realizada apenas quando
     * o módulo inicia.
     */
    private void checkPeersStatus() {
        try {
            List<String> listPeers = zkService.getChildren(zkService.getPath().PEERS.toString(), new UpdatePeerData(zkService, this));
            for (String peerPath : listPeers) {
//                if(!plugins.contains(peerPath)){
//                    plugins.add(peerPath);
//                    RpcClient rpcClient = new AvroClient("http", plugin.getHost().getAddress(), PORT);
//                    rpcClient.getProxy().setWatcher(plugin.getId());
//                    rpcClient.close();

                    if (zkService.getZNodeExist(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, false)) {
                        //TO DO descomentar linha abaixo caso o storage estiver fazendo a recuperação do peer 
                        if (zkService.getData(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, null).contains("S") && zkService.getData(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, null).contains("E")) {
                            deletePeer(ROOT_PEER + SEPARATOR + peerPath);
                        }
                    }
//                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
     /**
      * Verifica se algum arquivo está pendente há algum tempo(duas vezes o tempo de execução da monitoring), e se estiver
      * apaga e cria novamente o arquivo para que os seus watcher informem sua existência.
      */
      private void checkPendingSave(){ 
          try { 
                List<String> listPendingSaves= zkService.getChildren(zkService.getPath().PENDING_SAVE.getFullPath("", "", ""), null);
                if(listPendingSaves!=null && !listPendingSaves.isEmpty()){
                    
                    for (String filePending : listPendingSaves) {
                        String datas =  zkService.getData(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", filePending.substring(13, filePending.length()), ""), null);
                        
                        if(datas!=null && datas.isEmpty()){

                            //verifica se o arquivo já estava na lista, recupera e lança novamente os dados para disparar watchers
                            if (waitingFiles.contains(filePending)) {
                                PluginInfo pluginInfo = new ObjectMapper().readValue(datas, PluginInfo.class);
                                //condição para verificar se arquivo na pending ainda existe
                                if(zkService.getZNodeExist(zkService.getPath().PENDING_SAVE.getFullPath("", filePending.substring(13, filePending.length()), ""), false)){
                                    zkService.delete(zkService.getPath().PENDING_SAVE.getFullPath("", filePending.substring(13, filePending.length()),""));
                                    zkService.createPersistentZNode(zkService.getPath().PENDING_SAVE.getFullPath("", filePending.substring(13, filePending.length()),""), pluginInfo.toString());
                                }
                                waitingFiles.remove(filePending);
                            } else {
                                waitingFiles.add(filePending);
                            }
                        }
                        
                    }
                    
                }
            } catch (KeeperException ex) {
              Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex); 
            } catch (IOException ex) {
              Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex); 
            } catch (InterruptedException ex) {
              Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex); 
            } 
      
      }
     
    /**
     * Incia o processo de recuperação dos peers caso ainda não tenho sido
     * iniciado e adiciona um watcher nos peer on-lines.
     */
    private void checkPeers() {
        try {
            //executa a verificação inicial para ver se os peers estão on-line, adiciona um watcher para avisar quando o peer ficar off-line
            List<String> listPeers = zkService.getChildren(ROOT_PEER, null);
            for (String peerPath : listPeers) {
                    if (zkService.getZNodeExist(ROOT_PEER + SEPARATOR + peerPath + STATUS, false)) {
                        //adicionando wacth
                        zkService.getData(ROOT_PEER + SEPARATOR + peerPath + STATUS, new UpdatePeerData(zkService, this));

                    }
                    //verifica se algum plugin havia ficado off e não foi realizado sua recuperação
                    if (!zkService.getZNodeExist(ROOT_PEER + SEPARATOR + peerPath + STATUS, false)
                            && !zkService.getZNodeExist(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, false)) {
                        zkService.createPersistentZNode(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, "");
                        zkService.getData(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, new UpdatePeerData(zkService, this));
                    }
                    plugins.add(peerPath);
            }
        } catch (KeeperException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void deletePeer(String peerPath) throws InterruptedException, KeeperException {
        if (!zkService.getZNodeExist(peerPath + STATUS, false) && zkService.getZNodeExist(peerPath + STATUSWAITING, false)) {
            zkService.delete(peerPath);
        }
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
