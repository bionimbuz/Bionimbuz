/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.unb.cic.bionimbus.services.messaging;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

/**
 *
 * @author willian
 */
public class CuratorMessageService implements CloudMessageService {
    
    CuratorFramework client;
    
    public CuratorMessageService(String connectionString) {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        
        client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        client.start();
    }
    
    @Override
    public void createPersistentZNode(String node, String desc) {
        try {
            Stat s = client.checkExists().forPath(node);
            if (s == null) {
                client.create().withMode(CreateMode.PERSISTENT).
                                withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).
                                forPath(node, (desc == null) ? new byte[0] : desc.getBytes());
            } else {
                Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, "Existent node {0}", node);
            }
        } catch (Exception ex) {
            Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
