package br.unb.cic.bionimbuz.rest.response;

import br.unb.cic.bionimbuz.model.Log;
import br.unb.cic.bionimbuz.model.WorkflowOutputFile;
import java.util.List;

/**
 *
 * @author Vinicius
 */
public class GetWorkflowHistoryResponse implements ResponseInfo {

    private List<Log> history;

    // List with output files for the given workflow
    private List<WorkflowOutputFile> workflowOutputFiles;

    public GetWorkflowHistoryResponse() {
    }

    public GetWorkflowHistoryResponse(List<Log> history, List<WorkflowOutputFile> workflowOutputFiles) {
        this.history = history;
        this.workflowOutputFiles = workflowOutputFiles;
    }

    public List<Log> getHistory() {
        return history;
    }

    public void setHistory(List<Log> history) {
        this.history = history;
    }

    public List<WorkflowOutputFile> getWorkflowOutputFiles() {
        return workflowOutputFiles;
    }

    public void setWorkflowOutputFiles(List<WorkflowOutputFile> workflowOutputFiles) {
        this.workflowOutputFiles = workflowOutputFiles;
    }

}
