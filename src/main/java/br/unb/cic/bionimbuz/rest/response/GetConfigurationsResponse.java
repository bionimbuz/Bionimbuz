package br.unb.cic.bionimbuz.rest.response;

import br.unb.cic.bionimbuz.model.Instance;
import java.util.List;

import br.unb.cic.bionimbuz.plugin.PluginService;

/**
 *
 * @author Vinicius
 */
public class GetConfigurationsResponse implements ResponseInfo {

    private List<PluginService> servicesList;

    private List<String> references;

    private List<String> supportedFormats;
    
    private List<Instance> instances;

    public GetConfigurationsResponse() {
    }
//, List<Instance> instancesList 
    public GetConfigurationsResponse(List<PluginService> servicesList, List<String> references, List<String> supportedFormats, List<Instance> instances) {
        this.servicesList = servicesList;
        this.references = references;
        this.supportedFormats = supportedFormats;
        this.instances = instances; 
    }

    public List<PluginService> getServicesList() {
        return servicesList;
    }

    public void setServicesList(List<PluginService> servicesList) {
        this.servicesList = servicesList;
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    public List<String> getSupportedFormats() {
        return supportedFormats;
    }

    public void setSupportedFormats(List<String> supportedFormats) {
        this.supportedFormats = supportedFormats;
    }
    
    public List<Instance> getInstances() {
        return instances;
    }
   
    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

}
