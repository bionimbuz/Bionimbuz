/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
public class UpdatePeerData implements Watcher  {

    private final CloudMessageService cms;
    private final Service service;
    
    public UpdatePeerData(CloudMessageService cms, Service service) {
        this.cms = cms;
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
            switch(event.getType()){
            
                case NodeChildrenChanged:
                    if(cms.getZNodeExist(event.getPath(), false))
                        cms.getChildren(event.getPath(), this);
                break;
                case NodeDataChanged:
                    if(cms.getZNodeExist(event.getPath(), false))
                        cms.getData(event.getPath(), this);
                break;
            
            }
    } 
        
}
