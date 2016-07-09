/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.controller;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import org.apache.zookeeper.WatchedEvent;

/**
 *  Interface that defines a controller
 *  @author Vinicius
 */
public interface Controller {

    public void start(BioNimbusConfig config);

    public void shutdown();

    public void getStatus();

    /**
     * Método para tratar os watchers disparados pelo zookeeper
     */
    public void verifyPlugins();

    public void event(WatchedEvent eventType);
}
