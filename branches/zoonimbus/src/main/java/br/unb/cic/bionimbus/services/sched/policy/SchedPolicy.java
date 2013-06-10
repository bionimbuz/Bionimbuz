package br.unb.cic.bionimbus.services.sched.policy;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.sched.policy.impl.AcoSched;
import br.unb.cic.bionimbus.utils.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public abstract class SchedPolicy {
    private ConcurrentHashMap<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    private List<PluginInfo> cloudList = new ArrayList<PluginInfo>();

    public void setCloudMap(ConcurrentHashMap<String, PluginInfo> cloudMap) {
        this.cloudMap = cloudMap;
    }
    
    protected ConcurrentHashMap<String, PluginInfo> getCloudMap() {
        return this.cloudMap;
    }
    
    public void setCloudList(List<PluginInfo> cloudList) {
        this.cloudList = cloudList;
    }

    public List<PluginInfo> getCloudList() {
        return cloudList;
    }

    public static SchedPolicy getInstance() {
        SchedPolicy policy = new AcoSched();
        return policy;
    }

    public static SchedPolicy getInstance(ConcurrentHashMap<String, PluginInfo> cloudMap) {
        SchedPolicy policy = new AcoSched();
        policy.setCloudMap(cloudMap);
        return policy;
    }

    public abstract HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos);
    
    public abstract HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos, ZooKeeperService zk);

    public abstract List<PluginTask> relocate(Collection<Pair<JobInfo, PluginTask>> taskPairs);

    public abstract void cancelJobEvent(PluginTask task);

    public abstract void jobDone(PluginTask task);
}
