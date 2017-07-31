/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.tests;

import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginService;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author willian
 */
public abstract class PipelineTestGenerator {

    protected List<Workflow> pipelinesTemplates;
    protected List<PluginService> servicesTemplates;
    protected List<PluginInfo> resourceTemplates;

    protected abstract void generatePipelineTemplates();

    protected abstract void generateResourcesTemplates();

    protected abstract void generateServicesTemplates();

    public List<Workflow> getPipelinesTemplates() {
        if (pipelinesTemplates == null) {
            pipelinesTemplates = new ArrayList<>();
            generatePipelineTemplates();
        }

        return pipelinesTemplates;
    }

    public List<PluginService> getServicesTemplates() {
        if (servicesTemplates == null) {
            servicesTemplates = new ArrayList<>();
            generateServicesTemplates();
        }

        return servicesTemplates;
    }

    public List<PluginInfo> getResourceTemplates() {
        if (resourceTemplates == null) {
            resourceTemplates = new ArrayList<>();
            generateResourcesTemplates();
        }

        return resourceTemplates;
    }

    // main implemented solely to test the tests generator
    public static void main(String[] args) {

        PipelineTestGenerator gen = new FromMockFileTestGenerator(1);

        System.out.println("Pipeline size: " + gen.getPipelinesTemplates().size());
        System.out.println("Services size: " + gen.getServicesTemplates().size());
        System.out.println("Resource size: " + gen.getResourceTemplates().size());

        int i = 0;
        for (Workflow p : gen.getPipelinesTemplates()) {
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
