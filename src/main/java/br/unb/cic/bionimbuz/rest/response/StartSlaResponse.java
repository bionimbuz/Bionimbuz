/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest.response;

import br.unb.cic.bionimbuz.model.SLA;

/**
 *
 * @author biolabid2
 */
public class StartSlaResponse implements ResponseInfo {
    
    private boolean SlaDone;
    private SLA sla;
    public StartSlaResponse() {
    
    }
    
    public StartSlaResponse(SLA sla){
        this.sla=sla;
    }
    
    public StartSlaResponse(boolean SlaDone) {
        this.SlaDone = SlaDone;
    }
    
    /**
     * @return the SlaDone
     */
    public boolean isSlaDone() {
        return SlaDone;
    }

    /**
     * @param SlaDone the SlaDone to set
     */
    public void setSlaDone(boolean SlaDone) {
        this.SlaDone = SlaDone;
    }

    /**
     * @return the sla
     */
    public SLA getSla() {
        return sla;
    }

    /**
     * @param sla the sla to set
     */
    public void setSla(SLA sla) {
        this.sla = sla;
    }
    
}
