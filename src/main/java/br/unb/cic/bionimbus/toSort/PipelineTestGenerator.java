/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.PipelineInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 *
 * @author willian
 */
public class PipelineTestGenerator {
    
    private static List<PipelineInfo> pipelinesTemplates;
    private static List<PluginInfo> resourceTemplates;

    private static void generateTestValues() {
        double worstExecStart   =  50000000000d;
        double worstExecMaxStep = 500000000000d;
        
        int numMaxResources = 2;
        double minCpuFrequency = 2400000000d;
        double maxCpuFrequency = 4000000000d;
        double minCostPerHour = 0.0002d;
        double maxCostPerHour = 0.05d;
        
        int numTasksStep = 2;
        int numMaxTasks = numTasksStep;
        List<Integer> numTasksList = new ArrayList<Integer>();
        
        Random rn = new Random(new java.util.Date().getTime());
        
        // generate resources
        for (int i=1; i<=numMaxResources; i++) {
            PluginInfo resource = new PluginInfo();
            resource.setId(UUID.randomUUID().toString());
            double freq = minCpuFrequency + rn.nextDouble()*(maxCpuFrequency - minCpuFrequency);
            resource.setFactoryFrequencyCore(freq);
            double cost = minCostPerHour +  rn.nextDouble()*(maxCostPerHour - minCostPerHour);
            resource.setCostPerHour(cost);
            resourceTemplates.add(resource);
        }
        
        // set numTaskList
        for (int i=numTasksStep; i<=numMaxTasks; i+=numTasksStep) {
            numTasksList.add(i);
        }
        
        // generate pipelines
        for (Integer numTasks : numTasksList) {
            PipelineInfo pipeline = new PipelineInfo();
            for (int i=0; i<numTasks; i++) {
                JobInfo job = new JobInfo();
                job.setWorstExecution(worstExecStart+(rn.nextDouble()*worstExecMaxStep));
                pipeline.addJob(job);
            }
            pipelinesTemplates.add(pipeline);
        }
    }
    
    public static List<PipelineInfo> getPipelinesTemplates () {
        if (pipelinesTemplates == null) {
            pipelinesTemplates = new ArrayList<PipelineInfo>();
            resourceTemplates = new ArrayList<PluginInfo>();
            generateTestValues();
        }
        
        return pipelinesTemplates;
    }
    
    public static List<PluginInfo> getResourceTemplates () {
        if (resourceTemplates == null) {
            pipelinesTemplates = new ArrayList<PipelineInfo>();
            resourceTemplates = new ArrayList<PluginInfo>();
            generateTestValues();
        }
        
        return resourceTemplates;
    }
    
    // main implemented solely to test the tests generator
    public static void main(String[] args) {
        System.out.println("Pipeline size: " + getPipelinesTemplates().size());
        System.out.println("Resource size: " + getResourceTemplates().size());
        
        int i=0;
        for (PipelineInfo p : getPipelinesTemplates()) {
            System.out.println("Pipeline " + i + " - size: " + p.getJobs().size());
            i++;
        }
        
        System.out.println(getPipelinesTemplates());
        System.out.println(getResourceTemplates());
    }
}
