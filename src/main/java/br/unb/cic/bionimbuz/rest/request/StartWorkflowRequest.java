package br.unb.cic.bionimbuz.rest.request;

import br.unb.cic.bionimbuz.model.SLA;
import br.unb.cic.bionimbuz.model.Workflow;

/**
 *
 * @author Vinicius
 */
public class StartWorkflowRequest implements RequestInfo {

    private Workflow workflow;
    private SLA sla;

    public StartWorkflowRequest() {
    }

    public StartWorkflowRequest(Workflow workflow) {
        this.workflow = workflow;
    }
     
    public StartWorkflowRequest(Workflow workflow, SLA sla) {
        this.workflow = workflow;
        this.sla=sla;
    }
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Workflow getWorkflow() {
        return workflow;
    }
    
    public SLA getSla() {
        return sla;
    }
    
    public void setSLA(SLA sla) {
        this.sla = sla;
    }
}