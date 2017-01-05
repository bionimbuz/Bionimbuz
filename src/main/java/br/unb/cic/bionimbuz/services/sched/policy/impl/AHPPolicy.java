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
import java.util.Map;

import Jama.Matrix;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.plugin.PluginTaskState;
import br.unb.cic.bionimbuz.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbuz.utils.Pair;

public class AHPPolicy extends SchedPolicy {

    private static final int BLACKLIST_LIMIT = 12;
    private final List<PluginInfo> usedResources = new ArrayList<PluginInfo>();
    private final Map<PluginTask, Integer> blackList = new HashMap<PluginTask, Integer>();

    @Override
    public void cancelJobEvent(PluginTask task) {
        blackList.remove(task);
    }

    @Override
    public HashMap<Job, PluginInfo> schedule(List<Job> jobs) {
        List<Job> jobInfos = jobs;
        if (jobInfos==null || jobInfos.isEmpty()) return null;

        HashMap<Job, PluginInfo> jobMap = new HashMap<Job, PluginInfo>();
        Job biggerJob = getBiggerJob(new ArrayList<Job>(jobInfos));
        biggerJob.setTimestamp(System.currentTimeMillis());
        jobMap.put(biggerJob, this.scheduleJob(biggerJob));
        return jobMap;
    }

    @Override
    public synchronized List<PluginTask> relocate(Collection<Pair<Job, PluginTask>> taskPairs) {
        List<PluginTask> tasksToCancel = new ArrayList<PluginTask>();
        for (Pair<Job, PluginTask> taskPair : taskPairs) {
            PluginTask task = taskPair.getSecond();
            Job job = taskPair.getFirst();

            if (PluginTaskState.RUNNING.equals(task.getState())) {
                if (blackList.containsKey(task)) {
                    blackList.remove(task);
                }
            }

            if (!PluginTaskState.WAITING.equals(task.getState())) continue;

            int count = 0;
            if (blackList.containsKey(task)) {
                count = blackList.get(task);
            }

            blackList.put(task, count + 1);

            if (blackList.get(task) >= BLACKLIST_LIMIT) {
                if (job != null) {
                    tasksToCancel.add(task);
                }
            }
        }

        for (PluginTask task : tasksToCancel) {
            blackList.remove(task);
        }

        return tasksToCancel;
    }

    @Override
    public void jobDone(PluginTask task) {
        //DEBUG
        if (blackList.containsKey(task)) {
            blackList.remove(task);
        }
    }

    private PluginInfo scheduleJob(Job jobInfo) {
        List<PluginInfo> plugins = filterByService(jobInfo.getServiceId(),
                getCloudMap().values());
        if (plugins.isEmpty()) {
            return null;
        }
        return getBestService(plugins);
    }

    public static Job getBiggerJob(List<Job> jobInfos) {
        if (jobInfos.isEmpty())
            return null;
        Job bigger = null;
        long biggerTotal = 0L;
        for (Job jobInfo : jobInfos) {
            long total = getTotalSizeOfJobsFiles(jobInfo);
            if (bigger == null) {
                bigger = jobInfo;
                biggerTotal = total;
            } else {
                if (getTotalSizeOfJobsFiles(jobInfo) > biggerTotal) {
                    bigger = jobInfo;
                    biggerTotal = total;
                }
            }
        }
        return bigger;
    }

    public static long getTotalSizeOfJobsFiles(Job jobInfo) {
        long sum = 0;
        for (FileInfo info : jobInfo.getInputFiles()) {
            sum += info.getSize();
        }
        return sum;
    }

    public static double comparePluginInfo(PluginInfo a, PluginInfo b,
                                          String attribute) {
        double valueA = 0.0f;
        double valueB = 0.0f;

        if (attribute.equals("latency")) {
            // Tem que ser inverso ja que no caso de latency quanto menor
            // melhor!
            valueA = b.getLatency();
            valueB = a.getLatency();
        } else if (attribute.equals("uptime")) {
            valueA = a.getUptime();
            valueB = b.getUptime();
        } else if (attribute.equals("processor")) {
            if (b.getNumOccupied() == 0 && a.getNumOccupied() == 0) {
                valueA = (float) 1.0 / b.getNumCores();
                valueB = (float) 1.0 / a.getNumCores();
            } else {
                valueA = (float) b.getNumOccupied() / b.getNumCores();
                valueB = (float) a.getNumOccupied() / a.getNumCores();
            }
        } else {
            throw new RuntimeException("Atributo " + attribute
                    + " não encontrado para escalonamento.");
        }

        if (valueA == 0.0 && valueB == 0.0) {
            return 1.0f;
        }

        if (valueB == 0.0) {
            return Float.MAX_VALUE;
        }

        return valueA / valueB;
    }

    public static Matrix generateComparisonMatrix(List<PluginInfo> pluginInfos,
                                                  String attribute) {
        Matrix m = new Matrix(pluginInfos.size(), pluginInfos.size());

        for (int i = 0; i < pluginInfos.size(); ++i) {
            for (int j = 0; j < pluginInfos.size(); ++j) {
                m.set(i,
                        j,
                        comparePluginInfo(pluginInfos.get(i),
                                pluginInfos.get(j), attribute));
            }
        }

        return m;
    }

    public static List<Double> getPrioritiesOnMatrix(Matrix m) {
        List<Double> priorities = new ArrayList<Double>();
        double sum = 0.0;

        for (int i = 0; i < m.getColumnDimension(); ++i) {
            for (int j = 0; j < m.getRowDimension(); ++j) {
                sum += m.get(i, j);
            }
        }

        for (int i = 0; i < m.getColumnDimension(); ++i) {
            double pSum = 0.0;
            for (int j = 0; j < m.getRowDimension(); ++j) {
                pSum += m.get(i, j);
            }
            priorities.add(pSum / sum);
        }

        return priorities;
    }

    private static int getMaxNumberIndex(List<Double> numbers) {
        int maxIndex = 0;

        for (int i = 0; i < numbers.size(); ++i) {
            if (numbers.get(i) > numbers.get(maxIndex)) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static List<Double> multiplyVectors(List<Double> a, List<Double> b) {
        List<Double> result = new ArrayList<Double>();

        if (a.size() != b.size())
            throw new RuntimeException(
                    "Vetores sendo multiplicados possuem tamanhos diferentes no escalonamento");
        for (int i = 0; i < a.size(); ++i) {
            result.add(a.get(i) * b.get(i));
        }
        return result;
    }

    private static boolean allCoresOccupied(List<PluginInfo> pluginInfos) {
        for (PluginInfo pluginInfo : pluginInfos) {
            if (pluginInfo.getNumCores() > pluginInfo.getNumOccupied()) {
                return false;
            }
        }
        return true;
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

    public List<PluginInfo> getServiceOrderedByPriority(
            List<PluginInfo> pluginInfos) {

        List<PluginInfo> plugins = new ArrayList<PluginInfo>();
        Matrix mLatency = generateComparisonMatrix(pluginInfos, "latency");
        Matrix mProcessor = generateComparisonMatrix(pluginInfos, "processor");
        List<Double> prioritiesLatency = getPrioritiesOnMatrix(mLatency);
        List<Double> prioritiesProcessor = getPrioritiesOnMatrix(mProcessor);
        List<Double> priorities = multiplyVectors(prioritiesLatency, prioritiesProcessor);

        System.out.println("Latency: " + prioritiesLatency);
        System.out.println("Process: " + prioritiesProcessor);
        System.out.println("Global : " + priorities);

        while (!priorities.isEmpty()) {
            int index = getMaxNumberIndex(priorities);
            plugins.add(pluginInfos.get(index));
            pluginInfos.remove(index);
            priorities.remove(index);
        }
        return plugins;
    }

    public PluginInfo getBestService(List<PluginInfo> pluginInfos) {
        if (pluginInfos.isEmpty())
            return null;

        PluginInfo resource;
        if (allCoresOccupied(pluginInfos)) {
            resource = filterByUsed().get(0);
        } else {
            resource = getServiceOrderedByPriority(pluginInfos).get(pluginInfos.size());
        }
        usedResources.add(resource);
        return resource;
    }

    private List<PluginInfo> filterByService(String serviceId,
                                             Collection<PluginInfo> plgs) {
        ArrayList<PluginInfo> plugins = new ArrayList<PluginInfo>();
        for (PluginInfo pluginInfo : plgs) {
            if (pluginInfo.getService(serviceId) != null)
                plugins.add(pluginInfo);
        }

        return plugins;
    }

    public Matrix inducedMatrix(Matrix matrix, double n) {
        return matrix.arrayTimes(matrix).minus(matrix.times(n));
    }

    @Override
    public String getPolicyName() {
        return "Name: "+ AHPPolicy.class.getSimpleName()+" - Número: 1";
    }
}
