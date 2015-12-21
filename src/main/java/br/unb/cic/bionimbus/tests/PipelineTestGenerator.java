/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.tests;

import br.unb.cic.bionimbus.client.PipelineInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author willian
 */
public abstract class PipelineTestGenerator {

    protected List<PipelineInfo> pipelinesTemplates;
    protected List<PluginService> servicesTemplates;
    protected List<PluginInfo> resourceTemplates;

    protected abstract void generatePipelineTemplates();

    protected abstract void generateResourcesTemplates();

    protected abstract void generateServicesTemplates();

    public List<PipelineInfo> getPipelinesTemplates() {
        if (pipelinesTemplates == null) {
            pipelinesTemplates = new ArrayList<PipelineInfo>();
            generatePipelineTemplates();
        }

        return pipelinesTemplates;
    }

    public List<PluginService> getServicesTemplates() {
        if (servicesTemplates == null) {
            servicesTemplates = new ArrayList<PluginService>();
            generateServicesTemplates();
        }

        return servicesTemplates;
    }

    public List<PluginInfo> getResourceTemplates() {
        if (resourceTemplates == null) {
            resourceTemplates = new ArrayList<PluginInfo>();
            generateResourcesTemplates();
        }

        return resourceTemplates;
    }

    // main implemented solely to test the tests generator
    public static void main(String[] args) {
        PipelineTestGenerator gen = new FromMockFileTestGenerator();

        System.out.println("Pipeline size: " + gen.getPipelinesTemplates().size());
        System.out.println("Services size: " + gen.getServicesTemplates().size());
        System.out.println("Resource size: " + gen.getResourceTemplates().size());

        int i = 0;
        for (PipelineInfo p : gen.getPipelinesTemplates()) {
            System.out.print(p.getJobs().size() + ",");
            //System.out.println("Pipeline " + i + " - size: " + p.getJobs().size());
            i++;
        }
        System.out.println("");
//        System.out.println(gen.getPipelinesTemplates());
        System.out.println(gen.getServicesTemplates());
//        System.out.println(gen.getResourceTemplates());
    }
}
