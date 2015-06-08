/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author will
 * O nome da classe é temporario, assim como sua localização
 * 
 * Dados disponiveis atraves de metodos get
 */
public class RepositoryService {
    
    // TODO: deve haver uma classe basica contendo as informações de instancias
    // pergunta: qual é a nomeclatura para uma instancia de infra que não foi ativada e 
    //    qual é a nomeclatura para uma instancia ativa (executando algo)
    public List<Instances> getInstancesList() {
        // garante que a lista retornada pode ser a referencia atual, não precisando ser uma copia
        return Collections.unmodifiableList(null);
    }
    
    public float getInstanceCost(int instId) {
        return 0;
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
    public List<Long> getTaskHistory (int taskId) {
        return Collections.unmodifiableList(null);
    }
    
}
