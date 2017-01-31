/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.controller.slacontroller;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.Controller;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.AmazonAPI;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.ElasticityController;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.GoogleAPI;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.model.Job;
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
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zoonimbus
 */
public class SlaController implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final WorkflowLoggerDao loggerDao;
    private final ScheduledExecutorService threadExecutor = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("SlaController-%d").build());
    protected CloudMessageService cms;
    protected BioNimbusConfig config;
    private final RepositoryService repositoryService;
    private final List<SLA> slas = Collections.synchronizedList(new ArrayList());
    private static final int LIMITATIONEXECUTIONTIME = 0;
    private static final int LIMITATIONEXECUTIONCUST = 1;

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
        this.repositoryService = rs;
        this.cms = cms;
        loggerDao = new WorkflowLoggerDao();
    }

    @Override
    public void start(BioNimbusConfig config) {
        LOGGER.info("SLAController started");
        threadExecutor.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
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
        LOGGER.info("DISPARADOOOOOOOOOOOOOOOOOOOOOO");
        switch (eventType.getType()) {

            case NodeChildrenChanged:
                if (eventType.getPath().equals(Path.USERS_INFO.getFullPath())) {
                    LOGGER.info("Imprimir");

                    for (User u : repositoryService.getUsers()) {
                        LOGGER.info("User: " + u.toString());
                        for (Workflow work : u.getWorkflows()) {
                            LOGGER.info("Workflow: " + work.toString());
                        }
                    }
                }
                break;
            case NodeDeleted:
                break;
        }
    }

    @Override
    public void run() {
        LOGGER.info("[SlaController] Checking SLA users: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(System.currentTimeMillis())));
        try {
            if (repositoryService != null) {
                for (User u : repositoryService.getUsers()) {
                    LOGGER.info("[SlaController] repositoryService not null");
                    for (Workflow work : u.getWorkflows()) {
                        //verifica se as instancias criadas pelos servidores são as mesmas das especificações
                        compareHardware(work.getIntancesWorkflow(), work.getUserId(), work.getId());
                        Double valueWorkflow = 0D;
                        Long period = 0L;
                        
                        //calcula o gasto atual
                        if(work.getSla().getLimitationValueExecutionCost()!=null ||work.getSla().getLimitationValueExecutionTime()!=null ){
                            for (Instance i : u.getInstances()) {
                                valueWorkflow += (System.currentTimeMillis() - i.getCreationTimer()) * i.getCostPerHour();
                                if (i.getCreationTimer() > period) {
                                    period = i.getCreationTimer();
                                }
                            }
                            //se passou o limite e o usuario esta disposto a pagar um pouco mais para finalizar o workflow
                            if ((valueWorkflow.equals(work.getSla().getLimitationValueExecutionCost()))
                                    || ((System.currentTimeMillis() - period) == work.getSla().getLimitationValueExecutionTime())
                                    && work.getSla().getExeceedValueExecutionCost() > 0D) {
                                valueWorkflow = valueWorkflow + work.getSla().getExeceedValueExecutionCost();
                            }
                        }    
                        //Se aceitou a predição na montagem do workflow
                        if (work.getSla().getPrediction()) {
                            System.out.println("Verificar os tempos, atribuidos aos serviços");
                            for (Prediction pred : work.getSla().getSolutions()) {
                                for (Instance i : u.getInstances()) {
                                    //Verifica se o ip da instancia é o mesmo da solucao dada pela predicao
                                    if (pred.getInstance().getIp().equals(i.getId())) {
                                        //Se for verifica se o tempo previsto foi extrapolado
                                        if ((System.currentTimeMillis() - i.getCreationTimer()) == pred.getTimeService()) {
                                            //se tiver estrapolado tem que mandar um alerta ainda nao definido
                                            LOGGER.info("[SlaController] Execution time service has been hitted");
                                            loggerDao.log(new Log("Tempo limite da instância: " + i.getIp()
                                                    + " previsto passou, tempo previsto: "
                                                    + new SimpleDateFormat("HH:mm:ss").format(new Date(pred.getTimeService()))
                                                    + " tempo atual: "
                                                    + new SimpleDateFormat("HH:mm:ss").format(new Date((System.currentTimeMillis() - i.getCreationTimer()))) ,
                                                    work.getUserId(), work.getId(), LogSeverity.WARN));
//                                            deleteInstances(i.getProvider(), i.getIp());
                                        }
                                        if (((System.currentTimeMillis() - i.getCreationTimer()) * i.getCostPerHour()) == pred.getCustoService()) {
                                            //se tiver estrapolado tem que mandar um alerta ainda nao definido
                                            LOGGER.info("[SlaController] Execution cust service has been hitted");
                                            loggerDao.log(new Log("Custo limite da instância: " + i.getIp()
                                                    + " previsto passou, custo previsto:$ " + pred.getCustoService()
                                                    + " Custo: " + ((System.currentTimeMillis() - i.getCreationTimer()) * i.getCostPerHour()),
                                                    work.getUserId(), work.getId(), LogSeverity.WARN));
//                                            deleteInstances(i.getProvider(), i.getIp());
                                        }
                                    }
                                }
                            }
                        } else {
                            if (work.getSla().getLimitationExecution()) {
                                //0 = time
                                if (work.getSla().getLimitationType() == LIMITATIONEXECUTIONTIME) {
                                    System.out.println("Parar quando chegar nesse tempo");
                                    work.getSla().getLimitationValueExecutionTime();
                                    Double valorCorrente = 0D;
                                    for (Instance i : u.getInstances()) {
                                        valorCorrente = valorCorrente + (System.currentTimeMillis() - i.getCreationTimer()) * i.getCostPerHour();
                                        if ((System.currentTimeMillis() - i.getCreationTimer()) == work.getSla().getLimitationValueExecutionTime()
                                                && valorCorrente.equals(valorCorrente + work.getSla().getExeceedValueExecutionCost())) {

                                            loggerDao.log(new Log("Tempo limite da instância: " + i.getIp()
                                                    + " atingiu a limitação, tempo limitado a: "
                                                    + new SimpleDateFormat("HH:mm:ss").format(new Date(work.getSla().getLimitationValueExecutionTime()))
                                                    + " tempo atual: "
                                                    + new SimpleDateFormat("HH:mm:ss").format(new Date((System.currentTimeMillis() - i.getCreationTimer()))) + " deletando instância.",
                                                    work.getUserId(), work.getId(), LogSeverity.WARN));
                                            //terminate instances by elasticity controller
                                            LOGGER.info("[SlaController] Limitating execution time has been hitted, Removing instance: " + i.getType() + " from User: " + u.getNome());
                                            deleteInstances(i.getProvider(), i.getIp());
                                        }
                                    }
                                } else {
                                    //1= cust
                                    if (work.getSla().getLimitationType() == LIMITATIONEXECUTIONCUST) {
                                        System.out.println("Parar quando atingir esse valor");
                                        work.getSla().getLimitationValueExecutionCost();

                                        //If have exceed cust case the workflow not done
                                        for (Instance i : u.getInstances()) {
                                            if (valueWorkflow.equals(work.getSla().getLimitationValueExecutionCost())) {
                                                loggerDao.log(new Log("Custo limite da instância: " + i.getIp()
                                                        + " atingiu a limitação, custo previsto: $" + work.getSla().getLimitationValueExecutionCost()
                                                        + " Custo: $" + ((System.currentTimeMillis() - i.getCreationTimer()) * i.getCostPerHour()) + " deletando instância.",
                                                        work.getUserId(), work.getId(), LogSeverity.WARN));
                                                //terminate instances by elasticity controller
                                                LOGGER.info("[SlaController] Limitating cust value has been hitted, Removing instance: " + i.getType() + " from User: " + u.getNome());
                                                deleteInstances(i.getProvider(), i.getIp());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.debug(ex.getMessage());
        }
//        if(!repositoryService.getUsers().isEmpty()){
//            users=repositoryService.getUsers();
//        }
    }

    public void deleteInstances(String provider, String ip) {
        AmazonAPI amazonapi = new AmazonAPI();
        GoogleAPI googleapi = new GoogleAPI();
        switch (provider) {
            case "Amazon": {
                //amazonapi.createinstance(type, nameInstance);
                amazonapi.terminate(ip);
                break;
            }
            case "Google": {
                //amazonapi.createinstance(type, nameInstance);
                googleapi.terminate(ip);
            }
        }
    }

    public void compareHardware(List<Instance> instancesUser, Long userId, String worflowId) {
        for (Instance iUser : instancesUser) {
            for (PluginInfo peer : repositoryService.getPeers().values()) {
                if (iUser.getIp().equals(peer.getHost().getAddress())) {
                    if (!peer.getNumCores().equals(iUser.getNumCores())) {

                        loggerDao.log(new Log("Instância: " + iUser.getIp()
                                + " não corresponde as especificações de numeros de cores esperados: "
                                + iUser.getNumCores()
                                + " número de cores da instância: "
                                + peer.getNumCores(),
                                userId, worflowId, LogSeverity.WARN));
                    }
                    if (!peer.getFactoryFrequencyCore().equals(iUser.getCpuHtz())) {
                        loggerDao.log(new Log("Instância: " + iUser.getIp()
                                + " não corresponde as especificações da frequência de clock esperada: "
                                + iUser.getCpuHtz()
                                + "GHZ frequência de clock da instância: "
                                + peer.getFactoryFrequencyCore() + "GHZ",
                                userId, worflowId, LogSeverity.WARN));
                    }
                    if (!peer.getMemoryTotal().equals(iUser.getMemoryTotal())) {
                        loggerDao.log(new Log("Instância: " + iUser.getIp()
                                + " não corresponde as especificações de quantidade de memoria esperada: "
                                + iUser.getMemoryTotal()
                                + "GB, quantidade de memória da instância: "
                                + peer.getMemoryTotal() + " GB",
                                userId, worflowId, LogSeverity.WARN));
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
