/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

/**
 *
 * @author willian
 */
public class AllocatedTask {
    public final Float cost;
    public final Task taskRef;

    public AllocatedTask(Float cost, Task task) {
        this.cost = cost;
        taskRef = task;
    }
    
}
