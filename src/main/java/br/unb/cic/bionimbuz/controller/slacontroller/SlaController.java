/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.controller.slacontroller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import br.unb.cic.bionimbuz.controller.Controller;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.AmazonAPI;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.GoogleAPI;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.model.Log;
import br.unb.cic.bionimbuz.model.LogSeverity;
import br.unb.cic.bionimbuz.model.Prediction;
import br.unb.cic.bionimbuz.model.SLA;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.services.monitor.MonitoringService;

/**
 *
 * @author zoonimbus
 */
public class SlaController implements Controller, Runnable {

    private static final int TIME_TO_RUN = 1;
    private static final long ONE_HOUR_MILLES = 36 * 100000;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final WorkflowLoggerDao loggerDao;
    private final ScheduledExecutorService threadExecutor = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("SlaController-%d").build());
    protected CloudMessageService cms;
    private final RepositoryService rs;

    /**
     * Starts SlaController execution
     *
     * @param cms
     * @param rs
     */
    @Inject
    public SlaController(CloudMessageService cms, RepositoryService rs) {
        Preconditions.checkNotNull(cms);
        Preconditions.checkNotNull(rs);
        this.cms = cms;
        this.rs = rs;
        this.loggerDao = new WorkflowLoggerDao();
    }

    @Override
    public void start() {
        LOGGER.info("SLAController started");
        this.threadExecutor.scheduleAtFixedRate(this, 0, TIME_TO_RUN, TimeUnit.MINUTES);
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
        LOGGER.info("DISPARADOOOOOOOOOOOOOOOOOOOOOO");
        switch (eventType.getType()) {

            case NodeChildrenChanged:
                if (eventType.getPath().equals(Path.USERS_INFO.getFullPath())) {
                    LOGGER.info("Imprimir");

                    // for (User u : MonitoringService.getZkUsers()) {
                    // LOGGER.info("User: " + u.toString());
                    // for (Workflow work : u.getWorkflows()) {
                    // LOGGER.info("Workflow: " + work.toString());
                    // }
                    // }
                }
                break;
            case NodeDeleted:
                break;
            case NodeCreated:
                break;
            case NodeDataChanged:
                break;
            case None:
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        LOGGER.info("[SlaController] Checking SLA users: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(System.currentTimeMillis())));
        this.checkSla();
        // if(!repositoryService.getUsers().isEmpty()){
        // users=repositoryService.getUsers();
        // }
    }

    /**
     * Check all the SLA from all users
     */
    private void checkSla() {
        try {
            //
            for (final User u : MonitoringService.getZkUsers()) {
                boolean deleteZkWorkflow = false;
                for (final Workflow work : u.getWorkflows()) {
                    final Double toleranceCost = work.getSla().getExeceedValueExecutionCost();
                    final Double limitCost = work.getSla().getLimitationValueExecutionCost();
                    final Long limitTime = work.getSla().getLimitationValueExecutionTime();
                    double workflowCostPerHour = 0d;
                    double currentCost = 0D;
                    long period = System.currentTimeMillis();
                    final int VARIANCE = 60 * 1000 * TIME_TO_RUN;
                    // calcula o gasto atual
                    if (limitCost != null || limitTime != null) {
                        for (final Instance i : work.getIntancesWorkflow()) {
                            workflowCostPerHour += i.getCostPerHour();
                            currentCost += (System.currentTimeMillis() + VARIANCE - i.getCreationTimer()) * i.getCostPerHour();
                            if (i.getCreationTimer() < period) {
                                period = i.getCreationTimer();
                            }
                        }
                        LOGGER.info("[SlaController] Checking SLA user: " + u.getNome() + " Workflow: " + work.getId());
                        // Se aceitou a predição na montagem do workflow
                        if (work.getSla().getPrediction()) {
                            // System.out.println("Verificar os tempos, atribuidos aos serviços");
                            for (final Prediction pred : work.getSla().getSolutions()) {
                                for (final Instance i : work.getIntancesWorkflow()) {
                                    // Verifica se o ip da instancia é o mesmo da solucao dada pela predicao
                                    if (pred.getInstance().getIp().equals(i.getId())) {
                                        // Se for verifica se o tempo previsto foi extrapolado
                                        if (System.currentTimeMillis() - i.getCreationTimer() > pred.getTimeService()) {
                                            // se tiver estrapolado tem que mandar um alerta ainda nao definido
                                            LOGGER.info("[SlaController] Execution time service has been hitted");
                                            this.loggerDao.log(new Log(
                                                    "Tempo limite da instância: " + i.getIp() + " previsto passou, tempo previsto: "
                                                            + new SimpleDateFormat("HH:mm:ss").format(new Date(pred.getTimeService())) + " tempo atual: "
                                                            + new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis() - i.getCreationTimer())),
                                                    work.getUserId(), work.getId(), LogSeverity.WARN));
                                            // deleteInstances(i.getProvider(), i.getIp());
                                        }
                                        if ((System.currentTimeMillis() - i.getCreationTimer()) * i.getCostPerHour() > pred.getCustoService()) {
                                            // se tiver estrapolado tem que mandar um alerta ainda nao definido
                                            LOGGER.info("[SlaController] Execution cust service has been hitted");
                                            this.loggerDao.log(new Log("Custo limite da instância: " + i.getIp() + " previsto passou, custo previsto:$ " + pred.getCustoService() + " Custo: "
                                                    + (System.currentTimeMillis() - i.getCreationTimer()) * i.getCostPerHour(), work.getUserId(), work.getId(), LogSeverity.WARN));
                                            // deleteInstances(i.getProvider(), i.getIp());
                                        }
                                    }
                                }
                            }
                        } else {
                            if (work.getSla().getLimitationExecution()) {
                                // se passou o limite e o usuario esta disposto a pagar um pouco mais para finalizar o workflow
                                if (limitTime != null && System.currentTimeMillis() - period > limitTime - VARIANCE) {
                                    final long toleranceTime = (long) (toleranceCost / workflowCostPerHour * ONE_HOUR_MILLES);
                                    if (System.currentTimeMillis() - period > toleranceTime + limitTime - VARIANCE) {
                                        this.deleteInstances(work.getIntancesWorkflow());
                                        deleteZkWorkflow = true;
                                        final String message = String.format("O tempo limite de execução (%s horas) do Workflow Id: %s foi excedido em %s.", limitTime, work.getId(),
                                                System.currentTimeMillis() - (toleranceTime + limitTime));
                                        this.loggerDao.log(new Log(message, work.getUserId(), work.getId(), LogSeverity.WARN));
                                        LOGGER.debug("[SlaController] Limitating execution time has been hitted, Removing workflow: " + work.getId() + " from User: " + u.getNome());
                                    }
                                }
                            }
                        }
                        // TODO tem que modificar para verificar se o workflow
                        // terminou pegando dos peers;
                        if (limitCost != null && limitCost > 0 && currentCost > toleranceCost + limitCost) {
                            // terminate instances by elasticity controller
                            this.deleteInstances(work.getIntancesWorkflow());
                            deleteZkWorkflow = true;
                            final String message = String.format("O custo limite de execução (U$ %s) do Workflow Id: %s foi excedido em %s. Workflow interrompido.", limitCost + toleranceCost,
                                    work.getId(), currentCost - (limitCost + toleranceCost));
                            this.loggerDao.log(new Log(message, work.getUserId(), work.getId(), LogSeverity.WARN));
                            LOGGER.debug("[SlaController] Limitating execution cost has been hitted, Removing workflow: " + work.getId() + " from User: " + u.getNome());
                        }
                    }
                    // deleta do zookeeper o workflow que teve suas maquinas excluidas
                    if (deleteZkWorkflow) {
                        this.cms.delete(Path.NODE_WORFLOW_USER.getFullPath(u.getLogin(), work.getId()));
                        u.getWorkflows().remove(work);
                        deleteZkWorkflow = false;
                    }
                }
            }
        } catch (final Exception ex) {
            LOGGER.debug(ex.getMessage());
        }
    }

    /**
     * Sobrecarga para deletar todas as instancias do workflow e os peers
     * correspondentes do zookeeper
     *
     * @param list
     */
    private void deleteInstances(List<Instance> list) {
        for (final Instance element : list) {
            this.deleteInstances(element.getProvider(), element.getIp());
        }
    }

    /**
     * Chama o metodo das api da amazon ou da google para deletar a instancia
     * com o ip
     *
     * @param provider
     * @param ip
     */
    private void deleteInstances(String provider, String ip) {
        switch (provider) {
            case "Amazon": {
                final AmazonAPI amazonapi = new AmazonAPI();
                // amazonapi.createinstance(type, nameInstance);
                amazonapi.terminate(ip);
                break;
            }
            case "Google": {
                final GoogleAPI googleapi = new GoogleAPI();
                // amazonapi.createinstance(type, nameInstance);
                googleapi.terminate(ip);
            }
            default:
                break;
        }
    }
    /**
     * TODO fazer a verificação do jobs nos peers
     *
     * @param wokflow
     */
    // public void verificaJobs(Workflow wokflow){
    // for(final PluginInfo peer : this.rs.getPeers().values()){
    // for (final String taskId : this.cms.getChildren(Path.TASKS.getFullPath(peer.getId()), null)) {
    // Job jobp = new Job();
    // try {
    // final PluginTask plugintask = new ObjectMapper().readValue(this.cms.getData(Path.NODE_TASK.getFullPath(peer.getId(), taskId), null), PluginTask.class);
    // jobp= plugintask.getJobInfo();
    // } catch (IOException ex) {
    // java.util.logging.Logger.getLogger(SlaController.class.getName()).log(Level.SEVERE, null, ex);
    // }
    // for(Job job : wokflow.getJobs()){
    // if (job.getId().equals(jobp.getId())){
    // if(jobp.getTimestamp()){
    // }
    // }
    // }
    // }
    // }
    // }

    /**
     * verifica se as instancias criadas pelos servidores são as mesmas das especificações
     *
     * @param instancesUser
     * @param userId
     * @param worflowId
     */
    public void compareHardware(List<Instance> instancesUser, Long userId, String worflowId) {

        for (final Instance iUser : instancesUser) {
            this.loggerDao.log(new Log("Máquina virtual criada...Ip: " + iUser.getIp() + " Provedor: " + iUser.getProvider(), userId, worflowId, LogSeverity.INFO));
            for (final PluginInfo peer : this.rs.getPeers().values()) {
                if (iUser.getIp().equals(peer.getHost().getAddress())) {
                    if (!peer.getNumCores().equals(iUser.getNumCores())) {
                        this.loggerDao.log(new Log("Instância: " + iUser.getIp() + " não corresponde as especificações de numeros de cores esperados: " + iUser.getNumCores()
                                + " número de cores da instância: " + peer.getNumCores(), userId, worflowId, LogSeverity.WARN));
                    }
                    if (((peer.getFactoryFrequencyCore()) / 1000000000.0D) != iUser.getCpuHtz()) {
                        this.loggerDao.log(new Log("Instância: " + iUser.getIp() + " não corresponde as especificações da frequência de clock esperada: " + iUser.getCpuHtz()
                                + "GHZ frequência de clock da instância: " + peer.getFactoryFrequencyCore() + "GHZ", userId, worflowId, LogSeverity.WARN));
                    }
                    if (!peer.getMemoryTotal().equals(iUser.getMemoryTotal())) {
                        this.loggerDao.log(new Log("Instância: " + iUser.getIp() + " não corresponde as especificações de quantidade de memoria esperada: " + iUser.getMemoryTotal()
                                + "GB, quantidade de memória da instância: " + peer.getMemoryTotal() + " GB", userId, worflowId, LogSeverity.WARN));
                    }
                }
            }
        }
    }

    public void startSla(SLA sla, Workflow workflow) {

    }

    public void createSlaTemplate(SLA sla, Workflow workflow) {

    }

}
