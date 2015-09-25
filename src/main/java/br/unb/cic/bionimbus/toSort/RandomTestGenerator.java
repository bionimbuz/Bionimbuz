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
public class RandomTestGenerator extends PipelineTestGenerator {
    
    public static int numMaxResources = 30;
    public int numTasksStep = 5;
    public int numMaxTasks = numTasksStep;
    
    private final double modeStart   =  50000000000d;
    private final double modeMaxStep = 500000000000d;
    
    private final Random rn = new Random(9455);

    @Override
    protected void generatePipelineTemplates() {
        double worstExecStart   =  50000000000d;
        double worstExecMaxStep = 500000000000d;
        
        if(servicesTemplates == null) {
            servicesTemplates = new ArrayList<PluginService>();
            generateServicesTemplates();
        }
        
        List<Integer> numTasksList = new ArrayList<Integer>();
        
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
    
    @Override
    protected void generateResourcesTemplates() {
        double minCpuFrequency = 2400000000d;
        double maxCpuFrequency = 4000000000d;
        double minCostPerHour = 0.0002d;
        double maxCostPerHour = 0.05d;

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
    }
    
    @Override
    protected void generateServicesTemplates() {
        int numMaxServices = 5;

        // generate services
        for (int i=1; i<=numMaxServices; i++) {
            PluginService service = new PluginService();
            service.setId(UUID.randomUUID().toString());
            
            // set history mode
//            service.setHistoryMode(modeStart + rn.nextDouble()*modeMaxStep);
            
            servicesTemplates.add(service);
        }
    }
    
}

