/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.controller;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Abstract class that will be extended by Controllers (SLAController and
 * JobController)
 *
 * @author Vinicius
 */
public abstract class AbstractBioController implements Controller, Runnable {
    protected CloudMessageService cms;
    protected BioNimbusConfig config;
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<>();

    /**
     * Retrieves Zookeeper peers
     *
     * @return Map<String, PluginInfo> 
     */
    public Map<String, PluginInfo> getPeers() {
        List<String> children;
        
        // Initializes cloud map
        cloudMap.clear();

        try {
            // Get children from PEERS path
            children = cms.getChildren(CuratorMessageService.Path.PEERS.getFullPath(), null);

            // Iterates over the children getting Plugin ID
            for (String pluginId : children) {
                ObjectMapper mapper = new ObjectMapper();
                
                // Get data from NODE_PEER path
                String datas = cms.getData(CuratorMessageService.Path.NODE_PEER.getFullPath(pluginId), null);

                if (datas != null && !datas.trim().isEmpty()) {
                    PluginInfo myInfo = mapper.readValue(datas, PluginInfo.class);

                    if (cms.getZNodeExist(CuratorMessageService.Path.STATUS.getFullPath(pluginId), null)) {
                        
                        // Puts it into cloud map
                        cloudMap.put(myInfo.getId(), myInfo);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(AbstractBioController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Returns it
        return cloudMap;
    }
}
