package br.unb.cic.bionimbus.rest.model;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Object model of a file sent from the user web application
 * @author Vinicius
 */
@Entity
@Table(name = "tb_files")
public class UploadedFileInfo implements Serializable {

    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
    private String id;
    private Long userId;
    private String name;
    private String uploadTimestamp;
    private long size;

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
}
