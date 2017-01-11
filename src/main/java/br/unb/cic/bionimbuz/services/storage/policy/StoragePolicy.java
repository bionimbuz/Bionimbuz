/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package br.unb.cic.bionimbuz.services.storage.policy;

import br.unb.cic.bionimbuz.avro.gen.NodeInfo;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Classe que contem os metodos da politica de armazenamento
 *
 * @author deric
 */
public abstract class StoragePolicy {
    
    /**
     * Calcular o custo de armazenamento de uma nuvem //ta passando so 1 plugin
     *
     * @param cms
     * @param pluginList
     * @return 
     */
    public abstract List<NodeInfo> calcBestCost(CloudMessageService cms, Collection<PluginInfo> pluginList);
    
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
        List<PluginInfo> plugin = new ArrayList<>();
        
        for (PluginInfo pluginInfo : plugins) {
            plugin.add(pluginInfo);
        }
        return plugin;
    }
}
