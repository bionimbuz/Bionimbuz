/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.sched;

import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 *
 * @author gabriel
 */
public class SchedUpdatePeerData implements Watcher  {

    private ZooKeeperService zkService;
    private Service service;
    
   
    public SchedUpdatePeerData(ZooKeeperService zkService,Service service) {
        this.zkService = zkService;
        this.service=service;
    }
    
    
    @Override
    public void process(WatchedEvent event){
        System.out.println(event);
        service.event(event);
        try {
            switch(event.getType()){
            
                case NodeChildrenChanged:
                    zkService.getChildren(event.getPath(), this);
                    break;
                case NodeDataChanged:
                    zkService.getData(event.getPath(), this);
                    break;
                    
            }
            //Realiza a solicitação para um novo observer
        } catch (KeeperException ex) {
            Logger.getLogger(SchedUpdatePeerData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SchedUpdatePeerData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
        
}

