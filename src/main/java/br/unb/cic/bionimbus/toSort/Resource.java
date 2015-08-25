/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

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
    private final List<AllocatedFixedTask> allocatedTasks;
    private final List<AllocatedFixedTask> newAllocatedTasks;

    public Resource(String id, Double clock, Double cost) {
        this.id = id;
        this.clock = clock;
        this.cost = cost;
        allocatedTasks = new ArrayList();
        newAllocatedTasks = new ArrayList();
    }
    
    // Copy constructor
    public Resource(Resource resource) {
        this.id = resource.id;
        this.clock = resource.clock;
        this.cost = resource.cost;
        allocatedTasks = new ArrayList(resource.getAllTasks());
        newAllocatedTasks = new ArrayList(resource.getNewTasks());
    }
    
    public void allocateTask(AllocatedFixedTask task) {
        allocatedTasks.add(task);
        newAllocatedTasks.add(task);
    }
    
    public void addAllocatedTask(AllocatedFixedTask task) {
        allocatedTasks.add(task);
    }
    
    public List<AllocatedFixedTask> getAllTasks() {
        return allocatedTasks;
    }
    
    public List<AllocatedFixedTask> getNewTasks() {
        return newAllocatedTasks;
    }
    
    public Double getExecTime() {
        double cycles = 0;
        
        for (AllocatedFixedTask task : allocatedTasks)
            cycles += task.cost;
        
        return cycles/clock;
    }
    
    public Double getCost() {
        float cycles = 0;
        
        for (AllocatedFixedTask task : allocatedTasks)
            cycles += task.cost;
        
        return cost*cycles/clock;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Resource) {
            Resource r = (Resource) obj;
            return id.equals(r.id);
        } else
            return false;
        
    }

    @Override
    public String toString() {
        return "Id: " + id + ", Clock: " + clock + ", Cost: " + cost;
    }
    
}
