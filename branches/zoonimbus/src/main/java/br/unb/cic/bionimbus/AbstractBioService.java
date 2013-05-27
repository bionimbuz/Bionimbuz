/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus;

import br.unb.cic.bionimbus.Service;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.storage.StorageService;
import br.unb.cic.bionimbus.zookeeper.ZooKeeperService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author biocloud1
 */
public abstract class AbstractBioService implements Service, P2PListener, Runnable {

    protected ZooKeeperService zkService;

}
