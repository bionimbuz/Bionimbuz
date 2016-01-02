package br.unb.cic.bionimbus.rest.response;

/**
 *
 * @author Vinicius
 */
public class StartWorkflowResponse implements ResponseInfo {

    private Boolean workflowProcessed;

    public StartWorkflowResponse() {
    }

    public StartWorkflowResponse(Boolean workflowProcessed) {
        this.workflowProcessed = workflowProcessed;
    }

    public void setWorkflowProcessed(Boolean workflowProcessed) {
        this.workflowProcessed = workflowProcessed;
    }

    public Boolean isWorkflowProcessed() {
        return workflowProcessed;
    }
}
