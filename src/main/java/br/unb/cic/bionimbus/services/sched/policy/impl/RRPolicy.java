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
import br.unb.cic.bionimbus.services.sched.SchedException;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.utils.Pair;

public class RRPolicy extends SchedPolicy {

    private List<JobInfo> jobs = new ArrayList<JobInfo>();

    private List<PluginInfo> usedResources = new ArrayList<PluginInfo>();

    public void addJob(JobInfo jobInfo) throws SchedException {
        jobs.add(jobInfo);
    }

    public PluginInfo scheduleJob(JobInfo jobInfo) {
        List<PluginInfo> plugins = filterByService(jobInfo.getServiceId(), filterByUsed());
        jobInfo.setTimestamp(System.currentTimeMillis());
        if (plugins.size() == 0) {
            return null;
        }
        return plugins.get(0);
    }

    @Override
    public HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos) {
        HashMap<JobInfo, PluginInfo> schedMap = new HashMap<JobInfo, PluginInfo>();

        for (JobInfo jobInfo : jobInfos) {
            jobInfo.setTimestamp(System.currentTimeMillis());
            PluginInfo resource = this.scheduleJob(jobInfo);
            schedMap.put(jobInfo, resource);
            usedResources.add(resource);
            break;
        }

        return schedMap;
    }

    private List<PluginInfo> filterByService(long serviceId, List<PluginInfo> plgs) {
        ArrayList<PluginInfo> plugins = new ArrayList<PluginInfo>();
        for (PluginInfo pluginInfo : plgs) {
            if (pluginInfo.getService(serviceId) != null)
                plugins.add(pluginInfo);
        }

        return plugins;
    }

    private List<PluginInfo> filterByUsed() {
        ArrayList<PluginInfo> plugins = new ArrayList<PluginInfo>();
        for (PluginInfo pluginInfo : getCloudMap().values()) {
            if (!usedResources.contains(pluginInfo))
                plugins.add(pluginInfo);
        }

        if (plugins.size() == 0) {
            usedResources.clear();
            return new ArrayList<PluginInfo>(getCloudMap().values());
        }

        return plugins;
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
        return schedule(jobInfos);
    }
    
    @Override
    public String getPolicyName() {
        return "Name: "+ RRPolicy.class.getSimpleName()+" - Número: 2";
    }
}
