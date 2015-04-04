/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

/**
 *
 * @author willian
 */
public class TesteCurator implements Watcher{
    private static final int SESSION_TIMEOUT = 5000;
    private ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    
    public void connect(String hosts) throws IOException, InterruptedException {
        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
        connectedSignal.await();
    }
    
    @Override
    public void process(WatchedEvent event) { // Watcher interface
        if (event.getState() == KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }
    
    public void create(String groupName) throws KeeperException,InterruptedException {
        String path = "/" + groupName;
        String createdPath = zk.create(path, null/*data*/, Ids.OPEN_ACL_UNSAFE,
        CreateMode.PERSISTENT);
        System.out.println("Created " + createdPath);
    }
    public void close() throws InterruptedException {
        zk.close();
    }
     public void event(WatchedEvent eventType) {
         String path =eventType.getPath();
//         String content =
     }
    public static void main(String[] args) throws IOException {
        CloudMessageService cms = new CuratorMessageService("192.168.1.121:2181");
        
        PluginFile file = new PluginFile();
        file.setId("abc.pdf");
        file.setName("abc.pdf");
        file.setSize(154L);
        
//        zk.connect("192.168.1.111");
        cms.createZNode(CreateMode.PERSISTENT, "/pending_save3","");

//        zk.getData("/pending_save", );
        cms.createZNode(CreateMode.PERSISTENT, "/pending_save3/files", file.toString());
            
    }
}
