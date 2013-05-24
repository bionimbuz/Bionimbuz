/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.storage;

import java.util.ArrayList;
import java.util.Collection;

import br.unb.cic.bionimbus.plugin.PluginInfo;


/**
 *
 * @author deric
 */
public class StoragePolicy {
    
    private double peso_Latencia = 0.5;
    private double peso_Freesize = 0.2;
    private double peso_Uptime = 0.3;
    
    // Método para receber as variaveis e calcular o custo de armazenamento
    public float calcBestCost(PluginInfo plugin) {
        
        float cost;
        float bestcost;
       // PluginInfo plugin = new PluginInfo();
        
                cost = (float) ((plugin.getFsFreeSize() * peso_Freesize) + (plugin.getUptime() * peso_Uptime) + (plugin.getLatency() * peso_Latencia));
                plugin.setStorageCost(cost);
            
        
        return cost;
        
    }
    
    
    
}
