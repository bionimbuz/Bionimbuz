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

import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.utils.Pair;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public abstract class AbstractPlugin implements Plugin, Runnable {

    private String id;

    private BioNimbusConfig config;

    private Future<PluginInfo> futureInfo = null;

    private PluginInfo myInfo = null;

    private String errorString = "Plugin is loading...";

    private int myCount = 0;

    private final ScheduledExecutorService schedExecutorService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("bionimbus-plugin-%d").build());

    private final ConcurrentMap<String, Pair<PluginTask, Integer>> pendingTasks = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Pair<PluginTask, Future<PluginTask>>> executingTasks = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Pair<PluginTask, Integer>> endingTasks = new ConcurrentHashMap<>();

    private final List<Future<PluginFile>> pendingSaves = new CopyOnWriteArrayList<>();

    private final List<Future<PluginGetFile>> pendingGets = new CopyOnWriteArrayList<>();

    private final ConcurrentMap<String, Pair<String, Integer>> inputFiles = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, PluginFile> pluginFiles = new ConcurrentHashMap<>();

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

    public abstract Future<PluginTask> startTask(PluginTask task, CloudMessageService cms, Workflow workflow);

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

    private void setErrorString(String errorString) {
        this.errorString = errorString;
    }

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
        if (myCount < 10) {
            return;
        }
        myCount = 0;
        // Future<PluginInfo> futureinfo = getFutureInfo(); HAVE TO CHECK
        futureInfo = getFutureInfo();
        if (futureInfo == null) {
            setFutureInfo(startGetInfo());
            return;
        }
        if (!futureInfo.isDone()) {
            return;
        }
        try {
            PluginInfo newInfo = futureInfo.get();
            newInfo.setId(getId());

            setMyInfo(newInfo);
        } catch (InterruptedException | ExecutionException e) {
            setErrorString(e.getMessage());
            setMyInfo(null);
        }
        setFutureInfo(null);
    }

    private void checkFinishedTasks() {
        Future<PluginTask> futureTask;
        PluginTask task;
        for (Pair<PluginTask, Future<PluginTask>> pair : executingTasks.values()) {
            futureTask = pair.second;
            if (!futureTask.isDone()) {
                continue;
            }
            try {
                task = futureTask.get();
            } catch (InterruptedException | ExecutionException e) {
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
                endingTasks.put(task.getId(), new Pair<>(task, count));
            }
        }
    }

    private void checkPendingSaves() {
        for (Future<PluginFile> f : pendingSaves) {
            if (!f.isDone()) {
                continue;
            }
            try {
                PluginFile file = f.get();
                List<String> pluginIds = new ArrayList<String>();
                pluginIds.add(getId());
                file.setPluginId(pluginIds);
                pendingSaves.remove(f);
                pluginFiles.put(file.getId(), file);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                //TODO criar mensagem de erro?
            }
        }
    }

    private void checkPendingGets() {

        for (Future<PluginGetFile> f : pendingGets) {
            if (!f.isDone()) {
                continue;
            }
            try {
                PluginGetFile get = f.get();
                pendingGets.remove(f);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                // TODO criar mensagem de erro?
            }
        }
    }
}
