package br.unb.cic.bionimbuz.model;


import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.map.ObjectMapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object model of a file sent from the user web application
 */
@Entity
@Table(name = "tb_files")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FileInfo implements Serializable {
    
    private static final long serialVersionUID = -3461106598236899028L;
    
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
    
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public byte[] getPayload() {
        return this.payload;
    }
    
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
    
    public long getSize() {
        return this.size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public String getUploadTimestamp() {
        return this.uploadTimestamp;
    }
    
    public void setUploadTimestamp(String uploadedTimestamp) {
        this.uploadTimestamp = uploadedTimestamp;
    }
    
    public long getUserId() {
        return this.userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getHash() {
        return this.hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public String getBucket() {
        return this.bucket;
    }
    
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    
    public static FileInfo valueOf(final String jsonString) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final FileInfo object = mapper.readValue(jsonString, FileInfo.class);
            return object;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
