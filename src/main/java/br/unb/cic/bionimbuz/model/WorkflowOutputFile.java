package br.unb.cic.bionimbuz.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Used to link a workflow with its output files.
 *
 * @author Vinicius
 */
@Entity
@Table(name = "tb_workflow_output_file")
public class WorkflowOutputFile implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();

    private String workflowId;

    private String outputFilename;

    public WorkflowOutputFile() {
    }

    public WorkflowOutputFile(String workflowId, String outputFileId) {
        this.workflowId = workflowId;
        this.outputFilename = outputFileId;
    }

    public String getId() {
        return id;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

}
