package br.unb.cic.bionimbus.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Object model of a file sent from the user web application
 *
 * @author Vinicius
 */
@Entity
@Table(name = "tb_files")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UploadedFileInfo implements Serializable {

    @Id
    private String id;

    private Long userId;

    private String name;

    private String uploadTimestamp;

    private long size;
    
    @Transient
    @JsonProperty("payload")
    private byte[] payload;

    public UploadedFileInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(String uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

}
