package br.unb.cic.bionimbus.model;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Contains data used to log something
 *
 * @author Vinicius
 */
@Entity
@Table(name = "tb_workflow_log")
public class Log implements Serializable {

    @Id
    private final String id = UUID.randomUUID().toString();

    private String text;

    private long userId;

    private String workflowId;

    private String timestamp;

    @Enumerated(EnumType.STRING)
    private LogSeverity severity;

    public Log() {
    }

    public Log(String text, long userId, String workflowId, LogSeverity severity, String timestamp) {
        this.text = text;
        this.userId = userId;
        this.workflowId = workflowId;
        this.severity = severity;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public LogSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(LogSeverity severity) {
        this.severity = severity;
    }

}
