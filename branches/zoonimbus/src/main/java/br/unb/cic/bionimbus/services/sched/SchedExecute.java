/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.sched;

import br.unb.cic.bionimbus.plugin.PluginTask;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gabriel
 */
public class SchedExecute extends Thread{
    
    private final Queue<PluginTask> runningJobs = new ConcurrentLinkedQueue<PluginTask> ();
    
    private boolean taskExecuted;
    
 
    private String service;

    public SchedExecute() {
    }

    public boolean executeTask(PluginTask task, String service){
        taskExecuted =false;
        this.service = service;
        runningJobs.add(task);
        return taskExecuted;
    } 
    
    
    
//    cria a thread de execução dos jobs
//         new Thread(new Runnable() {
//           public void run() {
//                         
//           } 
//        }).start();

        @Override
        public void run() {
            while (true) {
                if(!runningJobs.isEmpty()){
                    PluginTask task = runningJobs.remove();
                    try {
                        Runtime.getRuntime().exec(service+ " -i " + task.getJobInfo().getInputs() + " -o " + task.getJobInfo().getOutputs());
                    } catch (IOException ex) {
                        Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    taskExecuted = true;
                }
            }              
    }
    
    
}
