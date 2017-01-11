/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.sched.model;

import java.util.ArrayList;
import java.util.List;

import br.unb.cic.bionimbuz.services.RepositoryService;

/**
 *
 * @author willian
 */
public class ResourceList {

    public final List<Resource> resources;

    public ResourceList() {
        this.resources = new ArrayList<>();
    }

    // Copy constructor
    public ResourceList(ResourceList resourceList) {

        resources = new ArrayList<>();

        for (Resource resource : resourceList.resources) {
            if (resource != null) {
                resources.add(new Resource(resource));
            }
        }
    }

    public Double getFullCost(RepositoryService rs) {

        Double cost = (double) 0;

        for (Resource resource : resources) {
            cost += resource.getCost(rs);
        }

        return cost;
    }

    public Double getAvgTime(RepositoryService rs) {

        Double time = (double) 0;

        for (Resource resource : resources) {
            time += resource.getExecTime(rs);
        }

        return time / resources.size();
    }

    public Double getMaxTime(RepositoryService rs) {

        Double maxTime = (double) 0;

        for (Resource resource : resources) {
            if (resource.getExecTime(rs) > maxTime) {
                maxTime = resource.getExecTime(rs);
            }
        }

        return maxTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != ResourceList.class) {
            return false;
        }
        return resources.equals(((ResourceList) obj).resources);
    }

    public String result(RepositoryService rs) {
        return getFullCost(rs) + ", " + getAvgTime(rs) + ", " + getMaxTime(rs);
    }
    
    @Override
    public String toString() {
        String ret = "[";
        for (Resource r : resources) {
            ret += r.id + ", " +  r.getAlloc()+ "; ";
        }
        return ret + "]";
    }

}
