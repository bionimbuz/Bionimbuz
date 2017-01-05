package br.unb.cic.bionimbuz.rest.response;

import br.unb.cic.bionimbuz.model.Workflow;
import java.util.List;

/**
 * Defines the server response to a GetWorkflowStatus request
 *
 * @author Vinicius
 */
public class GetWorkflowStatusResponse implements ResponseInfo {

    private List<Workflow> userWorkflows;

    public GetWorkflowStatusResponse() {
    }

    public GetWorkflowStatusResponse(List<Workflow> userWorkflows) {
        this.userWorkflows = userWorkflows;
    }

    public List<Workflow> getUserWorkflows() {
        return userWorkflows;
    }

    public void setUserWorkflows(List<Workflow> userWorkflows) {
        this.userWorkflows = userWorkflows;
    }

}
