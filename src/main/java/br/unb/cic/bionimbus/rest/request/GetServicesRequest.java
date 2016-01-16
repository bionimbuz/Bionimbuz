package br.unb.cic.bionimbus.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Vinicius
 */
public class GetServicesRequest implements RequestInfo {

    @JsonProperty(value = "request")
    private boolean request;    

    public GetServicesRequest() {
    }

    public GetServicesRequest(boolean request) {
        this.request = request;
    }

    public boolean isRequest() {
        return request;
    }

    public void setRequest(boolean request) {
        this.request = request;
    }

}
