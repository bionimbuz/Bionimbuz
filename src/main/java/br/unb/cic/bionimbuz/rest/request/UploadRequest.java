package br.unb.cic.bionimbuz.rest.request;

import java.io.InputStream;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import br.unb.cic.bionimbuz.model.FileInfo;

/**
 * Defines an upload request to be used in a REST request
 * 
 * @author Vinicius
 */
public class UploadRequest implements RequestInfo {
    
    private InputStream data;
    private FileInfo fileInfo;
    private String bucketName;
    
    // --------------------------------------------------------------
    // Constructors.
    // --------------------------------------------------------------
    public UploadRequest() {
        super();
    }
    
    // --------------------------------------------------------------
    // get/set
    // --------------------------------------------------------------
    public InputStream getData() {
        return this.data;
    }
    public FileInfo getFileInfo() {
        return this.fileInfo;
    }
    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public void setData(InputStream data) {
        this.data = data;
    }
    @FormParam("file_info")
    @PartType(MediaType.APPLICATION_JSON)
    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
    
    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
