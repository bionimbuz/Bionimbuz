/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import java.io.File;
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
import org.apache.zookeeper.server.ZKDatabase;

/**
 *
 * @author biocloud2
 */
public class Teste implements Watcher{
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
       ZooKeeperService zk =new ZooKeeperService();
        PluginFile file = new PluginFile();
        file.setId("abc.pdf");
        file.setName("abc.pdf");
        file.setSize(154L);
        
        try {
            zk.connect("192.168.1.111");
            zk.createPersistentZNode("/pending_save","");
//            zk.getData("/pending_save", );
            zk.createPersistentZNode("/pending_save/files", file.toString());
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Teste.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
