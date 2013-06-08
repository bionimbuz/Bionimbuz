/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;


import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author biocloud1
 */
public abstract class AbstractBioService implements Service, P2PListener, Runnable {

    protected ZooKeeperService zkService;
    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    //public List<PluginInfo> getPeers(){
    public Map<String, PluginInfo> getPeers(){
        List<String> children;
//        List<PluginInfo> listPlugin= new ArrayList<PluginInfo>();

        try {
            children = zkService.getChildren(ROOT_PEER, null);
            for (String child : children) {
                String childStr = zkService.getData(ROOT_PEER +SEPARATOR+ child, null);
                ObjectMapper mapper = new ObjectMapper();
                PluginInfo myInfo = mapper.readValue(childStr, PluginInfo.class);
                cloudMap.put(myInfo.getId(), myInfo);
                //listPlugin.add(myInfo);
                    
            }
        } catch (KeeperException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
//        return listPlugin;
        return cloudMap;
    }
    

}
