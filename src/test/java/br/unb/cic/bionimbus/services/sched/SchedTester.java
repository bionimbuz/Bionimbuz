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
package br.unb.cic.bionimbus.services.sched;

import br.unb.cic.bionimbuz.services.sched.SchedService;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginService;
import br.unb.cic.bionimbuz.services.sched.policy.SchedPolicy;

public class SchedTester {

    private final ConcurrentHashMap<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    private final Map<String, Job> pendingJobs = new ConcurrentHashMap<String, Job>();
    private final Map<String, Job> runningJobs = new ConcurrentHashMap<String, Job>();
    private final Map<PluginInfo, Queue<Job>> queueMap = new ConcurrentHashMap<PluginInfo, Queue<Job>>();
    private SchedPolicy schedPolicy;

    private static final Logger LOG = LoggerFactory.getLogger(SchedService.class);

    private PluginInfo generatePlugin(String id, int numCores, int latency, int numOccupied) {
        PluginInfo p = new PluginInfo();
        p.setId(id);
        p.setLatency((double) latency);
        p.setNumCores(numCores);
        p.setNumOccupied(numOccupied);
        ArrayList<PluginService> services = new ArrayList<PluginService>();
        PluginService ps = new PluginService();
        ps.setId("1");
        services.add(ps);
        p.setServices(services);
        return p;
    }

    private void addPlugin(PluginInfo p) {
        cloudMap.put(p.getId(), p);
    }

    private void cloudMapStart() {
        addPlugin(generatePlugin("p1", 4, 0, 0));
        addPlugin(generatePlugin("p2", 4, 0, 0));
        addPlugin(generatePlugin("p3", 8, 0, 0));
    }

    public SchedPolicy getPolicy() {
        if (schedPolicy == null) {
            schedPolicy = SchedPolicy.getInstance(SchedPolicy.Policy.ACO_SCHED, cloudMap);
        }
        return schedPolicy;
    }

    private Job generateJob(String id) {
//        Job j = new Job(null);
//        j.setId(id);
//        j.setServiceId("1");
//        return j;]
        return null;
    }

    private void addJob(Job j) {
        pendingJobs.put(j.getId(), j);
    }

    private void pendingJobsStart() {
        addJob(generateJob("j1"));
        addJob(generateJob("j2"));
        addJob(generateJob("j3"));
        addJob(generateJob("j4"));
        addJob(generateJob("j5"));
        addJob(generateJob("j6"));
        addJob(generateJob("j7"));
        addJob(generateJob("j8"));
        addJob(generateJob("j9"));
        addJob(generateJob("j10"));
        addJob(generateJob("j11"));
        addJob(generateJob("j12"));
        addJob(generateJob("j13"));
        addJob(generateJob("j14"));
        addJob(generateJob("j15"));
        addJob(generateJob("j16"));
        addJob(generateJob("j17"));
        addJob(generateJob("j18"));
        addJob(generateJob("j19"));
        addJob(generateJob("j20"));
        addJob(generateJob("j21"));
        addJob(generateJob("j22"));
        addJob(generateJob("j23"));
        addJob(generateJob("j25"));
        addJob(generateJob("j26"));
        addJob(generateJob("j27"));
        addJob(generateJob("j28"));
        addJob(generateJob("j29"));
        addJob(generateJob("j30"));
    }

    private void addToQueue(PluginInfo p, Job j) {
        LOG.info(j.getId() + "adicionado a fila de" + p.getId());
        if (!queueMap.containsKey(p)) {
            queueMap.put(p, new LinkedList<Job>());
        }
        queueMap.get(p).add(j);
    }

    private void simulateRun(Job j, PluginInfo p) {
        if (p.getNumOccupied() >= p.getNumCores()) {
            addToQueue(p, j);
        } else {
            p.setNumOccupied(p.getNumOccupied() + 1);
        }

        LOG.info("Job " + j.getId() + " movido para a lista de jobs rodando.");
        pendingJobs.remove(j.getId());
        runningJobs.put(j.getId(), j);
        scheduleJobs();
    }

    private void run() {
        cloudMapStart();
        pendingJobsStart();
        scheduleJobs();
    }

    private void scheduleJobs() {

        if (!pendingJobs.isEmpty()) {
            return;
        }

        LOG.info("--- Inicio de escalonamento ---");
        final Map<Job, PluginInfo> schedMap = null;//getPolicy().schedule(pendingJobs.values());
        for (Map.Entry<Job, PluginInfo> entry : schedMap.entrySet()) {
            Job jobInfo = entry.getKey();
            PluginInfo pluginInfo = entry.getValue();

            if (pluginInfo == null) {
                LOG.info("Serviço não disponível.");
            } else {
                LOG.info(jobInfo.getId() + " scheduled to " + pluginInfo.getId());
                simulateRun(jobInfo, pluginInfo);
            }
        }
        LOG.info("--- Fim de escalonamento ---");

    }

    public static String showDoubleList(List<Double> list) {
        StringBuilder sb = new StringBuilder();
        for (Double d : list) {
            sb.append(d + ",");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SchedTester st = new SchedTester();
        st.run();
    }
}
