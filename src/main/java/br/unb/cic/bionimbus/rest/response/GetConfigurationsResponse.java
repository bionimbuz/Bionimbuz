package br.unb.cic.bionimbus.rest.response;

import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.plugin.PluginService;
import java.util.List;

/**
 *
 * @author Vinicius
 */
public class GetConfigurationsResponse implements ResponseInfo {

    private List<PluginService> servicesList;

    private List<String> references;

    private List<String> supportedFormats;
    
//    private List<Instance> instancesList;

    public GetConfigurationsResponse() {
    }
//, List<Instance> instancesList 
    public GetConfigurationsResponse(List<PluginService> servicesList, List<String> references, List<String> supportedFormats) {
        this.servicesList = servicesList;
        this.references = references;
        this.supportedFormats = supportedFormats;
//        this.instancesList = instancesList; 
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

    /**
     * @return the instancesList
     */
//    public List<Instance> getInstancesList() {
//        return instancesList;
//    }

    /**
     * @param instancesList the instancesList to set
     */
//    public void setInstancesList(List<Instance> instancesList) {
//        this.instancesList = instancesList;
//    }

}
