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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.sched;

import br.unb.cic.bionimbuz.plugin.PluginTask;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gabriel
 */
public class SchedExecute extends Thread {

    private final Queue<PluginTask> runningJobs = new ConcurrentLinkedQueue<PluginTask>();

    private boolean taskExecuted;

    private String service;

    public SchedExecute() {
    }

    public boolean executeTask(PluginTask task, String service) {
        taskExecuted = false;
        this.service = service;
        runningJobs.add(task);
        return taskExecuted;
    }

    @Override
    public void run() {
        while (true) {
            if (!runningJobs.isEmpty()) {
                PluginTask task = runningJobs.remove();
                try {
                    Runtime.getRuntime().exec(service + " -i " + task.getJobInfo().getInputFiles() + " -o " + task.getJobInfo().getOutputs());
                } catch (IOException ex) {
                    Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                }
                taskExecuted = true;
            }
        }
    }

}
