
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.messaging;

/**
 *
 * @author willian
 */
public interface CloudMessageService {
    
    public void createPersistentZNode(String node, String desc);
    
}
