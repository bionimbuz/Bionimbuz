package br.unb.cic.bionimbuz.rest.request;

/**
 * Defines an object to request an user workflow status
 *
 * @author Vinicius
 */
public class GetWorkflowStatusRequest implements RequestInfo {

    private Long userId;

    public GetWorkflowStatusRequest() {
    }

    public GetWorkflowStatusRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
