package br.unb.cic.bionimbus.plugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.toSort.Listeners;
import br.unb.cic.bionimbus.utils.Pair;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;


public abstract class AbstractPlugin implements Plugin, Runnable {

    private String id;
    
    private BioNimbusConfig config;

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
    public AbstractPlugin(final BioNimbusConfig config) throws IOException {
        //id provisório
        this.config = config;
        id = config.getId();
    }

    public Map<String, Pair<String, Integer>> getInputFiles() {
        return inputFiles;
    }

    protected abstract Future<PluginInfo> startGetInfo();

//    protected abstract Future<PluginFile> saveFile(String filename);

//    protected abstract Future<PluginGetFile> getFile(Host origin, PluginFile file, String taskId, String savePath);

    public abstract Future<PluginTask> startTask(PluginTask task,ZooKeeperService zk);

//    private String getId() {
    public String getId() {
        
        return id;
    }
    
    public BioNimbusConfig getConfig() {
        return config;
    }

    private Future<PluginInfo> getFutureInfo() {
        return futureInfo;
    }

    private void setFutureInfo(Future<PluginInfo> futureInfo) {
        this.futureInfo = futureInfo;
    }

    public PluginInfo getMyInfo() {
        return myInfo;
    }
    //private void setMyInfo(PluginInfo info) {
    public void setMyInfo(PluginInfo info) {
        myInfo = info;

    }

//    private String getErrorString() {
//        return errorString;
//    }

    private void setErrorString(String errorString) {
        this.errorString = errorString;
    }
    //não esta setando a p2p é isso mesmo?
//    @Override
//    public void setP2P(P2PService p2p) {
//        // TODO Auto-generated method stub
//
//    }

    @Override
    public void start() {
        
        schedExecutorService.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        schedExecutorService.shutdown();
//        service.remove(this);
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
            
// need to find another host info source --------------------------------------------------------------------------------------------------------------------------------------------
            //newInfo.setHost(service.getPeerNode().getHost());
            setMyInfo(newInfo);
        } catch (Exception e) {
            setErrorString(e.getMessage());
            setMyInfo(null);
        }

        setFutureInfo(null);
    }

    private void checkFinishedTasks() {
//        P2PService p2p = service;
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
                continue;
            }

            executingTasks.remove(task.getId());

            if (task.getJobInfo().getOutputs().size() > 0) {
                int count = 0;
                for (String output : task.getJobInfo().getOutputs()) {
                    File file = new File(config.getServerPath() + "/" + output);
                    FileInfo info = new FileInfo();
                    info.setName(config.getServerPath() + "/" + output);
                    info.setSize(file.length());
                    count++;
                }
                endingTasks.put(task.getId(), new Pair<PluginTask, Integer>(task, count));
            } else {
//                EndMessage endMsg = new EndMessage(p2p.getPeerNode(), task);
//                p2p.broadcast(endMsg);
            }
        }
    }

    private void checkPendingSaves() {
//        P2PService p2p = service;

        for (Future<PluginFile> f : pendingSaves) {

            if (!f.isDone())
                continue;
                
            try {
                PluginFile file = f.get();
                List<String> pluginIds = new ArrayList<String>();
                pluginIds.add(getId());
                file.setPluginId(pluginIds);
                pendingSaves.remove(f);
                pluginFiles.put(file.getId(), file);
            } catch (Exception e) {
                e.printStackTrace();
                //TODO criar mensagem de erro?
            }
        }
    }

    private void checkPendingGets() {
//        P2PService p2p = service;

        for (Future<PluginGetFile> f : pendingGets) {

            if (!f.isDone())
                continue;

            try {
                PluginGetFile get = f.get();
                pendingGets.remove(f);
//                Message msg = new PrepRespMessage(p2p.getPeerNode(), getMyInfo(), get.getPluginFile(), get.getTaskId());
//                p2p.sendMessage(get.getPeer(), msg);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO criar mensagem de erro?
            }
        }
    }

}
