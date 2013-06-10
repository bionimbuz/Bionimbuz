package br.unb.cic.bionimbus.plugin;

public enum PluginTaskState {
    //job aguardando escalonamento
    PENDING,
    //job escalonado aguardando execução
    WAITING,
    //job atualmente sendo executado
    RUNNING,
    //job executado
    DONE,
    //job cancelado
    CANCELLED,
}
