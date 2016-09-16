/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbus.services.sched.model.Resource;
import br.unb.cic.bionimbus.services.sched.model.ResourceList;
import br.unb.cic.bionimbus.toSort.Listeners;

/**
 *
 * @author will O nome da classe é temporario, assim como sua localização
 *
 * Dados disponiveis atraves de metodos get
 */
@Singleton
public final class RepositoryService extends AbstractBioService {
    private static Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);
    private static final String SERVICES_DIR = "services";
    private final List<PluginService> supportedServices = new ArrayList<>();
    private static final int PERIOD_HOURS=12;
    
    public enum InstanceType {

        AMAZON_LARGE,
        PERSONAL,
        LABID_I7
    }

    @Inject
    public RepositoryService(final CloudMessageService cms, BioNimbusConfig config) {
        this.cms = cms;

        LOGGER.info("Starting Repository Service...");
    }

    // TODO: deve haver uma classe basica contendo as informações de instancias
    // pergunta: qual é a nomeclatura para uma instancia de infra que não foi ativada e 
    //    qual é a nomeclatura para uma instancia ativa (executando algo)
    //    public List<Instances> getInstancesList() {
    //        // garante que a lista retornada pode ser a referencia atual, não precisando ser uma copia
    //        return Collections.unmodifiableList(null);
    //    }
    @Override
    public void run() {
        // this will be executed periodicaly
        System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
    }

    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {
        Preconditions.checkNotNull(listeners);
        this.config = config;
        this.listeners = listeners;

        // Add current instance as a peer
        addPeerToZookeeper(new PluginInfo(config.getId()));

        listeners.add(this);
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verifyPlugins() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void event(WatchedEvent eventType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Double average(List<String> ls) {
        double sum = 0;
        Integer count = 1;
        for (String s : ls) {
            sum += Double.parseDouble(cms.getData(Path.NODE_MODES.getFullPath(count.toString(), s), null));
            count++;
        }
        return sum / ls.size();
    }

    /**
     * Get instance cost from zookeeper cost
     *
     * MOCKED
     *
     * @param type type of instance (e.g. AMAZON_LARGE)
     * @return cost of the input type instance
     */
    public double getInstanceCost(InstanceType type) {
        switch (type) {
            case LABID_I7:
                return 0.35d;
            case PERSONAL:
                return 0.12d;
            default:
                return -1d;
        }
    }

    /**
     * Returns from zookeeper the list of modes of cycles necessary to execute a
     * given service List is updated lazily
     *
     * MOCKED
     *
     * @param serviceId id of requested service
     * @return
     */
    public Double getWorstExecution(String serviceId) {
        // check if service is supported
        if (!cms.getZNodeExist(Path.NODE_SERVICE.getFullPath(serviceId), null)) {

            // Problem: task not supported
            LOGGER.error("Service " + serviceId + " is not supported");

            return null;
        }

        // return modes average
        List<String> data = cms.getChildren(Path.MODES.getFullPath(serviceId), null);
        return average(data);
    }

    /**
     * Get all peers/instances from zookeeper and return them in the
     * ResourceList format Peers/instances will be fetched by the
     * AbstractBioService.getPeers() method
     *
     * @return
     */
    public ResourceList getCurrentResourceList() {
        ResourceList resources = new ResourceList();

        for (Map.Entry<String, PluginInfo> peer : getPeers().entrySet()) {
            Resource r = new Resource(peer.getValue().getId(),
                    peer.getValue().getFactoryFrequencyCore(),
                    peer.getValue().getCostPerHour());
            for (String taskId : cms.getChildren(Path.TASKS.getFullPath(peer.getValue().getId()), null)) {
                try {
                    PluginTask job = new ObjectMapper().readValue(cms.getData(Path.NODE_TASK.getFullPath(peer.getValue().getId(), taskId), null), PluginTask.class);
                    r.addTask(job.getJobInfo());
                } catch (IOException ex) {
                    LOGGER.error("[IOException] " + ex.getMessage());
                }
            }
            resources.resources.add(r);
        }

        return resources;
    }

    /**
     *
     *
     * @param resource Resource to be added
     */
    public void addPeerToZookeeper(PluginInfo resource) {
        cms.createZNode(CreateMode.PERSISTENT, Path.NODE_PEER.getFullPath(resource.getId()), resource.toString());
        cms.createZNode(CreateMode.EPHEMERAL, Path.STATUS.getFullPath(resource.getId()), null);
        cms.createZNode(CreateMode.PERSISTENT, Path.SCHED.getFullPath(resource.getId()), null);
        cms.createZNode(CreateMode.PERSISTENT, Path.TASKS.getFullPath(resource.getId()), null);
    }

    /**
     * Returns the list of BioNimbuZ supported services
     *
     * @return
     */
    public List<PluginService> getSupportedServices() {
        return this.supportedServices;
    }
}
