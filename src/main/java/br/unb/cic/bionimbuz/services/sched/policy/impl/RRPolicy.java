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
package br.unb.cic.bionimbuz.services.sched.policy.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.services.sched.SchedException;
import br.unb.cic.bionimbuz.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbuz.utils.Pair;

public class RRPolicy extends SchedPolicy {

    private List<Job> jobs = new ArrayList<Job>();

    private List<PluginInfo> usedResources = new ArrayList<PluginInfo>();

    public void addJob(Job jobInfo) throws SchedException {
        jobs.add(jobInfo);
    }

    public PluginInfo scheduleJob(Job jobInfo) {
        List<PluginInfo> plugins = filterByService(jobInfo.getServiceId(), filterByUsed());
        jobInfo.setTimestamp(System.currentTimeMillis());
        if (plugins.isEmpty()) {
            return null;
        }
        return plugins.get(0);
    }

    @Override
    public HashMap<Job, PluginInfo> schedule(List<Job> jobs) {
        HashMap<Job, PluginInfo> schedMap = new HashMap<Job, PluginInfo>();

        for (Job jobInfo : jobs) {
            jobInfo.setTimestamp(System.currentTimeMillis());
            PluginInfo resource = this.scheduleJob(jobInfo);
            schedMap.put(jobInfo, resource);
            usedResources.add(resource);
            break;
        }

        return schedMap;
    }

    private List<PluginInfo> filterByService(String serviceId, List<PluginInfo> plgs) {
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

        if (plugins.isEmpty()) {
            usedResources.clear();
            return new ArrayList<PluginInfo>(getCloudMap().values());
        }

        return plugins;
    }

    @Override
    public List<PluginTask> relocate(
            Collection<Pair<Job, PluginTask>> taskPairs) {
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
    public String getPolicyName() {
        return "Name: "+ RRPolicy.class.getSimpleName()+" - Número: 2";
    }
}
