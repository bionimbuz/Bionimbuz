/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest.response;

/**
 *
 * @author biolabid2
 */
public class CreateElasticityResponse implements ResponseInfo {
    private String ip;
    private boolean done;

    public CreateElasticityResponse() {
    }

    public CreateElasticityResponse(String ip, boolean done) {
        this.ip = ip;
        this.done = done;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
    
    
}
