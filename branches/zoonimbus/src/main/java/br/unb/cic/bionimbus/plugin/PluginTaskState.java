package br.unb.cic.bionimbus.plugin;

public enum PluginTaskState {
    //task escalonado 
    PENDING,
    //task escalonado aguardando execução
    WAITING,
    //task atualmente sendo executado
    RUNNING,
    //task executado
    DONE,
    //task cancelado
    CANCELLED,
    //task EXECUTOU e retornou um erro
    ERRO,
    
}
