package br.unb.cic.bionimbus.rest.response;

/**
 *
 * @author Vinicius
 */
public class StartWorkflowResponse implements ResponseInfo {

    private boolean workflowProcessed;

    public StartWorkflowResponse() {
    }

    public StartWorkflowResponse(boolean workflowProcessed) {
        this.workflowProcessed = workflowProcessed;
    }

    public void setWorkflowProcessed(boolean workflowProcessed) {
        this.workflowProcessed = workflowProcessed;
    }

    public boolean isWorkflowProcessed() {
        return workflowProcessed;
    }
}
