/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest.request;


/**
 *
 * @author biolabid2
 */
public class CreateElasticityRequest implements RequestInfo {
    
    private String provider;
    private String type;
    private String instanceName;
    private String operation;
    private String idInstance;
    
    public CreateElasticityRequest(){
        
    
    }
    
    public CreateElasticityRequest(String provider, String type, String instanceName, String operation, String idInstance) {
        this.provider = provider;
        this.type = type;
        this.instanceName = instanceName;
        this.operation = operation;
        this.idInstance = idInstance;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getIdInstance() {
        return idInstance;
    }

    public void setIdInstance(String idInstance) {
        this.idInstance = idInstance;
    }
    
    
    
}
