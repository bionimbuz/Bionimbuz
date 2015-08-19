/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.sched.policy.impl;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.toSort.AllocatedFixedTask;
import br.unb.cic.bionimbus.toSort.Pareto;
import br.unb.cic.bionimbus.toSort.Resource;
import br.unb.cic.bionimbus.toSort.ResourceList;
import br.unb.cic.bionimbus.utils.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author willian
 */
public class Chessmaster extends SchedPolicy {
    
    private int lookahead = 5;
    private float alpha = (float) 0.3;
    
    @Override
    public HashMap<JobInfo, PluginInfo> schedule(Collection<JobInfo> jobInfos) {
        
        ResourceList rl = rs.getCurrentResourceList();
        System.out.println(rl.resources.toString());
//        ResourceList out = minmaxPlayer(rl, new LinkedList<JobInfo>(jobInfos), alpha, lookahead);
//        
//        System.out.println("best max time: " + out.getMaxTime());
//        System.out.println("best avg time: " + out.getAvgTime());
//        System.out.println("best cost: " + out.getFullCost());
//        for (Resource resource : out.resources) {
//            System.out.println("R" + resource.id + " - t: " + resource.getExecTime() + " - c: " + resource.getCost());
//            for (AllocatedFixedTask task : resource.getAllTasks()) {
//                System.out.println("| T" + task.taskRef.getId() + " - c: " + task.cost);
//            }
//            System.out.println("");
//        }
        return null;
    }

    @Override
    public List<PluginTask> relocate(Collection<Pair<JobInfo, PluginTask>> taskPairs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelJobEvent(PluginTask task) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void jobDone(PluginTask task) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPolicyName() {
        return "Name: " + Chessmaster.class.getSimpleName();
    }
    
    private ResourceList minmaxPlayer(ResourceList resourceList, Queue<JobInfo> taskList, float alpha, int depth) {
        
        if (!taskList.isEmpty()) {
            printTab(depth);
            System.out.println("[" + depth + "] Player - job: " + taskList.peek().getId());
        }
        
        // create a local best to minimise with maximum wastage
        ResourceList best = new ResourceList();
//        Resource r = new Resource(0, Float.MAX_VALUE);
//        r.allocateTask(new AllocatedTask((float) 1, null));
//        best.resources.add(r);
        
        List<ResourceList> paretoOptResults = new ArrayList();
        
        // create a max cost point (i.e. (0, inf))
        ResourceList maxCost = new ResourceList();
//        Resource r1 = new Resource(0, (long) 1, Float.MAX_VALUE);
//        r1.allocateTask(new AllocatedFixedTask((long)1, null));
//        maxCost.resources.add(r1);
//        paretoOptResults.add(maxCost);
//        
//        // create a max time point (i.e. (inf, 0))
//        ResourceList maxTime = new ResourceList();
//        Resource r2 = new Resource(0, (long) 1, (float) 0);
//        r2.allocateTask(new AllocatedFixedTask(Long.MAX_VALUE, null));
//        maxTime.resources.add(r2);
//        paretoOptResults.add(maxTime);
        
//        TODO: f1 = 1 and not 0. need to fix
//        Float f1 = maxCost.getMaxTime();
//        Float f2 = maxCost.getFullCost();
        
        // if we can still go deeper and there is a task to schedule
        if (depth != 0 && !taskList.isEmpty()) {
            for (Resource resource : resourceList.resources) {
                // create resourceList and taskList copies
                Queue<JobInfo> taskListCopy = new LinkedList(taskList);
                System.out.println("size: " + taskListCopy.size());
                ResourceList resourceListCopy = new ResourceList(resourceList);
                
                // run recursive call with current resource
                ResourceList result = minmaxNature(resource, resourceListCopy, taskListCopy, alpha, depth-1);
                
                // minimise player's play with a pareto optimal and an alpha selector
                // attempt to add new result to pareto curve
                paretoOptResults.add(result);
                List<ResourceList> newParetoOptResults = Pareto.getParetoCurve(paretoOptResults);
                
//                printTab(depth);
//                System.out.println("    cost=" + result.getFullCost() + ", avgTime=" + result.getAvgTime() + ", maxTime=" + result.getMaxTime());
                
                // if added, attempt to update best result
                if (newParetoOptResults != null) {
                    // update pareto curve
                    paretoOptResults = newParetoOptResults;
                    
                    // get pareto optimal for alpha
                    ResourceList newBest = Pareto.getParetoOptimal(paretoOptResults, alpha);
                    if (newBest != null)
                        best = newBest;
                }
            }
            return best;
        }
        // if the maximum depth was reached or all tasks were scheduled
        else {
            return resourceList;
        }
    }
    
    private ResourceList minmaxNature(Resource resource, ResourceList resourceList, Queue<JobInfo> taskList, float alpha, int depth) {
        printTab(depth);
        System.out.println("[" + depth + "] Nature - resource: " + resource.id);
        JobInfo job;
        
        // create current best with min cost
        ResourceList best = new ResourceList();
//        best.resources.add(new Resource(0, (long)1, Float.MIN_VALUE));
        
        // if we can still go deeper
        System.out.println("size2: " + taskList.size());
        job = taskList.poll();
        System.out.println("job: " + job.toString());
        if (depth != 0) {
            // for each aproximation of a task's cost
            for (Long cost : job.getHistory(rs)) {
                // attempt to use the new resource to allocate the new task
                
                // create resourceList and taskList copies
                Queue<JobInfo> taskListCopy = new LinkedList(taskList);
                ResourceList resourceListCopy = new ResourceList(resourceList);
                
                // Allocate the newTask to the new resource
                AllocatedFixedTask newTask = new AllocatedFixedTask(cost, job);
                getResource(resource, resourceListCopy.resources).allocateTask(newTask);
                
                // recursive call
                ResourceList result = minmaxPlayer(resourceListCopy, taskListCopy, alpha, depth-1);
                
                printTab(depth);
                System.out.println("    cost=" + result.getFullCost() + ", avgTime=" + result.getAvgTime() + ", maxTime=" + result.getMaxTime());
                // maxmise nature's play
                if (result.getFullCost() > best.getFullCost())
                    best = result;
            }
            return best;
        } else {
            // if the max depth was reached
            return resourceList;
        }
    }
    
    private static Resource getResource(Resource r, List<Resource> rs) {
        return rs.get(rs.indexOf(r));
    }
    
    private static void printTab(int n) {
        for (int i = 0; i < 10-n; i++) {
                System.out.print("   ");
        }
        
    }
    
}
