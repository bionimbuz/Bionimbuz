/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.client;

import br.unb.cic.bionimbus.toSort.RepositoryService;
import br.unb.cic.bionimbus.utils.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author willian
 */
public class PipelineInfo {
    private String id = UUID.randomUUID().toString();

    final private List<JobInfo> jobs;
    
    public PipelineInfo() {
        jobs = new ArrayList<JobInfo>();
    }
    
    public PipelineInfo(List<JobInfo> jobs) {
        this.jobs = new ArrayList<JobInfo>();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<JobInfo> getJobs() {
        return jobs;
    }
    
    public void addJob(JobInfo job) {
        jobs.add(job);
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException ex) {
            Logger.getLogger(PipelineInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
