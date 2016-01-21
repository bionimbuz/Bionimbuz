/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.mortbay.log.Log;

/**
 *
 * @author will
 * O nome da classe é temporario, assim como sua localização
 * 
 * Dados disponiveis atraves de metodos get
 */
public class RepositoryService {
    
    private final CloudMessageService cms;
    private static final String ROOT_REPOSITORY = CuratorMessageService.Path.HISTORY.toString();
    private static final String PREFIX_TASK = ROOT_REPOSITORY + CuratorMessageService.Path.PREFIX_TASK.toString();
    private static final String SEPARATOR = CuratorMessageService.Path.SEPARATOR.toString();
    private static final String START = CuratorMessageService.Path.START.toString();
    private static final String COUNT = CuratorMessageService.Path.COUNT.toString();

    @Inject
    public RepositoryService(final CloudMessageService cms) {
        this.cms = cms;
    }
    
    // TODO: deve haver uma classe basica contendo as informações de instancias
    // pergunta: qual é a nomeclatura para uma instancia de infra que não foi ativada e 
    //    qual é a nomeclatura para uma instancia ativa (executando algo)
    public List<Instances> getInstancesList() {
        // garante que a lista retornada pode ser a referencia atual, não precisando ser uma copia
        return Collections.unmodifiableList(null);
    }
    
    //Essa função retorna o preço da máquina virtual, a partir do seu id
    public double getInstanceCost(int instId) {
        return null;
    }
    
    // TODO: criar uma classe para arvores (arvores em java parecem ser demasiadamente/desnecessariamente complicadas)
    public TaskNode getDependencyTree(int pipelineId) {
        return null;
    }
    
    // TODO: criar classe que representa um arco na rede (i.e. entre duas instancia ativa)
    // retorna uma lista de arcos e os valores de throughput e latencia dos mesmos
    public List<Latency> getNetworkList () {
        return Collections.unmodifiableList(null);
    }
    
    // retorna um dos elementos da lista de historicos
    // lista atualizada preguiçosamente
    public List<Long> getTaskHistory (String taskId) {
        NavigableMap<Long, Long> currentHistory = new TreeMap<Long, Long>();
        List<Long> maximas = new ArrayList<Long>();

        // check if task is supported
        if(!cms.getZNodeExist(PREFIX_TASK+taskId, false)) {
            // problem: task not supported
            Log.warn("task not suported: task_" + taskId);
            return null;
        }
        
        // get histogram from task taskId
        for(String task : cms.getChildren(PREFIX_TASK+taskId, null)) {
            String count = cms.getData(PREFIX_TASK+taskId+SEPARATOR+task+COUNT, null);
            String intervalStart = cms.getData(PREFIX_TASK+taskId+SEPARATOR+task+START, null);
            currentHistory.put(Long.valueOf(intervalStart), Long.valueOf(count));
        }
        
        // apply moving average
        
        
        // get all local maximas
        
        
        return maximas;
    }
    
}