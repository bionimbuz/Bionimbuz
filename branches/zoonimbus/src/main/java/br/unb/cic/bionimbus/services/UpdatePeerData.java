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
    
    public UpdatePeerData(ZooKeeperService zkService) {
        this.zkService = zkService;
    }

    
    
    @Override
    public void process(WatchedEvent event){
        //Tratar eventos quando ocorrerem,
        //como retornar para a classe que realizou a instância do Wath?
        
        
        
        //Realiza a solicitação para um novo observer
        try {
            System.out.println(event);
            zkService.getData(event.getPath(), this);
        } catch (KeeperException ex) {
            Logger.getLogger(UpdatePeerData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(UpdatePeerData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
}
