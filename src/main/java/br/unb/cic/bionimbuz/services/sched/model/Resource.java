/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.sched.model;

import java.util.ArrayList;
import java.util.List;

import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.services.RepositoryService;

/**
 *
 * @author willian
 */
public class Resource {
    
    public final String id;
    public final Double clock;
    public final Double cost;
    private final List<Job> allocatedTasks;
    private final List<Job> preAllocatedTasks;

    public Resource(String id, Double clock, Double cost) {
        this.id = id;
        this.clock = clock;
        this.cost = cost;
        allocatedTasks = new ArrayList<>();
        preAllocatedTasks = new ArrayList<>();
    }
    
    // Copy constructor
    public Resource(Resource resource) {
        this.id = resource.id;
        this.clock = resource.clock;
        this.cost = resource.cost;
        allocatedTasks = new ArrayList<>(resource.getAllocatedTasks());
        preAllocatedTasks = new ArrayList<>(resource.getPreAllocatedTasks());
    }
    
    
    public void allocateTask(Job task) {
        allocatedTasks.add(task);
    }
    
    public void addTask(Job task) {
        preAllocatedTasks.add(task);
    }
    
    public List<Job> getAllocatedTasks() {
        return allocatedTasks;
    }
    
    public List<Job> getPreAllocatedTasks() {
        return preAllocatedTasks;
    }
    
    /**
     * Get execution time for all allocated and pre allocated tasks
     * @param rs RepositoryService
     * @return execTime in seconds
     */
    public Double getExecTime(RepositoryService rs) {
        double cycles = 0;
        
        ArrayList<Job> tasks = new ArrayList<>(allocatedTasks);
        tasks.addAll(preAllocatedTasks);
        for (Job task : tasks)
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
        for (Job t : allocatedTasks) {
            s += t.getWorstExecution() + ", ";
        }
        return s + "]";
    }
    
}
