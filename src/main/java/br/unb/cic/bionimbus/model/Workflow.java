package br.unb.cic.bionimbus.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author Vinicius
 */
@Entity
@Table(name = "tb_workflow")
public class Workflow implements Serializable{

    @Id
    private final String id = UUID.randomUUID().toString();

    @Transient
    private final List<WorkflowJobInfo> pipeline;

    private final String creationDatestamp;

    private final Long userId;

    private final String description;

    @Enumerated(EnumType.STRING)
    private WorkflowStatus status;

    public Workflow() {
        this.pipeline = null;
        this.creationDatestamp = null;
        this.userId = null;
        this.description = null;
    }

    public Workflow(Long userId, String description) {
        this.userId = userId;
        this.creationDatestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        this.pipeline = new ArrayList<>();
        this.description = description;
        this.status = WorkflowStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCreationDatestamp() {
        return creationDatestamp;
    }

    public String getDescription() {
        return description;
    }

    public List<WorkflowJobInfo> getPipeline() {
        return pipeline;
    }

    public void addJobToPipeline(WorkflowJobInfo job) {
        this.pipeline.add(job);
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public WorkflowStatus getStatus() {
        return status;
    }
}
