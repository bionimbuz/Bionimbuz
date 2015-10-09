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
package br.unb.cic.bionimbus.services.sched.policy.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.utils.Pair;

public class BasicSchedPolicy extends SchedPolicy {

    private final int CORES_WEIGHT = 3;
    private final int NODES_WEIGHT = 2;

    private List<PluginInfo> filterByService(long serviceId) {
        ArrayList<PluginInfo> plugins = new ArrayList<PluginInfo>();
        for (PluginInfo pluginInfo : getCloudMap().values()) {
            if (pluginInfo.getService(serviceId) != null)
                plugins.add(pluginInfo);
        }

        return plugins;
    }

    private PluginInfo getBestPluginForJob(List<PluginInfo> plugins, JobInfo job) {
        PluginInfo best = plugins.get(0);
        for (PluginInfo plugin : plugins) {
            if (calculateWeightSum(plugin) > calculateWeightSum(best)) best = plugin;
        }

        return best;
    }

    private int calculateWeightSum(PluginInfo plugin) {
        return (plugin.getNumCores() * CORES_WEIGHT) + (plugin.getNumNodes() * NODES_WEIGHT);
    }

    @Override
    public HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos) {
        HashMap<JobInfo, PluginInfo> schedMap = new HashMap<JobInfo, PluginInfo>();

        for (JobInfo jobInfo : jobInfos) {
            PluginInfo resource = this.scheduleJob(jobInfo);
            schedMap.put(jobInfo, resource);
        }

        return schedMap;
    }

    public PluginInfo scheduleJob(JobInfo jobInfo) {
        List<PluginInfo> availablePlugins = filterByService(jobInfo.getServiceId());
        if (availablePlugins.size() == 0) {
            return null;
        }
        return getBestPluginForJob(availablePlugins, jobInfo);
    }

    @Override
    public List<PluginTask> relocate(
            Collection<Pair<JobInfo, PluginTask>> taskPairs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelJobEvent(PluginTask task) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobDone(PluginTask task) {
        // TODO Auto-generated method stub

    }

    @Override
    public HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos, CloudMessageService cms) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public String getPolicyName() {
        return "Name: Política de escalonamento Básica  -  "+ BasicSchedPolicy.class.getSimpleName();
    }
}
