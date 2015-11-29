package br.unb.cic.bionimbus.rest.response;

/**
 * An upload response definition
 * @author Vinicius
 */
public class UploadResponse implements ResponseInfo {

    private boolean uploaded;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (uploaded ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UploadResponse other = (UploadResponse) obj;

        return uploaded == other.uploaded;
    }

}
