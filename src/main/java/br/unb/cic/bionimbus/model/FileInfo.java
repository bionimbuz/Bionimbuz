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
 */
@Entity
@Table(name = "tb_files")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FileInfo implements Serializable {

    @Id
    private String id;

    private String name;

    private long size;

    private long userId;

    private String uploadTimestamp;

    private String hash;
    
    private String bucket;

    @Transient
    @JsonProperty("payload")
    private byte[] payload;

    public FileInfo() {
        this.bucket = null;
    }

    public FileInfo(String id) {
        this.id = id;
        this.bucket = null;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(String uploadedTimestamp) {
        this.uploadTimestamp = uploadedTimestamp;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

}
