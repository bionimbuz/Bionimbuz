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
package br.unb.cic.bionimbuz.services.sched.policy;

import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.sched.policy.impl.AHPPolicy;
import br.unb.cic.bionimbuz.services.sched.policy.impl.AcoSched;
import br.unb.cic.bionimbuz.services.sched.policy.impl.RRPolicy;
import br.unb.cic.bionimbuz.services.sched.policy.impl.C99Supercolider;
import br.unb.cic.bionimbuz.services.sched.policy.impl.BasicSchedPolicy;
import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.utils.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public abstract class SchedPolicy {
    
    protected CloudMessageService cms;
    protected static RepositoryService rs;
    
    public enum Policy {
        ACO_SCHED,
        AHP,
        RR,
        C99SUPERCOLIDER,
        BasicSchedPolicy
    }
    
    private ConcurrentHashMap<String, PluginInfo> cloudMap = new ConcurrentHashMap<>();

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
        List<SchedPolicy> listPolicys= new ArrayList<>();
        listPolicys.add(Policy.ACO_SCHED.ordinal(),new AcoSched());
        listPolicys.add(Policy.AHP.ordinal(),new AHPPolicy());
        listPolicys.add(Policy.RR.ordinal(),new RRPolicy());
        listPolicys.add(Policy.C99SUPERCOLIDER.ordinal(),new C99Supercolider());
        listPolicys.add(Policy.BasicSchedPolicy.ordinal(),new BasicSchedPolicy());

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
    
    public abstract HashMap<Job, PluginInfo> schedule(List<Job> jobs);
    
    public abstract List<PluginTask> relocate(Collection<Pair<Job, PluginTask>> taskPairs);

    public abstract void cancelJobEvent(PluginTask task);

    public abstract void jobDone(PluginTask task);
    
    public abstract String getPolicyName();
}
