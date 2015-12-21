/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.services.sched.model.ResourceList;
import br.unb.cic.bionimbus.services.sched.model.Resource;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbus.toSort.Listeners;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.mortbay.log.Log;

/**
 *
 * @author will O nome da classe é temporario, assim como sua localização
 *
 * Dados disponiveis atraves de metodos get
 */
public class RepositoryService extends AbstractBioService {

    public enum InstanceType {
        AMAZON_LARGE,
        PERSONAL,
        LABID_I7
    }

    @Inject
    public RepositoryService(final CloudMessageService cms) {
        this.cms = cms;
    }

    // TODO: deve haver uma classe basica contendo as informações de instancias
    // pergunta: qual é a nomeclatura para uma instancia de infra que não foi ativada e 
    //    qual é a nomeclatura para uma instancia ativa (executando algo)
//    public List<Instances> getInstancesList() {
//        // garante que a lista retornada pode ser a referencia atual, não precisando ser uma copia
//        return Collections.unmodifiableList(null);
//    }
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
        if(!cms.getZNodeExist(Path.NODE_SERVICE.getFullPath(serviceId), null)) {
            // problem: task not supported
            Log.warn("service_" + serviceId + " not suported");
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
            System.out.println("[RepositoryService] resource converted: " + r.toString());
            resources.resources.add(r);
        }

        return resources;
    }

    /**
     * Add a service to zookeeper, thereby, generating the full history
     * structure for given service.
     *
     * The service can have a history mode, and, having it, the modes will be
     * added to the service zookeeper file even without a history. These modes
     * will be removed when the history is big enough to make its own modes.
     *
     * The preset modes feature should only be used for testing.
     *
     * @param service Service to be added
     */
    public void addServiceToZookeeper(PluginService service) {
        // create father node
        cms.createZNode(CreateMode.PERSISTENT, Path.NODE_SERVICE.getFullPath(String.valueOf(service.getId())), service.toString());
        
        // create history structure
        cms.createZNode(CreateMode.PERSISTENT, Path.MODES.getFullPath(String.valueOf(service.getId())), null);

        // add preset mode if there is one
        if (service.getPresetMode() != null)
            cms.createZNode(CreateMode.PERSISTENT, Path.NODE_MODES.getFullPath(String.valueOf(service.getId()), "0"), service.getPresetMode().toString());
    }

    /**
     *
     *
     * @param resource Resource to be added
     */
    public void addPeerToZookeeper (PluginInfo resource) {
        cms.createZNode(CreateMode.PERSISTENT, Path.NODE_PEER.getFullPath(resource.getId()), resource.toString());
        cms.createZNode(CreateMode.PERSISTENT, Path.STATUS.getFullPath(resource.getId()), null);
        cms.createZNode(CreateMode.PERSISTENT, Path.SCHED.getFullPath(resource.getId()), null);
        cms.createZNode(CreateMode.PERSISTENT, Path.TASKS.getFullPath(resource.getId()), null);
    }

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
}
