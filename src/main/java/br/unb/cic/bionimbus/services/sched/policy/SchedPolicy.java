package br.unb.cic.bionimbus.services.sched.policy;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.sched.policy.impl.AHPPolicy;
import br.unb.cic.bionimbus.services.sched.policy.impl.AcoSched;
import br.unb.cic.bionimbus.services.sched.policy.impl.RRPolicy;
import br.unb.cic.bionimbus.utils.Pair;
import java.util.ArrayList;
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

    /**
     * Retorna uma lista de política de escalonamento disponível.
     * @return lista com as políticas.
     */
    public static List<SchedPolicy> getInstances() {
        List<SchedPolicy> listPolicys= new ArrayList<SchedPolicy>();
        listPolicys.add(0,new AcoSched());
        listPolicys.add(1,new AHPPolicy());
        listPolicys.add(2,new RRPolicy());

        return listPolicys;
        
        
    }

    /**
     * Retorna qual o tipo de escalonador desejado com o mapa das nuvens disponíveis.
     * 0- AcoSched (Padrão)
     * 1- AHPPolicy
     * 2- RRPolicy
     * @param cloudMap
     * @return 
     */
    public static SchedPolicy getInstance(int numPolicy, ConcurrentHashMap<String, PluginInfo> cloudMap) {
        SchedPolicy policy = getInstances().get(numPolicy);
        policy.setCloudMap(cloudMap);
        return policy;
    }

    public abstract HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos);
    
    public abstract HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos, ZooKeeperService zk);

    public abstract List<PluginTask> relocate(Collection<Pair<JobInfo, PluginTask>> taskPairs);

    public abstract void cancelJobEvent(PluginTask task);

    public abstract void jobDone(PluginTask task);
    
    public abstract String getPolicyName();
}
