/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
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
    protected RepositoryService rs;
    protected List<Listeners> listeners;
    protected BioNimbusConfig config;
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();

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
