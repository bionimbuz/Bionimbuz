package br.unb.cic.bionimbus.services.sched.policy.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.sched.SchedException;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.utils.Pair;
import br.unb.cic.bionimbus.services.ZooKeeperService;

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
    public HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos, ZooKeeperService zk) {
        return schedule(jobInfos);
    }
    
    @Override
    public String getPolicyName() {
        return "Name: Round Robin Policy  -  "+ RRPolicy.class.getSimpleName()+" - Número: 2";
    }
}
