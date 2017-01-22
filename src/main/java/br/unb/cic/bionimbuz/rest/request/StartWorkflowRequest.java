package br.unb.cic.bionimbuz.rest.request;

import br.unb.cic.bionimbuz.model.Workflow;

/**
 *
 * @author Vinicius
 */
public class StartWorkflowRequest implements RequestInfo {

    private Workflow workflow;
    
    public StartWorkflowRequest() {
    }

    public StartWorkflowRequest(Workflow workflow) {
        this.workflow = workflow;
    }
     
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Workflow getWorkflow() {
        return workflow;
    }
    
}
