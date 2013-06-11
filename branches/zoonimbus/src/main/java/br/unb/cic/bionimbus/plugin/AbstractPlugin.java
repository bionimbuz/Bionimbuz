package br.unb.cic.bionimbus.plugin;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.services.messaging.Message;
import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.p2p.P2PAbstractListener;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.CancelRespMessage;
import br.unb.cic.bionimbus.p2p.messages.EndMessage;
import br.unb.cic.bionimbus.p2p.messages.GetReqMessage;
import br.unb.cic.bionimbus.p2p.messages.InfoErrorMessage;
import br.unb.cic.bionimbus.p2p.messages.InfoRespMessage;
import br.unb.cic.bionimbus.p2p.messages.PrepReqMessage;
import br.unb.cic.bionimbus.p2p.messages.PrepRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StartRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StatusRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreAckMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.p2p.messages.TaskErrorMessage;
import br.unb.cic.bionimbus.utils.GetIpMac;
import br.unb.cic.bionimbus.utils.Pair;
import com.google.inject.Inject;
import java.io.IOException;

public abstract class AbstractPlugin extends P2PAbstractListener implements Plugin, Runnable {

    private String id;

    private Future<PluginInfo> futureInfo = null;

    private PluginInfo myInfo = null;

    private String errorString = "Plugin is loading...";

    private int myCount = 0;

    private final ScheduledExecutorService schedExecutorService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("bionimbus-plugin-%d").build());

    private final ConcurrentMap<String, Pair<PluginTask, Integer>> pendingTasks = new ConcurrentHashMap<String, Pair<PluginTask, Integer>>();

    private final ConcurrentMap<String, Pair<PluginTask, Future<PluginTask>>> executingTasks = new ConcurrentHashMap<String, Pair<PluginTask, Future<PluginTask>>>();

    private final ConcurrentMap<String, Pair<PluginTask, Integer>> endingTasks = new ConcurrentHashMap<String, Pair<PluginTask, Integer>>();

    private final List<Future<PluginFile>> pendingSaves = new CopyOnWriteArrayList<Future<PluginFile>>();

    private final List<Future<PluginGetFile>> pendingGets = new CopyOnWriteArrayList<Future<PluginGetFile>>();

    private final ConcurrentMap<String, Pair<String, Integer>> inputFiles = new ConcurrentHashMap<String, Pair<String, Integer>>();

    private final ConcurrentMap<String, PluginFile> pluginFiles = new ConcurrentHashMap<String, PluginFile>();
    
    @Inject
    public AbstractPlugin(final P2PService p2p) throws IOException {
        super(p2p);
        
        //id provisório
        id = p2p.getConfig().getId();
       //id=GetIpMac.getMac();
//        id = UUID.randomUUID().toString();
//        File infoFile = new File("plugininfo.json");
//        if (infoFile.exists()) {
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                myInfo = mapper.readValue(infoFile, PluginInfo.class);
//                id = myInfo.getId();  //TODO plugininfo.json só serve para recuperar ID?
////                myInfo.setId(id);  
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    public Map<String, Pair<String, Integer>> getInputFiles() {
        return inputFiles;
    }

    protected abstract Future<PluginInfo> startGetInfo();

    protected abstract Future<PluginFile> saveFile(String filename);

    protected abstract Future<PluginGetFile> getFile(Host origin, PluginFile file, String taskId, String savePath);

    protected abstract Future<PluginTask> startTask(PluginTask task);

//    private String getId() {
    public String getId() {
        
        return id;
    }

    private Future<PluginInfo> getFutureInfo() {
        return futureInfo;
    }

    private void setFutureInfo(Future<PluginInfo> futureInfo) {
        this.futureInfo = futureInfo;
    }

    protected PluginInfo getMyInfo() {
        return myInfo;
    }
    //private void setMyInfo(PluginInfo info) {
    public void setMyInfo(PluginInfo info) {
        myInfo = info;

        //        myInfo.setId(getId());

//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.writeValue(new File("plugininfo.json"), myInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private String getErrorString() {
        return errorString;
    }

    private void setErrorString(String errorString) {
        this.errorString = errorString;
    }
    //não esta setando a p2p é isso mesmo?
    @Override
    public void setP2P(P2PService p2p) {
        // TODO Auto-generated method stub

    }

    @Override
    public void start() {
        schedExecutorService.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        schedExecutorService.shutdown();
        getP2P().remove(this);
    }

    @Override
    public void run() {
        checkGetInfo();
        checkFinishedTasks();
        checkPendingSaves();
        checkPendingGets();
    }

    private void checkGetInfo() {
        myCount++;
        if (myCount < 10)
            return;
        myCount = 0;

        Future<PluginInfo> futureInfo = getFutureInfo();
        if (futureInfo == null) {
            setFutureInfo(startGetInfo());
            return;
        }

        if (!futureInfo.isDone())
            return;

        try {
            PluginInfo newInfo = futureInfo.get();
            newInfo.setId(getId());
            newInfo.setHost(getP2P().getPeerNode().getHost());
            setMyInfo(newInfo);
        } catch (Exception e) {
            setErrorString(e.getMessage());
            setMyInfo(null);
        }

        setFutureInfo(null);
    }

    private void checkFinishedTasks() {
        P2PService p2p = getP2P();
        Future<PluginTask> futureTask;
        PluginTask task;

        for (Pair<PluginTask, Future<PluginTask>> pair : executingTasks.values()) {
            futureTask = pair.second;

            if (!futureTask.isDone())
                continue;

            try {
                task = futureTask.get();
            } catch (Exception e) {
                task = pair.first;
                Message msg = new TaskErrorMessage(p2p.getPeerNode(), getId(), task.getId(), e.getMessage());
                p2p.broadcast(msg);
                continue;
            }

            executingTasks.remove(task.getId());

            if (task.getJobInfo().getOutputs().size() > 0) {
                int count = 0;
                for (String output : task.getJobInfo().getOutputs()) {
                    File file = new File(p2p.getConfig().getServerPath() + "/" + output);
                    FileInfo info = new FileInfo();
                    info.setName(p2p.getConfig().getServerPath() + "/" + output);
                    info.setSize(file.length());
                    StoreReqMessage msg = new StoreReqMessage(p2p.getPeerNode(), info, task.getId());
                    p2p.broadcast(msg);
                    count++;
                }
                endingTasks.put(task.getId(), new Pair<PluginTask, Integer>(task, count));
            } else {
                EndMessage endMsg = new EndMessage(p2p.getPeerNode(), task);
                p2p.broadcast(endMsg);
            }
        }
    }

    private void checkPendingSaves() {
        P2PService p2p = getP2P();

        for (Future<PluginFile> f : pendingSaves) {

            if (!f.isDone())
                continue;

            try {
                PluginFile file = f.get();
                file.setPluginId(getId());
                pendingSaves.remove(f);
                pluginFiles.put(file.getId(), file);
                StoreAckMessage msg = new StoreAckMessage(p2p.getPeerNode(), file);
                p2p.broadcast(msg);
            } catch (Exception e) {
                e.printStackTrace();
                //TODO criar mensagem de erro?
            }
        }
    }

    private void checkPendingGets() {
        P2PService p2p = getP2P();

        for (Future<PluginGetFile> f : pendingGets) {

            if (!f.isDone())
                continue;

            try {
                PluginGetFile get = f.get();
                pendingGets.remove(f);
                Message msg = new PrepRespMessage(p2p.getPeerNode(), getMyInfo(), get.getPluginFile(), get.getTaskId());
                p2p.sendMessage(get.getPeer(), msg);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO criar mensagem de erro?
            }
        }
    }

    @Override
    protected void recvFile(File file, Map<String, String> parms) {
        if (parms.isEmpty()) {
            Future<PluginFile> save = saveFile(file.getPath());
            pendingSaves.add(save);
            return;
        }

        String fileId = parms.get("fileId");
        String fileName = parms.get("fileName");
        Pair<String, Integer> inputFile = inputFiles.get(fileId);
        int count = 0;
        if (inputFile != null)
            count = inputFile.second;
        count++;
        inputFiles.put(fileId, new Pair<String, Integer>(fileName, count));


        String taskId = parms.get("taskId");
        Pair<PluginTask, Integer> pair = pendingTasks.get(taskId);
        count = pair.second;
        if (--count == 0) {
            pendingTasks.remove(taskId);
            Future<PluginTask> futureTask = startTask(pair.first);
            Pair<PluginTask, Future<PluginTask>> fPair = Pair.of(pair.first, futureTask);
            executingTasks.put(pair.first.getId(), fPair);
            return;
        }

        Pair<PluginTask, Integer> newPair = new Pair<PluginTask, Integer>(pair.first, count);
        pendingTasks.put(taskId, newPair);
    }

    @Override
    protected void recvInfoReq(Host origin) {
        Message message;
        P2PService p2p = getP2P();

        if (getMyInfo() == null)
            message = new InfoErrorMessage(p2p.getPeerNode(), getId(), getErrorString());
        else
            message = new InfoRespMessage(p2p.getPeerNode(), getMyInfo());

        p2p.sendMessage(origin, message);
    }

    @Override
    protected void recvInfoResp(Host origin, PluginInfo info) {
    }

    @Override
    protected void recvStartReq(Host origin, JobInfo job) {
        P2PService p2p = getP2P();
        PluginService service = getMyInfo().getService(job.getServiceId());
        if (service == null)
            return;

        PluginTask task = new PluginTask();
        task.setJobInfo(job);
        if (!job.getInputs().isEmpty()) {
            pendingTasks.put(task.getId(), new Pair<PluginTask, Integer>(task, job.getInputs().size()));
            for (Pair<String, Long> pair : job.getInputs()) {
                String fileId = pair.first;
                p2p.broadcast(new GetReqMessage(p2p.getPeerNode(), fileId, task.getId()));
            }
        } else {
            Future<PluginTask> futureTask = startTask(task);
            Pair<PluginTask, Future<PluginTask>> pair = Pair.of(task, futureTask);
            executingTasks.put(task.getId(), pair);
        }

        StartRespMessage resp = new StartRespMessage(p2p.getPeerNode(), job.getId(), task);
        p2p.sendMessage(origin, resp);
    }

    @Override
    protected void recvStartResp(Host origin, String jobId, PluginTask task) {
    }

    @Override
    protected void recvEnd(Host origin, PluginTask task) {
    }

    @Override
    protected void recvStatusReq(Host origin, String taskId) {
        P2PService p2p = getP2P();
        PluginTask task = null;

        if (pendingTasks.containsKey(taskId)) {
            task = pendingTasks.get(taskId).first;
            task.setState(PluginTaskState.PENDING);
        } else if (executingTasks.containsKey(taskId)) {
            task = executingTasks.get(taskId).first;
        } else if (endingTasks.containsKey(taskId)) {
            task = endingTasks.get(taskId).first;
            task.setState(PluginTaskState.DONE);
        }

        if (task != null) {
            StatusRespMessage msg = new StatusRespMessage(p2p.getPeerNode(), task);
            p2p.sendMessage(origin, msg);
        }
    }

    @Override
    protected void recvStatusResp(Host origin, PluginTask task) {
    }

    @Override
    protected void recvStoreReq(Host origin, FileInfo file, String taskId) {
    }

    @Override
    protected void recvStoreResp(Host origin, PluginInfo plugin, FileInfo file, String taskId) {
        if (taskId.length() <= 0)
            return;

        P2PService p2p = getP2P();
        if (plugin.equals(getMyInfo())) {
            Future<PluginFile> save = saveFile(file.getName());
            pendingSaves.add(save);
        } else {
            p2p.sendFile(plugin.getHost(), file.getName());
        }

        Pair<PluginTask, Integer> pair = endingTasks.get(taskId);
        int count = pair.second;
        count--;

        if (count == 0) {
            endingTasks.remove(taskId);
            EndMessage endMsg = new EndMessage(p2p.getPeerNode(), pair.first);
            p2p.broadcast(endMsg);
        } else {
            Pair<PluginTask, Integer> newPair = new Pair<PluginTask, Integer>(pair.first, count);
            endingTasks.put(taskId, newPair);
        }
    }

    @Override
    protected void recvStoreAck(Host origin, PluginFile file) {
    }

    @Override
    protected void recvGetReq(Host origin, String fileId, String taskId) {
    }

    @Override
    protected void recvGetResp(Host origin, PluginInfo plugin, PluginFile file, String taskId) {
        P2PService p2p = getP2P();
        Message msg = new PrepReqMessage(p2p.getPeerNode(), file, taskId);
        p2p.sendMessage(plugin.getHost(), msg);
    }

    @Override
    protected void recvCloudReq(Host origin) {
    }

    @Override
    protected void recvCloudResp(Host origin, Collection<PluginInfo> plugins) {
    }

    @Override
    protected void recvSchedReq(Host origin, Collection<JobInfo> jobList) {
    }

    @Override
    protected void recvSchedResp(Host origin, String jobId, PluginInfo plugin) {
    }

    @Override
    protected void recvJobReq(Host origin, Collection<JobInfo> jobList) {
    }

    @Override
    protected void recvJobResp(Host origin, JobInfo job) {
    }

    @Override
    protected void recvError(Host origin, String error) {
    }

    @Override
    protected void recvPingReq(Host origin, long timestamp) {
    }

    @Override
    protected void recvPingResp(Host origin, long timestamp) {
    }

    @Override
    protected void recvListReq(Host origin) {
    }

    @Override
    protected void recvListResp(Host origin, Collection<PluginFile> files) {
    }

    @Override
    protected void recvPrepReq(Host origin, PluginFile file, String taskId) {
        Future<PluginGetFile> f = getFile(origin, file, taskId, getP2P().getConfig().getServerPath());
        pendingGets.add(f);
    }

    @Override
    protected void recvPrepResp(Host origin, PluginInfo plugin, PluginFile file, String taskId) {
        Map<String, String> parms = new HashMap<String, String>();
        parms.put("taskId", taskId);
        parms.put("fileId", file.getId());
        parms.put("fileName", file.getPath());

        if (plugin.equals(getMyInfo())) {
            recvFile(new File(file.getPath()), parms);
        } else {
            getP2P().getFile(plugin.getHost(), file.getPath(), parms);
        }
    }

    @Override
    protected void recvCancelReq(Host origin, String taskId) {
        P2PService p2p = getP2P();
        PluginTask task = null;

        if (executingTasks.containsKey(taskId)) {
            Pair<PluginTask, Future<PluginTask>> pair = executingTasks.remove(taskId);
            task = pair.first;
            pair.second.cancel(true);
        } else if (pendingTasks.containsKey(taskId)) {
            task = pendingTasks.remove(taskId).first;
        } else if (endingTasks.containsKey(taskId)) {
            task = endingTasks.remove(taskId).first;
        }

        CancelRespMessage msg = new CancelRespMessage(p2p.getPeerNode(), task);
        p2p.sendMessage(origin, msg);
    }

    @Override
    protected void recvCancelResp(Host origin, PluginTask task) {
    }

    @Override
    protected void recvJobCancelReq(Host origin, String jobId) {
    }

    @Override
    protected void recvJobCancelResp(Host origin, String jobId) {
    }
}
