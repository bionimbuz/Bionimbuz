package br.unb.cic.bionimbus.model;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Vinicius
 */
@Entity
@Table(name = "tb_workflow")
public class Workflow implements Serializable {

    @Id
    private String id;

    @Transient
    private List<Job> jobs;

    private String creationDatestamp;

    private long userId;

    private String description;

    @Enumerated(EnumType.STRING)
    private WorkflowStatus status;

    public Workflow() {
        this.jobs = null;
        this.creationDatestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        this.userId = 0l;
        this.description = null;
    }

    public Workflow(List<Job> jobs) {
        this.jobs = new ArrayList<>(jobs);
        this.creationDatestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());;
        this.userId = 0l;
        this.description = null;
    }

    public Workflow(Long userId, String description) {
        this.userId = userId;
        this.creationDatestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        this.jobs = new ArrayList<>();
        this.description = description;
        this.status = WorkflowStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public void addJob(Job job) {
        jobs.add(job);
    }

    public String getCreationDatestamp() {
        return creationDatestamp;
    }

    public void setCreationDatestamp(String creationDatestamp) {
        this.creationDatestamp = creationDatestamp;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException ex) {
            Logger.getLogger(Workflow.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
