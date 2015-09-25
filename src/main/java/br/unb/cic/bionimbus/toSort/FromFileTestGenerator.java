/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.PipelineInfo;
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
public class FromFileTestGenerator extends PipelineTestGenerator {

    @Override
    protected void generatePipelineTemplates() {
        double window = 50; // in secs
        double windowEnd = window; // in secs
                
        try {
            BufferedReader br = new BufferedReader(new FileReader("LLNL-Thunder-2007-1.1-cln.swf"));
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
            Logger.getLogger(FromFileTestGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    protected void generateResourcesTemplates() {
        
    }

    @Override
    protected void generateServicesTemplates() {
        
    }
    
}
