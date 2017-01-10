/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest.request;

import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.model.Workflow;

/**
 *
 * @author biolabid2
 */
public class StartElasticityRequest implements RequestInfo{
    private Instance instance;
    private Workflow workflow;
    
    public StartElasticityRequest(){
    
    }

    public StartElasticityRequest(Instance instance, Workflow workflow) {
        this.instance = instance;
        this.workflow = workflow;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }
    
    
    
}
