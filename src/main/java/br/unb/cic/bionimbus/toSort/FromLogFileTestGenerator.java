/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.PipelineInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.floor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author willian
 */
public class FromLogFileTestGenerator extends PipelineTestGenerator {
    
    private double window; // in secs
    private String pipeFile;
    protected String resFile;
    
    public FromLogFileTestGenerator(double window, String pipeFile, String resFile) {
        this.window = window;
        this.pipeFile = pipeFile;
        this.resFile = resFile;
    }
    
    protected FromLogFileTestGenerator() {}

    @Override
    protected void generatePipelineTemplates() {
        double windowEnd = window; // in secs
                
        try {
            BufferedReader br = new BufferedReader(new FileReader(pipeFile));
            String line = br.readLine();
            long clock;

            // jump though the comments
            while (line != null) {
                if (line.charAt(0) != ';')
                    break;
                line = br.readLine();
            }
            
            // get proc clock on the first line
            clock = Long.parseLong(line);
            
            PipelineInfo pipeline = new PipelineInfo();
            
            // for each line
            line = br.readLine();
            while (line != null) {
                // split all fields
                String[] elements = line.split("\\s+");
                
                // only work with tasks that have a submit time
                if (!elements[2].equals("-1")) {
                    
                    // get submit time
                    double submitTime = Double.parseDouble(elements[2]);

                    // create a new pipeline for a window and add the previous one to the pipeline list
                    if (submitTime > windowEnd) {
                        pipelinesTemplates.add(pipeline);
                        pipeline = new PipelineInfo();
                        windowEnd = floor(1 + submitTime/window)*window;
                    }
                
                    // set task execution cycles based test file enviroment
                    JobInfo job = new JobInfo(Double.parseDouble(elements[4])*clock);
                    job.testId = Long.parseLong(elements[1]);
                    pipeline.addJob(job);
                }
                    
                line = br.readLine();
            }
            
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(FromLogFileTestGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    protected void generateResourcesTemplates() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(resFile));
            String line = br.readLine();
            
            // for each line
            Integer id = 0;
            while (line != null) {
                // split all fields
                String[] elements = line.split("\\s+");
                
                // don't add the resource if there is no cpu freq
                String freq;
                if ((freq = elements[3]).equals("-")) {
                    line = br.readLine();
                    continue;
                }
                
                // create the resource
                PluginInfo res = new PluginInfo();
                res.setId(id.toString());
                res.setInstanceName(elements[1]);
                res.setFactoryFrequencyCore(Double.parseDouble(freq)*1000000000);
                res.setCostPerHour(Double.parseDouble(elements[7]));
                
                resourceTemplates.add(res);
                    
                line = br.readLine();
                id++;
            }
            
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(FromLogFileTestGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void generateServicesTemplates() {
        
    }
    
}
