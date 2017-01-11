package br.unb.cic.bionimbuz.rest.request;

/**
 *
 * @author Vinicius
 */
public class GetWorkflowHistoryRequest implements RequestInfo {

    private String workflowId;

    public GetWorkflowHistoryRequest() {
    }

    public GetWorkflowHistoryRequest(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

}
