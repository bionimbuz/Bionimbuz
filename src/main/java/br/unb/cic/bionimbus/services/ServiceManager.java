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
package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbus.toSort.Listeners;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ServiceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);

    private final Set<Service> services = new LinkedHashSet<Service>();

    private final CloudMessageService cms;

    private final RpcServer rpcServer;

    private final HttpServer httpServer;

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public ServiceManager(Set<Service> services, CloudMessageService cms, RpcServer rpcServer, HttpServer httpServer) {
        this.cms = cms;
        this.rpcServer = rpcServer;
        this.httpServer = httpServer;
        this.services.addAll(services);

        LOGGER.info("Initializing ServiceManager");
    }

    public void connectZK(String hosts) throws IOException, InterruptedException {
        cms.connect(hosts);
        LOGGER.info("Connected to ZooKeeper service on port 2181");
    }

    /**
     * Creates BioNimbuZ structure as ZooKeeper nodes
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void createZnodeZK() throws IOException, InterruptedException, KeeperException {
        //create root bionimbuz if does not exists
        if (!cms.getZNodeExist(Path.ROOT.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.ROOT.getFullPath(), "");
        }

        // create root peer node if does not exists
        if (!cms.getZNodeExist(Path.PEERS.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.PEERS.getFullPath(), "");
        }

        // add current instance as a peer
//        rs.addPeerToZookeeper(new PluginInfo(id));
        // create services repository node
        if (!cms.getZNodeExist(Path.SERVICES.getFullPath(), null)) {
            // create history root
            cms.createZNode(CreateMode.PERSISTENT, Path.SERVICES.getFullPath(), "");
        }

        // create finished tasks node if it doesn't exists
        if (!cms.getZNodeExist(Path.FINISHED_TASKS.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.FINISHED_TASKS.getFullPath(), "");
        }

        // Create /users
        if (!cms.getZNodeExist(Path.USERS.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.USERS.getFullPath(), "");
        }

        // Create /users/logged
        if (!cms.getZNodeExist(Path.USERS.getFullPath() + Path.LOGGED_USERS, null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.USERS.getFullPath() + Path.LOGGED_USERS, "");
        }
    }

    /**
     * Responsável pela limpeza do servidor a cada nova conexão onde o todos os
     * plug-ins havia ficado indisponíveis.
     */
    private void clearZookeeper() {

        if (cms.getZNodeExist(Path.ROOT.getFullPath(), null)) {
            cms.delete(Path.ROOT.getFullPath());
        }
//        if (cms.getZNodeExist(Path.PIPELINES.getFullPath(), null))
//            cms.delete(Path.PIPELINES.getFullPath());
//        if (cms.getZNodeExist(Path.PENDING_SAVE.getFullPath(), null))
//            cms.delete(Path.PENDING_SAVE.getFullPath());
//        if (cms.getZNodeExist(Path.PEERS.getFullPath(), null))
//            cms.delete(Path.PEERS.getFullPath());
//        if (cms.getZNodeExist(Path.SERVICES.getFullPath(), null))
//            cms.delete(Path.SERVICES.getFullPath());
//        if (cms.getZNodeExist(Path.FINISHED_TASKS.getFullPath(), null))
//            cms.delete(Path.FINISHED_TASKS.getFullPath());
    }

    public void register(Service service) {
        services.add(service);
    }

    public void startAll(BioNimbusConfig config, List<Listeners> listeners) {
        try {
            // Starts RPC server
            rpcServer.start();
            LOGGER.info("RPC Avro Server initialized on port 8080");

            // Starts HTTP server
            httpServer.start();
            LOGGER.info("HTTP Server initialized on port 8181");

            connectZK(config.getZkHosts());
            
            //limpando o servidor zookeeper caso não tenha peer on-line ao inciar servidor zooNimbus
            if (!config.isClient()) {
                clearZookeeper();
            }

            // Creates zookeeper structure
            createZnodeZK();

            for (Service service : services) {
                service.start(config, listeners);
            }

        } catch (Exception e) {
            LOGGER.error("[Exception] " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }

        LOGGER.info("All services are online");
    }
}
