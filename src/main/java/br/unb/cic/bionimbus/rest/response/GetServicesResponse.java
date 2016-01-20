package br.unb.cic.bionimbus.rest.response;

import br.unb.cic.bionimbus.plugin.PluginService;
import java.util.List;

/**
 *
 * @author Vinicius
 */
public class GetServicesResponse implements ResponseInfo {

    private List<PluginService> servicesList;

    public GetServicesResponse() {
    }

    public GetServicesResponse(List<PluginService> servicesList) {
        this.servicesList = servicesList;
    }

    public List<PluginService> getServicesList() {
        return servicesList;
    }

    public void setServicesList(List<PluginService> servicesList) {
        this.servicesList = servicesList;
    }
}
