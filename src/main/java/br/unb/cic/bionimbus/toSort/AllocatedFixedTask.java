/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.client.JobInfo;

/**
 *
 * @author willian
 */
public class AllocatedFixedTask {
    public final Double cost;
    public final JobInfo taskRef;

    public AllocatedFixedTask(Double cost, JobInfo taskRef) {
        this.cost = cost;
        this.taskRef = taskRef;
    }
    
}
