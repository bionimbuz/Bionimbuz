package br.unb.cic.bionimbuz.rest.response;

import org.jboss.resteasy.core.ServerResponse;

/**
 * An upload response definition
 *
 * @author Vinicius
 */
public class UploadResponse extends ServerResponse implements ResponseInfo {

    private boolean uploaded;
    private String bucketName;
    
    public UploadResponse() {
    }

    public UploadResponse(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
