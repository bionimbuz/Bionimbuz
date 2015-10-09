/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;


import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.toSort.Listeners;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author biocloud1
 */
@Singleton
public abstract class AbstractBioService implements Service, Runnable, Listeners {

    protected CloudMessageService cms;
    protected List<Listeners> listeners;
    protected BioNimbusConfig config;
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();    
    
    /**
     * Método que resgata os peers do zookeeper, que retorna um mapa com os valores dos plugins;
     * @return 
     */
    public Map<String, PluginInfo> getPeers(){
        List<String> children;
        cloudMap.clear();
        try {
            children = cms.getChildren(cms.getPath().PEERS.getFullPath("", "", ""), null);
            for (String pluginId : children) {
                ObjectMapper mapper = new ObjectMapper();
                String id=pluginId.substring(pluginId.indexOf(cms.getPath().UNDERSCORE.toString())+1);
                String datas = cms.getData(cms.getPath().PREFIX_PEER.getFullPath(id, "", ""), null);
                if (datas != null && !datas.trim().isEmpty()){
                    PluginInfo myInfo = mapper.readValue(datas, PluginInfo.class);
                    
                    if(cms.getZNodeExist(cms.getPath().STATUS.getFullPath(id, "", ""), false)){ 
                       cloudMap.put(myInfo.getId(), myInfo);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return cloudMap;
    }
   
}
