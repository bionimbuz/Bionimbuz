/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.sched.model;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.services.RepositoryService;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author willian
 */
public class Resource {
    
    public final String id;
    public final Double clock;
    public final Double cost;
    private final List<JobInfo> allocatedTasks;
    private final List<JobInfo> preAllocatedTasks;

    public Resource(String id, Double clock, Double cost) {
        this.id = id;
        this.clock = clock;
        this.cost = cost;
        allocatedTasks = new ArrayList();
        preAllocatedTasks = new ArrayList();
    }
    
    // Copy constructor
    public Resource(Resource resource) {
        this.id = resource.id;
        this.clock = resource.clock;
        this.cost = resource.cost;
        allocatedTasks = new ArrayList(resource.getAllocatedTasks());
        preAllocatedTasks = new ArrayList(resource.getPreAllocatedTasks());
    }
    
    
    public void allocateTask(JobInfo task) {
        allocatedTasks.add(task);
    }
    
    public void addTask(JobInfo task) {
        preAllocatedTasks.add(task);
    }
    
    public List<JobInfo> getAllocatedTasks() {
        return allocatedTasks;
    }
    
    public List<JobInfo> getPreAllocatedTasks() {
        return preAllocatedTasks;
    }
    
    /**
     * Get execution time for all allocated and pre allocated tasks
     * @param rs RepositoryService
     * @return execTime in seconds
     */
    public Double getExecTime(RepositoryService rs) {
        double cycles = 0;
        
        ArrayList<JobInfo> tasks = new ArrayList<>(allocatedTasks);
        tasks.addAll(preAllocatedTasks);
        for (JobInfo task : tasks)
            if (rs != null)
                cycles += rs.getWorstExecution(task.getServiceId());
            else
                cycles += task.getWorstExecution();
        
        return cycles/clock;
    }
    
    public Double getCost(RepositoryService rs) {
        return cost*getExecTime(rs);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Resource) {
            Resource r = (Resource) obj;
            return id.equals(r.id) && r.allocatedTasks.equals(allocatedTasks);
        } else
            return false;
        
    }

    @Override
    public String toString() {
        return "Id: " + id + ", Time: " + getExecTime(null) + ", Cost: " + getCost(null) + ", " + allocatedTasks.size() + "Tasks: " + allocatedTasks.toString();
    }
    
    public String getAlloc() {
        String s = "[";
        for (JobInfo t : allocatedTasks) {
            s += t.getWorstExecution() + ", ";
        }
        return s + "]";
    }
    
}
