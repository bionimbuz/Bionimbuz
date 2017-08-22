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
package br.unb.cic.bionimbuz.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.inject.Singleton;

import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.services.discovery.DiscoveryService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.toSort.Listeners;

/**
 * @author biocloud1
 */
@Singleton
public abstract class AbstractBioService implements Service, Runnable, Listeners {

    protected CloudMessageService cms;
    protected RepositoryService rs;
    protected List<Listeners> listeners;
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<>();

    /**
     * Método que resgata os peers do zookeeper, que retorna um mapa com os
     * valores dos plugins;
     *
     * @return
     */
    public Map<String, PluginInfo> getPeers() {
        List<String> children;
        cloudMap.clear();
        try {
            children = cms.getChildren(Path.PEERS.getFullPath(), null);
//            System.out.println("[AbstractBioService] children got: " + children.size());
            for (String pluginId : children) {
                ObjectMapper mapper = new ObjectMapper();
                String datas = cms.getData(Path.NODE_PEER.getFullPath(pluginId), null);
//                System.out.println("[AbstractBioService] data got: " + datas);
                if (datas != null && !datas.trim().isEmpty()) {
                    PluginInfo myInfo = mapper.readValue(datas, PluginInfo.class);
//                    System.out.println("[AbstractBioService] info mapped: " + myInfo.toString());
                    if(cms.getZNodeExist(Path.STATUS.getFullPath(pluginId), null)){ 
                       cloudMap.put(myInfo.getId(), myInfo);
//                        System.out.println("[AbstractBioService] peer put: " + myInfo.getId());
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cloudMap;
    }  
}
