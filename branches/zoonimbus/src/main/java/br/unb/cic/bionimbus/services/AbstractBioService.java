/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;


import br.unb.cic.bionimbus.p2p.P2PListener;

/**
 * @author biocloud1
 */
public abstract class AbstractBioService implements Service, P2PListener, Runnable {

    protected ZooKeeperService zkService;

}
