/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.storage;


import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 *
 * @author deric
 */
public class StoragePolicy {

    private double peso_latency = 0.5;
    private double peso_space = 0.2;
    private double peso_uptime = 0.3;
    
    Collection<PluginInfo> best = new ArrayList<PluginInfo>();
    
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    
    // Método para receber as variaveis e calcular o custo de armazenamento
    public long calcBestCost(long latency) {
    
        long cost;
        
        PluginInfo pluginInfo = null;
        
        //Calculando os custos de armazenamento dos peers
        cost = (long) ((pluginInfo.getFsFreeSize() * peso_space) + (pluginInfo.getUptime() * peso_uptime) + (latency * peso_latency));
        
        return cost;
     
    }
    /*
    public long compare(PluginInfo a, PluginInfo b){
            PluginInfo a1 = (PluginInfo) a;
            PluginInfo b1 = (PluginInfo) b;
            return a1.getLatency() < b1.getLatency() ? -1 : (a1.getLatency() > b1.getLatency() ? +1 : 0);            
    }*/
    
    
    
}