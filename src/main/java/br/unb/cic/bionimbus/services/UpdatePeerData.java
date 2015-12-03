/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 *
 * @author gabriel
 */
public class UpdatePeerData implements Watcher {

    private final CloudMessageService cms;
    private final Service service;

    public UpdatePeerData(CloudMessageService cms, Service service) {
        this.cms = cms;
        this.service = service;
    }

    /**
     * Recebe as notificações de evento do zookeeper.
     *
     * @param event evento que identifica a mudança realizada no zookeeper
     */
    @Override
    public void process(WatchedEvent event) {
        //chamada para alertar servico que adicionou o watcher, tratar evento na service
//        System.out.println("[UpdatePeerData] event: " + event.toString());
        service.event(event);

        //Realiza a solicitação para um novo observer
        switch (event.getType()) {

            case NodeChildrenChanged:
                if (cms.getZNodeExist(event.getPath(), null)) {
                    cms.getChildren(event.getPath(), this);
                }
                break;
            case NodeDataChanged:
                if (cms.getZNodeExist(event.getPath(), null)) {
                    cms.getData(event.getPath(), this);
                }
                break;

        }
    }

}
