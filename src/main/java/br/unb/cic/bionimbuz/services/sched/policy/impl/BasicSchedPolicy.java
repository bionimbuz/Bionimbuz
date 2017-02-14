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
import br.unb.cic.bionimbuz.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbuz.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicSchedPolicy extends SchedPolicy {

    private final int CORES_WEIGHT = 3;
    private final int NODES_WEIGHT = 2;
    protected static final Logger LOGGER = LoggerFactory.getLogger(BasicSchedPolicy.class);
    private List<PluginInfo> filterByService(String serviceId) {
        ArrayList<PluginInfo> plugins = new ArrayList<>();
        getCloudMap().values().stream().filter((pluginInfo) -> (pluginInfo.getService(serviceId) != null)).forEach((pluginInfo) -> {
            plugins.add(pluginInfo);
        });

        return plugins;
    }

    private PluginInfo getBestPluginForJob(List<PluginInfo> plugins, Job job) {
        PluginInfo best = plugins.get(0);
        for (PluginInfo plugin : plugins) {
            for (String ip : job.getIpjob())
                if(plugin.getHost().getAddress().equalsIgnoreCase(ip)){
//                 LOGGER.info("ESTA PORRA AQUI");
                    return plugin;
                }
        }
        for (PluginInfo plugin : plugins) {
            if (calculateWeightSum(plugin) > calculateWeightSum(best)) best = plugin;
        }
        return best;
    }

    private int calculateWeightSum(PluginInfo plugin) {
        return (plugin.getNumCores() * CORES_WEIGHT) + (plugin.getNumNodes() * NODES_WEIGHT);
    }
    /**
     * Atribui os valores 
     * @param jobs
     * @return 
     */
    @Override
    public HashMap<Job, PluginInfo> schedule(List<Job> jobs) {
        HashMap<Job, PluginInfo> schedMap = new HashMap<>();
        for(PluginInfo plugin: getCloudMap().values()){
            for(Job job : jobs){
                if(job.getIpjob().contains(plugin.getHost().getAddress())){
                    job.getIpjob().indexOf(plugin.getHost().getAddress());
                    schedMap.put(job,plugin);
                }
            }
        }
//        jobs.stream().forEach((jobInfo) -> {
//            PluginInfo resource = this.scheduleJob(jobInfo);
//            schedMap.put(jobInfo, resource);
//        });
        return schedMap;
    }

    public PluginInfo scheduleJob(Job jobInfo) {
        List<PluginInfo> availablePlugins = filterByService(jobInfo.getServiceId());
        if (availablePlugins.isEmpty()) {
            return null;
        }
        return getBestPluginForJob(availablePlugins, jobInfo);
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
        return "Name: Política de escalonamento Básica  -  "+ BasicSchedPolicy.class.getSimpleName();
    }
}
