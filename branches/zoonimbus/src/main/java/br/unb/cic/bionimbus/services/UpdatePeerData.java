/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 *
 * @author gabriel
 */
public class UpdatePeerData implements Watcher  {

    private ZooKeeperService zkService;
    private Service service;
    
    public UpdatePeerData(ZooKeeperService zkService, Service service) {
        this.zkService = zkService;
        this.service = service;
    }

    
    /**
     * Recebe as notificações de evento do zookeeper.
     * @param event evento que identifica a mudança realizada no zookeeper
     */
    @Override
    public void process(WatchedEvent event){
        //chamada para alertar servico que adicionou o watcher, tratar evento na service
        service.event(event);
        
        //Realiza a solicitação para um novo observer
        try {
            switch(event.getType()){
            
                case NodeChildrenChanged:
                    if(zkService.getZNodeExist(event.getPath(), false))
                        zkService.getChildren(event.getPath(), this);
                break;
                case NodeDataChanged:
                    if(zkService.getZNodeExist(event.getPath(), false))
                        zkService.getData(event.getPath(), this);
                break;
            
            }
        } catch (KeeperException ex) {
            Logger.getLogger(UpdatePeerData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(UpdatePeerData.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
        
}
