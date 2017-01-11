/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest.request;

import br.unb.cic.bionimbuz.model.SLA;
import br.unb.cic.bionimbuz.model.Workflow;

/**
 *
 * @author biolabid2
 */
public class StartSlaRequest implements RequestInfo{
    private SLA sla;
    private Workflow workflow;
    
    public StartSlaRequest(){
    
    }
    public StartSlaRequest(SLA sla, Workflow workflow) {
        this.sla=sla;
        this.workflow=workflow;
    }

    /**
     * @return the sla
     */
    public SLA getSla() {
        return sla;
    }

    /**
     * @param sla the sla to set
     */
    public void setSla(SLA sla) {
        this.sla = sla;
    }

    /**
     * @return the workflow
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }
    
}
