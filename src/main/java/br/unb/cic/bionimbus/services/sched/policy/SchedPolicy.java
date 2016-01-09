package br.unb.cic.bionimbus.services.sched.policy;

import br.unb.cic.bionimbus.model.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.sched.policy.impl.AHPPolicy;
import br.unb.cic.bionimbus.services.sched.policy.impl.AcoSched;
import br.unb.cic.bionimbus.services.sched.policy.impl.RRPolicy;
import br.unb.cic.bionimbus.services.sched.policy.impl.C99Supercolider;
import br.unb.cic.bionimbus.services.RepositoryService;
import br.unb.cic.bionimbus.utils.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public abstract class SchedPolicy {
    
    protected CloudMessageService cms;
    protected RepositoryService rs;
    
    public enum Policy {
        ACO_SCHED,
        AHP,
        RR,
        C99SUPERCOLIDER
    }
    
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
        listPolicys.add(Policy.ACO_SCHED.ordinal(),new AcoSched());
        listPolicys.add(Policy.AHP.ordinal(),new AHPPolicy());
        listPolicys.add(Policy.RR.ordinal(),new RRPolicy());
        listPolicys.add(Policy.C99SUPERCOLIDER.ordinal(),new C99Supercolider());

        return listPolicys;
    }

    /**
     * Retorna qual o tipo de escalonador desejado com o mapa das nuvens disponíveis.
     * AcoSched (Padrão)
     * AHPPolicy
     * RRPolicy
     * 
     * @param policy 
     * @param cloudMap
     * @return 
     */
    public static SchedPolicy getInstance(Policy policy, ConcurrentHashMap<String, PluginInfo> cloudMap) {
        SchedPolicy policyInst = getInstances().get(policy.ordinal());
        policyInst.setCloudMap(cloudMap);
        return policyInst;
    }
    
    public void setCms(CloudMessageService cms) {
        this.cms = cms;
    }
    
    public void setRs(RepositoryService rs) {
        this.rs = rs;
    }
    
    public abstract HashMap<JobInfo, PluginInfo> schedule(List<JobInfo> jobs);
    
    public abstract List<PluginTask> relocate(Collection<Pair<JobInfo, PluginTask>> taskPairs);

    public abstract void cancelJobEvent(PluginTask task);

    public abstract void jobDone(PluginTask task);
    
    public abstract String getPolicyName();
}
