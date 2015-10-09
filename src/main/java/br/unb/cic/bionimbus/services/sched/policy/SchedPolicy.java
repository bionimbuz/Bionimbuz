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
package br.unb.cic.bionimbus.services.sched.policy;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
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
    
    public abstract HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos, CloudMessageService cms);

    public abstract List<PluginTask> relocate(Collection<Pair<JobInfo, PluginTask>> taskPairs);

    public abstract void cancelJobEvent(PluginTask task);

    public abstract void jobDone(PluginTask task);
    
    public abstract String getPolicyName();
}
