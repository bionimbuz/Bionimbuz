/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package br.unb.cic.bionimbus.services.storage;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Classe que contem os metodos da politica de armazenamento
 *
 * @author deric
 */
public class StoragePolicy {
    
    private double peso_uptime = 0.25;
    private double peso_bandwidth = 0.6;
    private double peso_costs = 0.15;
    private List<NodeInfo> nodes = new ArrayList<NodeInfo>();
    Collection<PluginInfo> best = new ArrayList<PluginInfo>();
    
    /**
     * Calcular o custo de armazenamento de uma nuvem //ta passando so 1 plugin
     *
     * @param zkService
     * @param pluginList
     * @return 
     */
    public List<NodeInfo> calcBestCost(CloudMessageService cms, Collection<PluginInfo> pluginList) {
        
        double cost;
        double uptime;
        double costpergiga = 0;
        double latency;
        double bandwidth;
        		
        
        /*
        * Calculando os custos de armazenamento dos peers
        * Custo = (Espaço livre + Uptime) * Latencia
        */
        
        for (PluginInfo plugin : pluginList) {
            String datastring = cms.getData(cms.getPath().PREFIX_PEER.getFullPath(plugin.getId(), "", ""), null);
            try {
                PluginInfo plugindata = new ObjectMapper().readValue(datastring, PluginInfo.class);
                costpergiga = plugindata.getCostPerGiga();
            } catch (IOException ex) {
                Logger.getLogger(StoragePolicy.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            uptime = plugin.getUptime() / 3600000; //milis to hours
            latency = plugin.getLatency();
            bandwidth = plugin.getBandwidth();
            cost = latency/(peso_uptime*(10*Math.log10(uptime+0.1) +10) + (peso_bandwidth*bandwidth));
            cost = cost + peso_costs * costpergiga;
            /*
            * Seta o custo de armazenamento no peer
            */
            plugin.setStorageCost(cost);
            cms.setData(plugin.getPath_zk(), plugin.toString());
        }
        /*
        * Converte o tipo de list para facilitar o ordenamento dos dados
        */
        List<PluginInfo> plugin = SwapTypePlugin(pluginList);
        sortPlugins(plugin);
        
        /*
        * Converte a lista ordenada em NodeInfo, um objeto menor e que contem somente os dados necessarios
        * para a resolução da politica de armazenamento
        */
        for (PluginInfo plug : plugin) {
            NodeInfo node = new NodeInfo();
            node.setAddress(plug.getHost().getAddress());
            node.setLatency(plug.getLatency());
            node.setPeerId(plug.getId());
            nodes.add(node);
        }

        return nodes;
        
    }
    
    /**
     * Sort para ordenar os melhores peers para armazenar os dados de acordo com
     * o custo de armazenamento das nuvens.
     *
     * @param plugins - Lista com todos os peers que serão ordenados,
     * @return
     */
    public List<PluginInfo> sortPlugins(List<PluginInfo> plugins) {
        
        /*
        * Metodo Sort para ordenar os plugins de acordo com o custo de armazenamento
        */
        Collections.sort(plugins, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                PluginInfo p1 = (PluginInfo) o1;
                PluginInfo p2 = (PluginInfo) o2;
                return p1.getStorageCost() < p2.getStorageCost() ? -1 : (p1.getStorageCost() > p2.getStorageCost() ? +1 : 0);
            }
        });
        
        return plugins;
    }
    
    /**
     * Método para receber a map com os plugins e converter em List, para ficar
     * mais fácil o tratamento dos dados
     *
     * @param plugins
     * @return
     */
    public List<PluginInfo> SwapTypePlugin(Collection<PluginInfo> plugins) {
        List<PluginInfo> plugin = new ArrayList<PluginInfo>();
        
        for (PluginInfo pluginInfo : plugins) {
            plugin.add(pluginInfo);
        }
        return plugin;
    }
}