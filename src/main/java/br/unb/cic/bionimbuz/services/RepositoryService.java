/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services;

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

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.model.SLA;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginService;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.services.monitor.MonitoringService;
import br.unb.cic.bionimbuz.services.sched.model.Resource;
import br.unb.cic.bionimbuz.services.sched.model.ResourceList;
import br.unb.cic.bionimbuz.toSort.Listeners;

/**
 *
 * @author will O nome da classe é temporario, assim como sua localização Dados
 *         disponiveis atraves de metodos get
 */
@Singleton
public final class RepositoryService extends AbstractBioService {
    
    private static Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);
    private static final String SERVICES_DIR = "services";
    private final List<PluginService> supportedServices = new ArrayList<>();
    private static final int PERIOD_HOURS = 12;
    
    private static RepositoryService INSTANCE;
    
    public enum InstanceType {
        
        AMAZON_LARGE,
        PERSONAL,
        LABID_I7
    }
    
    @Inject
    public RepositoryService(final CloudMessageService cms) {
        this.cms = cms;
        
        LOGGER.info("Starting Repository Service...");
        INSTANCE = this;
    }
    
    public static synchronized RepositoryService getInstance() {
        return INSTANCE;
    }
    
    // TODO: deve haver uma classe basica contendo as informações de instancias
    // pergunta: qual é a nomeclatura para uma instancia de infra que não foi ativada e
    // qual é a nomeclatura para uma instancia ativa (executando algo)
    // A classe Instance é a instancia não ativada, a classe Pluginfo é a instancia ativada;
    // Uma ta localizada no zookeeper no seguinte endereço
    /// bionimbuz/users/user_info/userid/workflows_user/workflowUserId/instances_user/instances_userId/
    // A outra ta em /bionimbuz/peers/
    // public List<Instances> getInstancesList() {
    // // garante que a lista retornada pode ser a referencia atual, não precisando ser uma copia
    // return Collections.unmodifiableList(null);
    // }
    @Override
    public void run() {
        // this will be executed periodicaly
        LOGGER.info("[RepositoryService] " + Arrays.toString(Thread.currentThread().getStackTrace()));
    }
    
    @Override
    public void start(List<Listeners> listeners) {
        Preconditions.checkNotNull(listeners);
        this.listeners = listeners;
        
        // Add current instance as a peer HERE THE PEER IS ADD IN ZOOKEEPER
        this.addPeerToZookeeper(new PluginInfo(BioNimbusConfig.get().getId()));
        
        listeners.add(this);
    }
    
    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void verifyPlugins() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void event(WatchedEvent eventType) {
        switch (eventType.getType()) {
            
            case NodeChildrenChanged:
                if (eventType.getPath().equals(Path.USERS_INFO.getFullPath())) {
                    LOGGER.info("Imprimir");
                }
                break;
            case NodeDeleted:
                break;
        }
    }
    
    private Double average(List<String> ls) {
        double sum = 0;
        Integer count = 1;
        for (final String s : ls) {
            sum += Double.parseDouble(this.cms.getData(Path.NODE_MODES.getFullPath(count.toString(), s), null));
            count++;
        }
        return sum / ls.size();
    }
    
    /**
     * Get instance cost from zookeeper cost
     *
     * MOCKED
     *
     * @param type
     *            type of instance (e.g. AMAZON_LARGE)
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
     * @param serviceId
     *            id of requested service
     * @return
     */
    public Double getWorstExecution(String serviceId) {
        // check if service is supported
        if (!this.cms.getZNodeExist(Path.NODE_SERVICE.getFullPath(serviceId), null)) {
            
            // Problem: task not supported
            LOGGER.error("Service " + serviceId + " is not supported");
            
            return null;
        }
        
        // return modes average
        final List<String> data = this.cms.getChildren(Path.MODES.getFullPath(serviceId), null);
        return this.average(data);
    }
    
    /**
     * Get all peers/instances from zookeeper and return them in the
     * ResourceList format Peers/instances will be fetched by the
     * AbstractBioService.getPeers() method
     *
     * @return
     */
    public ResourceList getCurrentResourceList() {
        final ResourceList resources = new ResourceList();
        
        for (final Map.Entry<String, PluginInfo> peer : this.getPeers().entrySet()) {
            final Resource r = new Resource(peer.getValue().getId(), peer.getValue().getFactoryFrequencyCore(), peer.getValue().getCostPerHour());
            for (final String taskId : this.cms.getChildren(Path.TASKS.getFullPath(peer.getValue().getId()), null)) {
                try {
                    final PluginTask job = new ObjectMapper().readValue(this.cms.getData(Path.NODE_TASK.getFullPath(peer.getValue().getId(), taskId), null), PluginTask.class);
                    r.addTask(job.getJobInfo());
                } catch (final IOException ex) {
                    LOGGER.error("[IOException] " + ex.getMessage());
                }
            }
            resources.resources.add(r);
        }
        
        return resources;
    }
    
    /**
     * Returns the list of BionimbuZ Slas Users
     *
     * @param login
     * @return SlaList
     */
    public List<SLA> getSlasUserByUserLogin(String login) {
        
        User user = new User(login);
        final List<User> zkUsers = MonitoringService.getZkUsers();
        final List<SLA> slasUser = new ArrayList<>();
        if (zkUsers.contains(user)) {
            final int indexOf = zkUsers.indexOf(user);
            user = zkUsers.get(indexOf);
            for (final Workflow work : user.getWorkflows()) {
                slasUser.add(work.getSla());
            }
        }
        return slasUser;
    }
    /**
     * Returns the list of BionimbuZ Slas Users
     *
     * @param workflowId
     * @return SlaList
     */
    public SLA getSlaUserByWorkflowId(String workflowId) {
        for (final User u : MonitoringService.getZkUsers()) {
            for (final Workflow work : u.getWorkflows()) {
                if (work.getId().equals(workflowId)) {
                    
                    return work.getSla();
                }
            }
        }
        return null;
    }
    
    /**
     * @param resource
     *            Resource to be added
     */
    public void addPeerToZookeeper(PluginInfo resource) {
        this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_PEER.getFullPath(resource.getId()), resource.toString());
        this.cms.createZNode(CreateMode.EPHEMERAL, Path.STATUS.getFullPath(resource.getId()), null);
        this.cms.createZNode(CreateMode.PERSISTENT, Path.SCHED.getFullPath(resource.getId()), null);
        this.cms.createZNode(CreateMode.PERSISTENT, Path.TASKS.getFullPath(resource.getId()), null);
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
