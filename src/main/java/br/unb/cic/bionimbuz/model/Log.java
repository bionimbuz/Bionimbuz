package br.unb.cic.bionimbuz.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
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
    @Column(name = "id", length = 40)
    private String id = UUID.randomUUID().toString();

    @Column(name = "text", length = 1000)
    private String text;

    private long userId;

    @Column(name = "workflowId", length = 40)
    private String workflowId;

    private String timestamp;

    private String date;

    @Enumerated(EnumType.STRING)
    private LogSeverity severity;

    public Log() {
    }

    public Log(String text, long userId, String workflowId, LogSeverity severity) {
        this.text = text;
        this.userId = userId;
        this.workflowId = workflowId;
        this.severity = severity;
        this.timestamp = new SimpleDateFormat("hh:mm:ss.SSSS").format(new Date());
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    }

    /**
     * Method that compares Strings and sort them (used to sort Timestamps)
     */
    public static Comparator<Log> comparator = new Comparator<Log>() {

        @Override
        public int compare(Log log1, Log log2) {
            String time1 = log1.getTimestamp();
            String time2 = log2.getTimestamp();

            return time1.compareTo(time2);
        }
    };

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

    public String getDate() {
        return date;
    }

    public LogSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(LogSeverity severity) {
        this.severity = severity;
    }

}
