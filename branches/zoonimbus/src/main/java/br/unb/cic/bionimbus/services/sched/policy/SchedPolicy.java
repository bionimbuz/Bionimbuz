package br.unb.cic.bionimbus.services.sched.policy;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.sched.policy.impl.AHPPolicy;
import br.unb.cic.bionimbus.services.sched.policy.impl.AcoSched;
import br.unb.cic.bionimbus.utils.Pair;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public abstract class SchedPolicy {
    private ConcurrentHashMap<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();

    public void setCloudMap(ConcurrentHashMap<String, PluginInfo> cloudMap) {
        this.cloudMap = cloudMap;
    }
    
    protected ConcurrentHashMap<String, PluginInfo> getCloudMap() {
        return this.cloudMap;
    }

    public static SchedPolicy getInstance() {
//        SchedPolicy policy = new AcoSched();
        SchedPolicy policy = new AHPPolicy();

        return policy;
        
        
    }

    public static SchedPolicy getInstance(ConcurrentHashMap<String, PluginInfo> cloudMap, ZooKeeperService zk) {
//        SchedPolicy policy = new AcoSched();
        SchedPolicy policy = new AHPPolicy();
        policy.setCloudMap(cloudMap);
        return policy;
    }

    public abstract HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos);
    
    public abstract HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos, ZooKeeperService zk);

    public abstract List<PluginTask> relocate(Collection<Pair<JobInfo, PluginTask>> taskPairs);

    public abstract void cancelJobEvent(PluginTask task);

    public abstract void jobDone(PluginTask task);
}
