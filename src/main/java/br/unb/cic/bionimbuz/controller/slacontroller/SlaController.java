/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.controller.slacontroller;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.Controller;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.model.SLA;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.WatchedEvent;
import org.eclipse.jetty.util.ConcurrentArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zoonimbus
 */
public class SlaController  implements Controller, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final ScheduledExecutorService threadExecutor = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
                    .namingPattern("SlaController-%d").build());
    protected CloudMessageService cms;
    protected BioNimbusConfig config;
    private final RepositoryService repositoryService;
    private List<User> users = Collections.synchronizedList(new ArrayList());
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
        this.repositoryService = rs;
        this.cms = cms;

        LOGGER.info("SLAController started");
    }

    @Override
    public void start(BioNimbusConfig config) {
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
                    users=repositoryService.getUsers();
                    for(User u : users){
                      LOGGER.info("User: "+u.toString());
                        for(Workflow work : u.getWorkflows()){
                             LOGGER.info("Workflow: "+ work.toString());
                        }
                    }
                    // chamada para checar a pending_save apenas quando uma alerta para ela for lançado
                    // try{
                    // checkingPendingSave();
                    // }
                    // catch (IOException ex) {
                    // Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                    // }
                }
                break;
            case NodeDeleted:
                break;
        }
    }

    @Override
    public void run() {
        LOGGER.info("Checking SLA users:");
//        if(!repositoryService.getUsers().isEmpty()){
//            users=repositoryService.getUsers();
//        }
    }

    public void startSla(SLA sla, Workflow workflow) {
        
        System.out.println("SLAA: "+sla.toString());
        System.out.println("WORKFLOWWW: "+workflow.toString());
        if(sla.getPrediction()){
            System.out.println("Verificar os tempos, atribuidos aos serviços");
        }
        else{
            if(sla.getLimitationExecution()){
                //0 = time
                if(sla.getLimitationType()==LIMITATIONEXECUTIONTIME){
                    System.out.println("Parar quando chegar nesse tempo");
                   
                }else {
                    //1= cust
                    if(sla.getLimitationType()==LIMITATIONEXECUTIONCUST){
                        System.out.println("Parar quando atingir esse valor");
                    }
                }
            }
        }

    }
   
    public void createSlaTemplate(SLA sla, Workflow workflow) {
        System.out.println("Sla: "+sla.toString());
        System.out.println("Workflow: "+ workflow.toString());
        if(!repositoryService.getUsers().isEmpty()){
            users=repositoryService.getUsers();
        }
        
    }

}
