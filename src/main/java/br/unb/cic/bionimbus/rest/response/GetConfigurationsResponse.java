package br.unb.cic.bionimbus.rest.response;

import br.unb.cic.bionimbus.model.Instance;
import java.util.List;

import br.unb.cic.bionimbus.plugin.PluginService;

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
    public GetConfigurationsResponse(List<PluginService> servicesList, List<String> references, List<String> supportedFormats, List<Instance> instancesList) {
        this.servicesList = servicesList;
        this.references = references;
        this.supportedFormats = supportedFormats;
        this.instances = instancesList; 
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
    
    public List<Instance> getInstancesList() {
        return instances;
    }
   
    public void setInstancesList(List<Instance> instancesList) {
        this.instances = instancesList;
    }

}
