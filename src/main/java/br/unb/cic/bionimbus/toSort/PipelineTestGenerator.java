/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.PipelineInfo;
import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
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
    private static List<PluginService> servicesTemplates;
    private static List<PluginInfo> resourceTemplates;

    private static void generateTestValues() {
        double worstExecStart   =  50000000000d;
        double worstExecMaxStep = 500000000000d;
        
        int numMaxModes = 3;
        double modeStart   =  50000000000d;
        double modeMaxStep = 500000000000d;
        ArrayList<Double> modeTemplates = new ArrayList<Double>();
        
        int numMaxServices = 5;
        
        int numMaxResources = 6;
        double minCpuFrequency = 2400000000d;
        double maxCpuFrequency = 4000000000d;
        double minCostPerHour = 0.0002d;
        double maxCostPerHour = 0.05d;
        
        int numTasksStep = 10;
        int numMaxTasks = 10;
        List<Integer> numTasksList = new ArrayList<Integer>();
        
        Random rn = new Random(new java.util.Date().getTime());
        
        // generate mode templates
        modeTemplates.add(modeStart);
        for (int i=0; i<numMaxModes-1; i++) {
            modeTemplates.add(rn.nextDouble()*modeMaxStep + modeTemplates.get(i));
        }

        // generate services
        for (int i=1; i<=numMaxServices; i++) {
            PluginService service = new PluginService();
            service.setId(UUID.randomUUID().toString());
            double chance = rn.nextDouble();
            
            // add random number of modes
            for (int j=0; j<numMaxModes; j++) {
                if (chance > rn.nextDouble())
                    service.addModeToHistory(modeTemplates.get(j));
            }
            
            // add one random mode if mode list is empty
            if (service.getHistoryMode().isEmpty()) {
                service.addModeToHistory(modeTemplates.get(rn.nextInt(numMaxModes-1)));
            }
            servicesTemplates.add(service);
        }
        
        // generate resources
        for (int i=1; i<=numMaxResources; i++) {
            PluginInfo resource = new PluginInfo();
            resource.setId(UUID.randomUUID().toString());
            double freq = minCpuFrequency + rn.nextDouble()*(maxCpuFrequency - minCpuFrequency);
            resource.setFactoryFrequencyCore(freq);
            double cost = minCostPerHour +  rn.nextDouble()*(maxCostPerHour - minCostPerHour);
            resource.setCostPerHour(cost);
            resource.setServices(servicesTemplates);
            resource.setHost(new Host("0.0.0.0", 0));
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
                JobInfo job = new JobInfo(worstExecStart+(rn.nextDouble()*worstExecMaxStep));
                job.setServiceId(servicesTemplates.get(rn.nextInt(servicesTemplates.size())).getId());
                pipeline.addJob(job);
            }
            pipelinesTemplates.add(pipeline);
        }
    }
    
    public static List<PipelineInfo> getPipelinesTemplates () {
        if (pipelinesTemplates == null) {
            pipelinesTemplates = new ArrayList<PipelineInfo>();
            servicesTemplates = new ArrayList<PluginService>();
            resourceTemplates = new ArrayList<PluginInfo>();
            generateTestValues();
        }
        
        return pipelinesTemplates;
    }
    
    public static List<PluginService> getServicesTemplates () {
        if (servicesTemplates == null) {
            pipelinesTemplates = new ArrayList<PipelineInfo>();
            servicesTemplates = new ArrayList<PluginService>();
            resourceTemplates = new ArrayList<PluginInfo>();
            generateTestValues();
        }
        
        return servicesTemplates;
    }
    
    public static List<PluginInfo> getResourceTemplates () {
        if (resourceTemplates == null) {
            pipelinesTemplates = new ArrayList<PipelineInfo>();
            servicesTemplates = new ArrayList<PluginService>();
            resourceTemplates = new ArrayList<PluginInfo>();
            generateTestValues();
        }
        
        return resourceTemplates;
    }
    
    // main implemented solely to test the tests generator
    public static void main(String[] args) {
        System.out.println("Pipeline size: " + getPipelinesTemplates().size());
        System.out.println("Services size: " + getServicesTemplates().size());
        System.out.println("Resource size: " + getResourceTemplates().size());
        
        int i=0;
        for (PipelineInfo p : getPipelinesTemplates()) {
            System.out.println("Pipeline " + i + " - size: " + p.getJobs().size());
            i++;
        }
        
        System.out.println(getPipelinesTemplates());
        System.out.println(getServicesTemplates());
        System.out.println(getResourceTemplates());
    }
}

