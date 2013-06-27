package br.unb.cic.bionimbus.plugin.hadoop;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.services.messaging.Message;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PEventType;
import br.unb.cic.bionimbus.p2p.P2PFileEvent;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PMessageEvent;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.p2p.messages.AbstractMessage;
import br.unb.cic.bionimbus.p2p.messages.CancelReqMessage;
import br.unb.cic.bionimbus.p2p.messages.CancelRespMessage;
import br.unb.cic.bionimbus.p2p.messages.EndMessage;
import br.unb.cic.bionimbus.p2p.messages.GetReqMessage;
import br.unb.cic.bionimbus.p2p.messages.GetRespMessage;
import br.unb.cic.bionimbus.p2p.messages.InfoErrorMessage;
import br.unb.cic.bionimbus.p2p.messages.InfoRespMessage;
import br.unb.cic.bionimbus.p2p.messages.PrepReqMessage;
import br.unb.cic.bionimbus.p2p.messages.PrepRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StartReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StartRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StatusReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StatusRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreAckMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreRespMessage;
import br.unb.cic.bionimbus.p2p.messages.TaskErrorMessage;
import br.unb.cic.bionimbus.plugin.Plugin;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskState;
import br.unb.cic.bionimbus.utils.Pair;
import java.util.ArrayList;

public class HadoopPlugin implements Plugin, P2PListener, Runnable {

    private String id = UUID.randomUUID().toString();

    private final ScheduledExecutorService schedExecutorService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("HadoopPlugin-%d").build());

    private final ExecutorService executorService = Executors
            .newCachedThreadPool(new BasicThreadFactory.Builder()
                    .namingPattern("HadoopPlugin-workers-%d").build());

    private Future<PluginInfo> fInfo = null;

    private PluginInfo myInfo = null;

    private String errorString = "Plugin is loading...";

    private int myCount = 0;

    private final ConcurrentMap<String, Pair<PluginTask, Integer>> pendingTasks = new ConcurrentHashMap<String, Pair<PluginTask, Integer>>();

    private final ConcurrentMap<String, Pair<PluginTask, Future<PluginTask>>> executingTasks = new ConcurrentHashMap<String, Pair<PluginTask, Future<PluginTask>>>();

    private final ConcurrentMap<String, Pair<PluginTask, Integer>> endingTasks = new ConcurrentHashMap<String, Pair<PluginTask, Integer>>();

    private final List<Future<PluginFile>> pendingSaves = new CopyOnWriteArrayList<Future<PluginFile>>();

    private final List<Future<HadoopGetFile>> pendingGets = new CopyOnWriteArrayList<Future<HadoopGetFile>>();

    private final ConcurrentMap<String, PluginFile> pluginFiles = new ConcurrentHashMap<String, PluginFile>();

    private final ConcurrentMap<String, Pair<String, Integer>> inputFiles = new ConcurrentHashMap<String, Pair<String, Integer>>();

    private P2PService p2p;

    public HadoopPlugin() {
        File infoFile = new File("plugininfo.json");
        if (infoFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                myInfo = mapper.readValue(infoFile, PluginInfo.class);
                id = myInfo.getId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Pair<String, Integer>> getInputFiles() {
        return inputFiles;
    }

    @Override
    public void run() {
        checkGetInfo();
        checkFinishedTasks();
        checkPendingSaves();
        checkPendingGets();
    }

    private void checkFinishedTasks() {
        Future<PluginTask> fTask;
        PluginTask task;

        for (Pair<PluginTask, Future<PluginTask>> pair : executingTasks.values()) {
            fTask = pair.second;

            if (fTask.isDone()) {
                try {
                    task = fTask.get();
                } catch (Exception e) {
                    task = pair.first;
                    Message msg = new TaskErrorMessage(p2p.getPeerNode(), id, task.getId(), e.getMessage());
                    p2p.broadcast(msg);
                    continue;
                }

                executingTasks.remove(task.getId());

                if (!task.getJobInfo().getOutputs().isEmpty()) {
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
    }

    private Message buildFinishedGetInfoMsg(PluginInfo info) {
        if (info == null)
            return new InfoErrorMessage(p2p.getPeerNode(), id, errorString);
        return new InfoRespMessage(p2p.getPeerNode(), info);
    }

    private void checkGetInfo() {
        myCount++;
        if (myCount < 10)
            return;
        myCount = 0;

        if (fInfo == null) {
            fInfo = executorService.submit(new HadoopGetInfo());
            return;
        }

        if (fInfo.isDone()) {
            try {
                PluginInfo newInfo = fInfo.get();
                newInfo.setId(id);
                newInfo.setHost(p2p.getPeerNode().getHost());
                myInfo = newInfo;
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(new File("plugininfo.json"), myInfo);
            } catch (Exception e) {
                e.printStackTrace();
                errorString = e.getMessage();
                myInfo = null;
            }
            fInfo = null;
        }
    }

    private void checkTaskStatus(PeerNode receiver, String taskId) {
        PluginTask task = null;

        if (pendingTasks.containsKey(taskId)) {
            task = pendingTasks.get(taskId).first;
            task.setState(PluginTaskState.PENDING);
        } else if (executingTasks.containsKey(taskId)) {
            task = executingTasks.get(taskId).first;
            try {
                HadoopGetInfo.getTaskInfo(task);
            } catch (Exception ex) {
                //TODO: O que fazer no erro?
                // Por enquanto printa erro no servidor.
                ex.printStackTrace();
            }

        } else if (endingTasks.containsKey(taskId)) {
            task = endingTasks.get(taskId).first;
            task.setState(PluginTaskState.DONE);
        }

        if (task != null) {
            System.out.println(task.getJobInfo().getId() + "(" + task.getId() + "|" + task.getJobInfo().getLocalId() + ") : " + task.getState());
            StatusRespMessage msg = new StatusRespMessage(p2p.getPeerNode(), task);
            p2p.sendMessage(receiver.getHost(), msg);
        }
    }

    private void checkPendingSaves() {
        for (Future<PluginFile> f : pendingSaves) {
            if (f.isDone()) {
                try {
                    PluginFile file = f.get();
                    //verificar depois lista de ids do zookeeper
                    List<String> pluginsIds = new ArrayList<String>();
                    pluginsIds.add(this.id);
                    file.setPluginId(pluginsIds);
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
    }

    private void checkPendingGets() {
        for (Future<HadoopGetFile> f : pendingGets) {
            if (f.isDone()) {
                try {
                    HadoopGetFile get = f.get();
                    pendingGets.remove(f);
                    Message msg = new PrepRespMessage(p2p.getPeerNode(), myInfo, get.getPluginFile(), get.getTaskId());
                    p2p.sendMessage(get.getReceiver().getHost(), msg);
                } catch (Exception e) {
                    // TODO criar mensagem de erro?
                    e.printStackTrace();
                }
            }
        }
    }

    private void storeFile(File file, Map<String, String> parms) {
        if (parms.isEmpty()) {
            System.out.println("recebi arquivo " + file.getPath());
            Future<PluginFile> f = executorService.submit(new HadoopSaveFile(file.getPath()));
            pendingSaves.add(f);
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
        if (pair == null)
            return;

        count = pair.second;
        if (--count == 0) {
            pendingTasks.remove(taskId);
            startTask(pair.first);
            return;
        }

        Pair<PluginTask, Integer> newPair = new Pair<PluginTask, Integer>(pair.first, count);
        pendingTasks.put(taskId, newPair);
    }

    private void storeFile(P2PEvent event) {
        P2PFileEvent fileEvent = (P2PFileEvent) event;
        storeFile(fileEvent.getFile(), fileEvent.getParms());
    }

    private void getFileFromHadoop(PluginFile file, String taskId, PeerNode receiver) {
        Future<HadoopGetFile> f = executorService.submit(new HadoopGetFile(file, taskId, receiver, p2p.getConfig().getServerPath()));
        pendingGets.add(f);
    }

    @Override
    public void start() {
        System.out.println("starting Hadoop plugin...");
        schedExecutorService.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        executorService.shutdownNow();
        schedExecutorService.shutdownNow();
        p2p.remove(this);
    }

    @Override
    public void setP2P(P2PService p2p) {
        if (this.p2p != null)
            this.p2p.remove(this);

        this.p2p = p2p;

        if (this.p2p != null)
            this.p2p.addListener(this);
    }

    @Override
    public void onEvent(P2PEvent event) {
        if (event.getType().equals(P2PEventType.FILE)) {
            storeFile(event);
            return;
        } else if (!event.getType().equals(P2PEventType.MESSAGE))
            return;

        P2PMessageEvent msgEvent = (P2PMessageEvent) event;
        Message msg = msgEvent.getMessage();
        if (msg == null)
            return;

        PeerNode receiver = null;
        if (msg instanceof AbstractMessage) {
            receiver = ((AbstractMessage) msg).getPeer();
        }

        switch (P2PMessageType.of(msg.getType())) {
            case INFOREQ:
                Message infoMsg = buildFinishedGetInfoMsg(myInfo);
                p2p.sendMessage(receiver.getHost(), infoMsg);
                break;
            case STARTREQ:
                JobInfo job = ((StartReqMessage) msg).getJobInfo();
                StartRespMessage resp = new StartRespMessage(p2p.getPeerNode(), job.getId(), prepareTask(job));
                p2p.sendMessage(receiver.getHost(), resp);
                break;
            case STATUSREQ:
                StatusReqMessage reqMsg = (StatusReqMessage) msg;
                checkTaskStatus(receiver, reqMsg.getTaskId());
                break;
            case PREPREQ:
                PrepReqMessage prepMsg = (PrepReqMessage) msg;
                getFileFromHadoop(prepMsg.getPluginFile(), prepMsg.getTaskId(), receiver);
                break;
            case GETRESP:
                GetRespMessage getMsg = (GetRespMessage) msg;
                p2p.sendMessage(getMsg.getPluginInfo().getHost(), new PrepReqMessage(p2p.getPeerNode(), getMsg.getPluginFile(), getMsg.getTaskId()));
                break;
            case PREPRESP:
                PrepRespMessage respMsg = (PrepRespMessage) msg;
                Map<String, String> parms = new HashMap<String, String>();
                parms.put("taskId", respMsg.getTaskId());
                parms.put("fileId", respMsg.getPluginFile().getId());
                parms.put("fileName", respMsg.getPluginFile().getPath());

                if (respMsg.getPluginInfo().equals(myInfo)) {
                    storeFile(new File(respMsg.getPluginFile().getPath()), parms);
                } else {
                    p2p.getFile(respMsg.getPluginInfo().getHost(), respMsg.getPluginFile().getPath(), parms);
                }
                break;
            case STORERESP:
                StoreRespMessage storeMsg = (StoreRespMessage) msg;
                endTask(storeMsg.getTaskId(), storeMsg.getPluginInfo(), storeMsg.getFileInfo());
                break;
            case CANCELREQ:
                CancelReqMessage cancelMsg = (CancelReqMessage) msg;
                cancelTask(receiver, cancelMsg.getTaskId());
                break;
        }
    }

    private PluginTask prepareTask(JobInfo job) {
        PluginService service = myInfo.getService(job.getServiceId());
        if (service == null)
            return null;

        PluginTask task = new PluginTask();
        task.setJobInfo(job);
        if (!job.getInputs().isEmpty()) {
            pendingTasks.put(task.getId(), new Pair<PluginTask, Integer>(task, job.getInputs().size()));
            for (Pair<String, Long> pair : job.getInputs()) {
                String fileId = pair.first;
                p2p.broadcast(new GetReqMessage(p2p.getPeerNode(), fileId, task.getId()));
            }
        } else {
            task = startTask(task);
        }

        return task;
    }

    private PluginTask startTask(PluginTask task) {
        PluginService service = myInfo.getService(task.getJobInfo().getServiceId());
        if (service == null)
            return null;

        Future<PluginTask> fTask = executorService.submit(new HadoopTask(this, task, service, p2p.getConfig().getServerPath()));
        Pair<PluginTask, Future<PluginTask>> pair = Pair.of(task, fTask);
        executingTasks.put(task.getId(), pair);

        return task;
    }

    private void endTask(String taskId, PluginInfo plugin, FileInfo file) {
        if (taskId.length() <= 0)
            return;

        Pair<PluginTask, Integer> pair = endingTasks.get(taskId);
        if (pair == null)
            return;

        if (plugin.equals(myInfo)) {
            System.out.println("recebi arquivo " + file.getName());
            Future<PluginFile> f = executorService.submit(new HadoopSaveFile(file.getName()));
            pendingSaves.add(f);
        } else {
            p2p.sendFile(plugin.getHost(), file.getName());
        }

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

    private void cancelTask(PeerNode receiver, String taskId) {
        PluginTask task = null;

        if (executingTasks.containsKey(taskId)) {
            Pair<PluginTask, Future<PluginTask>> pair = executingTasks.remove(taskId);
            task = pair.first;
            pair.second.cancel(true);

            try {
                String exec = "hadoop job -kill " + task.getJobInfo().getLocalId();
                Runtime.getRuntime().exec(exec);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (pendingTasks.containsKey(taskId)) {
            task = pendingTasks.remove(taskId).first;
        } else if (endingTasks.containsKey(taskId)) {
            task = endingTasks.remove(taskId).first;
        }

        CancelRespMessage msg = new CancelRespMessage(p2p.getPeerNode(), task);
        p2p.sendMessage(receiver.getHost(), msg);
    }
}
