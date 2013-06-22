/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;


import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import com.google.inject.Singleton;
import java.io.IOException;
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
@Singleton
public abstract class AbstractBioService implements Service, P2PListener, Runnable {

    protected ZooKeeperService zkService;
    private static final String FILES = "/files";
    private static final String ROOT_PEER = "/peers";
    private static final String STATUS = "STATUS";
    private static final String SEPARATOR = "/";
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    private Map<String, PluginFile> savedFiles = new ConcurrentHashMap<String, PluginFile>();
    
    
    /**
     * Método que resgata os peers do zookeeper, que retorna um mapa com os valores dos plugins;
     * @return 
     */
    public Map<String, PluginInfo> getPeers(){
        List<String> children;
        cloudMap.clear();
        try {
            children = zkService.getChildren(ROOT_PEER, null);
            for (String child : children) {
                ObjectMapper mapper = new ObjectMapper();
                String datas = zkService.getData(ROOT_PEER +SEPARATOR+ child, null);
                PluginInfo myInfo = mapper.readValue(datas, PluginInfo.class);
                    
                if(zkService.getZNodeExist(myInfo.getPath_zk()+SEPARATOR+STATUS, false)){ 
                    cloudMap.put(myInfo.getId(), myInfo);
                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return cloudMap;
    }
    public Map<String, PluginFile> getFiles(){
        List<String> children;
        List<String> childrenF;
        savedFiles.clear();
        try {
            children = zkService.getChildren(ROOT_PEER, null);
            for (String child : children) {
                ObjectMapper mapper = new ObjectMapper();
                PluginFile files = mapper.readValue(zkService.getData(ROOT_PEER +SEPARATOR+ child+FILES, null), PluginFile.class);
                    
                if(zkService.getZNodeExist(files.getPath(), false)){ 
                    savedFiles.put(files.getId(), files);
                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return savedFiles;
    }

}
