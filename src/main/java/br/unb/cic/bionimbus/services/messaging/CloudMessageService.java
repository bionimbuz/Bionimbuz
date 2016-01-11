
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.messaging;

import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

/**
 *
 * @author willian
 */
public interface CloudMessageService {
    
    public void connect(String connectionString);
    
    public CuratorMessageService.Path getPath();
    
    public void createZNode(CreateMode cm, String node, String desc);
    
    public Boolean getZNodeExist(String path, Watcher watcher);
    
    public List<String> getChildren(String path, Watcher watcher);
    
    public int getChildrenCount(String path, Watcher watcher);
    
    public String getData(String path, Watcher watcher);
    
    public void setData(String path, String data);
    
    public void delete(String path);
    
    public void close();
}
