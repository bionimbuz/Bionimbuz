/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest.response;

import br.unb.cic.bionimbus.model.Instance;

/**
 *
 * @author biolabid2
 */
public class StartElasticityResponse implements ResponseInfo {
    
    private boolean ElasticityDone;
    private Instance elasticity;
    public StartElasticityResponse() {
    
    }

    public boolean isElasticityDone() {
        return ElasticityDone;
    }

    public void setElasticityDone(boolean ElasticityDone) {
        this.ElasticityDone = ElasticityDone;
    }

    public Instance getElasticity() {
        return elasticity;
    }

    public void setElasticity(Instance elasticity) {
        this.elasticity = elasticity;
    }
    
    
    
}
