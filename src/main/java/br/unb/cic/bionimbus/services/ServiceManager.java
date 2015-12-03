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

    private final RepositoryService rs;

    private final RpcServer rpcServer;

    private final HttpServer httpServer;

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public ServiceManager(Set<Service> services, CloudMessageService cms, RepositoryService rs, RpcServer rpcServer, HttpServer httpServer) {
        this.cms = cms;
        this.rs = rs;
        this.rpcServer = rpcServer;
        this.httpServer = httpServer;

        this.services.addAll(services);
    }

    public void connectZK(String hosts) throws IOException, InterruptedException {
        cms.connect(hosts);
        LOGGER.info("Connected to ZooKeeper service on port 2181");
    }

    public void createZnodeZK(String id) throws IOException, InterruptedException, KeeperException {
        // create root peer node if does not exists
        if (!cms.getZNodeExist(Path.PEERS.toString(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.PEERS.toString(), "");
        }

        // add current instance as a peer
        cms.createZNode(CreateMode.PERSISTENT, Path.PREFIX_PEER.getFullPath(id), null);
        cms.createZNode(CreateMode.EPHEMERAL, Path.STATUS.getFullPath(id), null);

        // create services repository node
        if (!cms.getZNodeExist(Path.SERVICES.getFullPath(), null)) {
            // create history root
            cms.createZNode(CreateMode.PERSISTENT, Path.SERVICES.getFullPath(), "");
        }

        // create finished tasks node if it doesn't exists
        if (!cms.getZNodeExist(Path.FINISHED_TASKS.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.FINISHED_TASKS.getFullPath(), "");
        }
    }

    /**
     * Responsável pela limpeza do servidor a cada nova conexão onde o todos os
     * plug-ins havia ficado indisponíveis.
     */
    private void clearZookeeper() {

        if (cms.getZNodeExist(Path.PIPELINES.getFullPath(), null)) {
            cms.delete(Path.PIPELINES.getFullPath());
        }
        if (cms.getZNodeExist(Path.PENDING_SAVE.getFullPath(), null)) {
            cms.delete(Path.PENDING_SAVE.toString());
        }
        if (cms.getZNodeExist(Path.PEERS.getFullPath(), null)) {
            cms.delete(Path.PEERS.toString());
        }
        if (cms.getZNodeExist(Path.SERVICES.getFullPath(), null)) {
            cms.delete(Path.SERVICES.toString());
        }
        if (cms.getZNodeExist(Path.FINISHED_TASKS.getFullPath(), null)) {
            cms.delete(Path.FINISHED_TASKS.toString());
        }
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
            //limpando o servicor zookeeper caso não tenha peer on-line ao inciar servidor zooNimbus
            clearZookeeper();
            createZnodeZK(config.getId());

            for (Service service : services) {
                service.start(config, listeners);
            }

        } catch (Exception e) {
            LOGGER.error("[Exception] ServiceManager.startAll()");
            e.printStackTrace();
            System.exit(0);
        }

        LOGGER.info("All services are online");
    }
}
