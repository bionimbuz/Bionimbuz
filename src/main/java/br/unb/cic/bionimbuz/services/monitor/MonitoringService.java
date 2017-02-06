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
package br.unb.cic.bionimbuz.services.monitor;

import static br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path.STATUS;
import static br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path.STATUSWAITING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.model.SLA;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.plugin.PluginTaskState;
import br.unb.cic.bionimbuz.services.AbstractBioService;
import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.services.UpdatePeerData;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.toSort.Listeners;

@Singleton
public class MonitoringService extends AbstractBioService {

    private static Logger LOGGER = LoggerFactory.getLogger(MonitoringService.class);
    private final ScheduledExecutorService schedExecService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("MonitorService-%d").build());
    private final Map<String, PluginTask> waitingTask = new ConcurrentHashMap<>();
    private final List<String> waitingJobs = new ArrayList<>();
    private final List<String> waitingFiles = new ArrayList<>();
    private final Collection<String> plugins = new ArrayList<>();
    private static final List<User> users = Collections.synchronizedList(new LinkedList<User>());
    private static final int TIME_TO_RUN = 3;
    
    @Inject
    public MonitoringService(final CloudMessageService cms) {
        this.cms = cms;
    }

    @Override
    public void run() {
        LOGGER.error("[MonitoringService] Executing Monitoring");
        checkPeersStatus();
        checkPipelines();
        checkPendingSave();
        checkUsers();
    }

    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {
        try {
            checkPeers();
//            checkPendingSave();
        } catch (Exception e) {
           LOGGER.error("[MonitoringService] Exception checkPeers"+ e.getMessage()); 
        }
        this.config = config;
        this.listeners = listeners;
        if (listeners != null) {
            listeners.add(this);
        }
        schedExecService.scheduleAtFixedRate(this, 0, TIME_TO_RUN, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
        listeners.remove(this);
        schedExecService.shutdownNow();
    }


    @Override
    public void event(WatchedEvent eventType) {
        String path = eventType.getPath();
        try {
            switch (eventType.getType()) {
                case NodeCreated:
                    LOGGER.info(path + "= NodeCreated");
                    break;
                case NodeChildrenChanged:
                    if(eventType.getPath().equals(Path.PEERS.toString()))
                        if(plugins.size()<getPeers().size())
                            verifyPlugins();
                    LOGGER.info(path + "= NodeChildrenChanged");
                    break;
                case NodeDeleted:
                    String peerPath = path.subSequence(0, path.indexOf("STATUS") - 1).toString();
                    if (path.contains(Path.STATUSWAITING.toString()))
                        deletePeer(peerPath);
                    break;
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void verifyPlugins() {
        Collection<PluginInfo> temp = getPeers().values();
        temp.removeAll(plugins);
        for(PluginInfo plugin : temp){
            if(cms.getZNodeExist(Path.STATUS.getFullPath(plugin.getId()), null))
                cms.getData(Path.STATUS.getFullPath(plugin.getId()), new UpdatePeerData(cms, this,null));
        }
    }

    @Override
    public void getStatus() {
         // TODO
    }
    
    public static List<User> getZkUsers() {
        return users;
    }
    /**
     * Verifica se os pipelines que estava aguardando escalonamento e as tarefas
     * que já foram escalonadas ainda estão com o mesmo status da última
     * leitura.
     */
    private void checkPipelines() {
        try {
            // Need to be adapted to pipeline arch
//            for (String pipeline : cms.getChildren(Path.PIPELINES.toString(), null)) {
//                //verifica se o pipeline possue jobs que já estavam na lista, recupera e lança novamente os dados para disparar watchers
//                for (String job : cms.getChildren(Path.JOBS.getFullPath("", "", "", pipeline.substring(9)), null)) {
//                    if (waitingJobs.contains(job)) {
//                        String datas = cms.getData(Path.JOBS.toString() + SEPARATOR + job, null);
//                        // remove e cria task novamente para que os watchers sejam disparados e execute essa tarefa
//                        cms.delete(Path.JOBS.toString() + SEPARATOR + job);
//                        cms.createZNode(CreateMode.EPHEMERAL, Path.JOBS.toString() + SEPARATOR + job, datas);
//                        waitingJobs.remove(job);
//                    } else {
//                        waitingJobs.add(job);
//                    }
//                }
//            }

            for (PluginInfo peer : getPeers().values()) {
                for (String task : cms.getChildren(Path.TASKS.getFullPath(peer.getId()), null)) {
                    String datas =  cms.getData(Path.NODE_TASK.getFullPath(peer.getId(), task), null);
                    
                    if(datas!=null && datas.isEmpty()){
                        PluginTask pluginTask = new ObjectMapper().readValue(datas, PluginTask.class);
                        //verifica se o job já estava na lista, recupera e lança novamente os dados para disparar watchers                    if(count ==1){
                        if (pluginTask.getState() == PluginTaskState.PENDING) {
                            if (waitingTask.containsKey(task)) {
                                //condição para verificar se a tarefa está sendo utilizada
                                if(cms.getZNodeExist(Path.NODE_TASK.getFullPath(peer.getId(), task), null)){
                                    cms.delete(Path.NODE_TASK.getFullPath(peer.getId(), task));
                                    cms.createZNode(CreateMode.PERSISTENT, Path.NODE_TASK.getFullPath(peer.getId(), task), pluginTask.toString());
                                }
                                waitingJobs.remove(task);
                            } else {
                                waitingTask.put(task, pluginTask);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Realiza a verificação dos peers existentes identificando se existe algum
     * peer aguardando recuperação, se o peer estiver off-line e a recuperação
     * já estiver sido feito, realiza a limpeza do peer. Realizada apenas quando
     * o módulo inicia.
     */
    private void checkPeersStatus() {
        try {
            List<String> listPeers = cms.getChildren(Path.PEERS.getFullPath(), new UpdatePeerData(cms, this,null));
            for (String peerId : listPeers) {
//                if(!plugins.contains(peerPath)){
//                    plugins.add(peerPath);
//                    RpcClient rpcClient = new AvroClient("http", plugin.getHost().getAddress(), PORT);
//                    rpcClient.getProxy().setWatcher(plugin.getId());
//                    rpcClient.close();
                
                if (cms.getZNodeExist(Path.STATUSWAITING.getFullPath(peerId), null)) {
                    //TO DO descomentar linha abaixo caso o storage estiver fazendo a recuperação do peer
                    if (cms.getData(Path.STATUSWAITING.getFullPath(peerId), null).contains("S") && cms.getData(Path.STATUSWAITING.getFullPath(peerId), null).contains("E")) {
                        deletePeer(Path.NODE_PEER.getFullPath(peerId));
                    }
                }
//                }
            }
        } catch (KeeperException | InterruptedException ex) {
            java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Verifica se algum arquivo está pendente há algum tempo(duas vezes o tempo
     * de execução da monitoring), e se estiver apaga e cria novamente o arquivo
     * para que os seus watcher informem sua existência.
     */
    private void checkPendingSave() {
        try {
            List<String> listPendingSaves= cms.getChildren(Path.PENDING_SAVE.getFullPath(), null);
            if(listPendingSaves!=null && !listPendingSaves.isEmpty()){
                for (String filePending : listPendingSaves) {
                    String datas =  cms.getData(Path.NODE_PENDING_FILE.getFullPath(filePending), null);
                    if(datas!=null && datas.isEmpty()){
                        //verifica se o arquivo já estava na lista, recupera e lança novamente os dados para disparar watchers
                        if (waitingFiles.contains(filePending)) {
                            PluginInfo pluginInfo = new ObjectMapper().readValue(datas, PluginInfo.class);
                            //condição para verificar se arquivo na pending ainda existe
                            if(cms.getZNodeExist(Path.PENDING_SAVE.getFullPath(filePending), null)){
                                cms.delete(Path.PENDING_SAVE.getFullPath(filePending));
                                cms.createZNode(CreateMode.PERSISTENT, Path.PENDING_SAVE.getFullPath(filePending), pluginInfo.toString());
                            }
                            waitingFiles.remove(filePending);
                        } else {
                            waitingFiles.add(filePending);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    /**
     * Incia o processo de recuperação dos peers caso ainda não tenho sido
     * iniciado e adiciona um watcher nos peer on-lines.
     */
    private void checkPeers() {
        try {
            //executa a verificação inicial para ver se os peers estão on-line, adiciona um watcher para avisar quando o peer ficar off-line
            List<String> listPeers = cms.getChildren(Path.PEERS.getFullPath(), null);
            for (String peerId : listPeers) {
                if (cms.getZNodeExist(Path.STATUS.getFullPath(peerId), null)) {
                    //adicionando wacth
                    cms.getData(Path.STATUS.getFullPath(peerId), new UpdatePeerData(cms, this,null));
                }
                //verifica se algum plugin havia ficado off e não foi realizado sua recuperação
                if (!cms.getZNodeExist(Path.STATUS.getFullPath(peerId), null)
                        && !cms.getZNodeExist(Path.STATUSWAITING.getFullPath(peerId), null)) {
                    cms.createZNode(CreateMode.PERSISTENT, Path.STATUSWAITING.getFullPath(peerId), "");
                    cms.getData(Path.STATUSWAITING.getFullPath(peerId), new UpdatePeerData(cms, this,null));
                }
                plugins.add(peerId);
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MonitoringService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns the list of BionimbuZ Users, with workflows, slas and instances
     *
     * @return
     */
    private void checkUsers() {
        List<String> usersLogins = new ArrayList<>();
        List<String> workflowsUsersIds = new ArrayList<>();
        List<String> instancesIPs = new ArrayList<>();
        final List<Workflow> workflowsUser = new ArrayList<>();
        final List<Instance> instances = new ArrayList<>();
        try {
            usersLogins = this.cms.getChildren(Path.USERS_INFO.getFullPath(), new UpdatePeerData(this.cms, this, null));
            // this.removeUnusedUsers(usersLogins);
            for (final String userId : usersLogins) {
                final User user = new ObjectMapper().readValue(this.cms.getData(Path.NODE_USERS.getFullPath(userId), null), User.class);
                workflowsUsersIds = this.cms.getChildren(Path.WORKFLOWS_USER.getFullPath(userId), null);
                for (final String workflowUserId : workflowsUsersIds) {
                    final Workflow workflowUser = new ObjectMapper().readValue(this.cms.getData(Path.NODE_WORFLOW_USER.getFullPath(userId, workflowUserId), null), Workflow.class);
                    final SLA sla = new ObjectMapper().readValue(this.cms.getData(Path.SLA_USER.getFullPath(userId, workflowUserId), null), SLA.class);
                    workflowUser.setSla(sla);
                    workflowsUser.add(workflowUser);
                    instancesIPs = this.cms.getChildren(Path.INSTANCES_USER.getFullPath(userId, workflowUserId), null);
                    for (final String InstanceIpUser : instancesIPs) {
                        final Instance instanceUser = new ObjectMapper().readValue(this.cms.getData(Path.NODE_INSTANCE_USER.getFullPath(userId, workflowUserId, InstanceIpUser), null), Instance.class);
                        instances.add(instanceUser);
                    }
                }
                user.setInstances(instances);
                user.setWorkflows(workflowsUser);
                this.users.add(user);
            }
        } catch (final IOException ex) {
            java.util.logging.Logger.getLogger(RepositoryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    private void removeUnusedUsers(List<String> usersLogins) {
//        for (User user : this.users) {
//            if (!usersLogins.contains(user.getLogin())) {
//                this.users.remove(user);
//            }
//        }
//    }
    
    private void deletePeer(String peerPath) throws InterruptedException, KeeperException {
        if (!cms.getZNodeExist(peerPath + STATUS, null) && cms.getZNodeExist(peerPath + STATUSWAITING, null)) {
            cms.delete(peerPath);
        }
    }
}
