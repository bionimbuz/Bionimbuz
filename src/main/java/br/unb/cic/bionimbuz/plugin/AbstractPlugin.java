/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.google.inject.Inject;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.utils.Pair;

public abstract class AbstractPlugin implements Plugin, Runnable {

    private final String id;

    private Future<PluginInfo> futureInfo = null;

    private PluginInfo myInfo = null;

    private int myCount = 0;

    private final ScheduledExecutorService schedExecutorService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("bionimbus-plugin-%d").build());

    private final ConcurrentMap<String, Pair<PluginTask, Future<PluginTask>>> executingTasks = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Pair<PluginTask, Integer>> endingTasks = new ConcurrentHashMap<>();

    private final List<Future<PluginFile>> pendingSaves = new CopyOnWriteArrayList<>();

    private final List<Future<PluginGetFile>> pendingGets = new CopyOnWriteArrayList<>();

    private final ConcurrentMap<String, Pair<String, Integer>> inputFiles = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, PluginFile> pluginFiles = new ConcurrentHashMap<>();

    @Inject
    public AbstractPlugin() throws IOException {
        // id provisório
        this.id = BioNimbusConfig.get().getId();
    }

    public Map<String, Pair<String, Integer>> getInputFiles() {
        return this.inputFiles;
    }

    protected abstract Future<PluginInfo> startGetInfo();

    public abstract Future<PluginTask> startTask(PluginTask task, CloudMessageService cms, Workflow workflow);

    // private String getId() {
    public String getId() {

        return this.id;
    }

    private Future<PluginInfo> getFutureInfo() {
        return this.futureInfo;
    }

    private void setFutureInfo(Future<PluginInfo> futureInfo) {
        this.futureInfo = futureInfo;
    }

    public PluginInfo getMyInfo() {
        return this.myInfo;
    }

    // private void setMyInfo(PluginInfo info) {
    public void setMyInfo(PluginInfo info) {
        this.myInfo = info;

    }

    @Override
    public void start() {

        this.schedExecutorService.scheduleAtFixedRate(this, 0, 3, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
        this.schedExecutorService.shutdown();
        // service.remove(this);
    }

    @Override
    public void run() {
        this.checkGetInfo();
        this.checkFinishedTasks();
        this.checkPendingSaves();
        this.checkPendingGets();
    }

    private void checkGetInfo() {
        this.myCount++;
        if (this.myCount < 10) {
            return;
        }
        this.myCount = 0;
        // Future<PluginInfo> futureinfo = getFutureInfo(); HAVE TO CHECK
        this.futureInfo = this.getFutureInfo();
        if (this.futureInfo == null) {
            this.setFutureInfo(this.startGetInfo());
            return;
        }
        if (!this.futureInfo.isDone()) {
            return;
        }
        try {
            final PluginInfo newInfo = this.futureInfo.get();
            newInfo.setId(this.getId());

            this.setMyInfo(newInfo);
        } catch (InterruptedException | ExecutionException e) {
            this.setMyInfo(null);
        }
        this.setFutureInfo(null);
    }

    private void checkFinishedTasks() {
        Future<PluginTask> futureTask;
        PluginTask task;
        for (final Pair<PluginTask, Future<PluginTask>> pair : this.executingTasks.values()) {
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
            this.executingTasks.remove(task.getId());
            if (task.getJobInfo().getOutputs().size() > 0) {
                int count = 0;
                for (final String output : task.getJobInfo().getOutputs()) {
                    final File file = new File(output);
                    final FileInfo info = new FileInfo();
                    info.setName(output);
                    info.setSize(file.length());
                    count++;
                }
                this.endingTasks.put(task.getId(), new Pair<>(task, count));
            }
        }
    }

    private void checkPendingSaves() {
        for (final Future<PluginFile> f : this.pendingSaves) {
            if (!f.isDone()) {
                continue;
            }
            try {
                final PluginFile file = f.get();
                final List<String> pluginIds = new ArrayList<>();
                pluginIds.add(this.getId());
                file.setPluginId(pluginIds);
                this.pendingSaves.remove(f);
                this.pluginFiles.put(file.getId(), file);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                // TODO criar mensagem de erro?
            }
        }
    }

    private void checkPendingGets() {

        for (final Future<PluginGetFile> f : this.pendingGets) {
            if (f.isDone()) {
                this.pendingGets.remove(f);
            }
        }
    }
}
