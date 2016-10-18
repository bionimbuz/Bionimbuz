/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbus.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.model.Job;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.security.AESEncryptor;

/**
 *
 * @author willian
 */
public class FromMockFileTestGenerator extends FromLogFileTestGenerator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FromMockFileTestGenerator.class);
    
    private final int numPipelines;
    
    public FromMockFileTestGenerator(int numPipelines) {
        this.numPipelines = numPipelines;
        final String pathHome = System.getProperty("user.dir");
        final String path = pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/";
        final AESEncryptor aes = new AESEncryptor();
        try {
            // TO-DO: Remove comment after William Final Commit
            // aes.decrypt(path+"resSample.txt");
        } catch (final Exception ex) {
            LOGGER.error("Exception - " + ex.getMessage());
        }
        this.resFile = path + "resSample.txt";
    }
    
    @Override
    protected void generatePipelineTemplates() {
        Job taskList[] = null;
        
        // get pipeline file path
        final String pathHome = System.getProperty("user.dir");
        final String path = pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/";
        final AESEncryptor aes = new AESEncryptor();
        try {
            // TO-DO: Remove comment after Willian Final Commit
            // aes.decrypt(path+"pipelineSample.txt");
        } catch (final Exception ex) {
            LOGGER.error("Exception - " + ex.getMessage());
        }

        
        for (int ii = 1; ii <= this.numPipelines; ii++) {
            try (
                 BufferedReader br = new BufferedReader(new FileReader(path + "pipelineSample" + ii + ".txt"))) {

                // get first line: number of tasks
                String line = br.readLine();
                final int tasksNumber = Integer.parseInt(line);
                taskList = new Job[tasksNumber];
                
                // get next tasksNumber lines: each task
                for (int i = 0; i < tasksNumber; i++) {
                    // generate a new jobInfo from json
                    line = br.readLine();
                    final Job jobInfo = new Job();
                    jobInfo.setId("i" + i);
                    jobInfo.setTimestamp(0l);
                    
                    // set serviceId from json
                    int lastComa = line.indexOf(",");
                    jobInfo.setServiceId(line.substring(line.indexOf("serviceId:") + 10, lastComa));
                    
                    // set args from json
                    lastComa = line.indexOf(",", lastComa + 1);
                    jobInfo.setArgs(line.substring(line.indexOf("args:") + 5, lastComa));
                    
                    // get input list from json
                    int lastBracket = line.indexOf("]");
                    String io = line.substring(line.indexOf("inputs:[") + 8, lastBracket);
                    final String inputs[] = io.split(",");
                    
                    // set inputs
                    // TODO: change addInput to receive the filename instead of its zookeeper id
                    for (final String inp : inputs) {
                        final FileInfo f = new FileInfo();
                        f.setId(inp);
                        f.setName("inp0");
                        f.setUploadTimestamp("00/00/00");
                        f.setHash("hash");
                        f.setPayload(new byte[10]);
                        
                        jobInfo.addInput(f);
                    }
                    
                    // get output list from json
                    lastBracket = line.indexOf("]", lastBracket + 1);
                    io = line.substring(line.indexOf("outputs:[") + 9, lastBracket);
                    final String outputs[] = io.split(",");
                    
                    // set outputs
                    for (final String out : outputs) {
                        jobInfo.addOutput(out);
                    }
                    
                    // put it into the map to, furthermore, set the dependencies
                    taskList[i] = jobInfo;
                }
                
                // get the remaining lines: dependency matrix
                for (int i = 0; i < tasksNumber; i++) {
                    final String deps[] = br.readLine().split(",");
                    for (int j = 0; j < tasksNumber; j++) {
                        if (Integer.parseInt(deps[j]) == 1) {
                            taskList[i].addDependency(taskList[j].getId());
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.getMessage();
                e.printStackTrace();
            }
            
            // push taskList to the pipelineTemplates
            final Workflow p = new Workflow(Arrays.asList(taskList));
            
            LOGGER.info("[TestGen] taskList " + taskList.length);
            LOGGER.info("[TestGen] pipeline " + p.getJobs().size());
            
            this.pipelinesTemplates.add(p);
        }
    }
    
    @Override
    protected void generateServicesTemplates() {
        final Job taskList[] = null;
        // get service file path
        final String pathHome = System.getProperty("user.dir");
        final String path = pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/";
        final AESEncryptor aes = new AESEncryptor();
        try {
            // TO-DO: Remove comment after William Final Commit
            // aes.decrypt(path+"servicesSample.txt");
        } catch (final Exception ex) {
            LOGGER.error("Exception - " + ex.getMessage());
        }

        try (
             BufferedReader br = new BufferedReader(new FileReader(path + "servicesSample.txt"))) {

            
            String line;
            while ((line = br.readLine()) != null) {
                final PluginService service = new PluginService();
                
                // set serviceId from json
                int lastComa = line.indexOf(",");
                service.setId(line.substring(line.indexOf("serviceId:") + 10, lastComa));
                
                // set args from json
                lastComa = line.indexOf(",", lastComa + 1);
                service.setPath(line.substring(line.indexOf("path:") + 5, lastComa));
                
                // set mode
                service.setPresetMode(Double.parseDouble(line.substring(line.indexOf("mode:") + 5, line.length() - 1)));
                
                // add service
                this.servicesTemplates.add(service);
            }
        } catch (final Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
}
