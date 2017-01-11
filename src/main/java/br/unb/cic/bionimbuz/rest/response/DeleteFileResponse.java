package br.unb.cic.bionimbuz.rest.response;

import org.jboss.resteasy.core.ServerResponse;

/**
 * Defines a REST response for a Delete File Action
 *
 * @author Vinicius
 */
public class DeleteFileResponse extends ServerResponse implements ResponseInfo {

    private boolean deleted;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
