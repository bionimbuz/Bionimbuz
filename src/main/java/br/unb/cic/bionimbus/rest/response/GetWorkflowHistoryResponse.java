package br.unb.cic.bionimbus.rest.response;

import br.unb.cic.bionimbus.model.Log;
import java.util.List;

/**
 *
 * @author Vinicius
 */
public class GetWorkflowHistoryResponse implements ResponseInfo {

    private List<Log> history;

    public GetWorkflowHistoryResponse() {
    }

    public GetWorkflowHistoryResponse(List<Log> history) {
        this.history = history;
    }

    public List<Log> getHistory() {
        return history;
    }

    public void setHistory(List<Log> history) {
        this.history = history;
    }

}
