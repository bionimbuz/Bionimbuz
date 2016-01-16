package br.unb.cic.bionimbus.services.storage.policy.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbus.services.storage.policy.StoragePolicy;

public class ZooClouSPolicy extends StoragePolicy{	
    
    private double peso_latency = 0.5;
    private double peso_space = 0.2;
    private double peso_uptime = 0.3;
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
        double freesize;
        double costpergiga = 0;
        
        /*
        * Calculando os custos de armazenamento dos peers
        * Custo = (Espaço livre + Uptime) * Latencia
        */
        
        for (PluginInfo plugin : pluginList) {
            String datastring = cms.getData(cms.getPath().NODE_PEER.getFullPath(plugin.getId()), null);
            try {
                PluginInfo plugindata = new ObjectMapper().readValue(datastring, PluginInfo.class);
                costpergiga = plugindata.getCostPerGiga();
            } catch (IOException ex) {
                Logger.getLogger(StoragePolicy.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            uptime = plugin.getUptime() / 1000;
            freesize = (plugin.getFsFreeSize() / 1024 / 1024 / 1024);
            cost = ( (plugin.getLatency() * peso_latency) / ( (freesize * peso_space) + (uptime * peso_uptime) ) );
            cost = cost + costpergiga;
            /*
            * Seta o custo de armazenamento no peer
            */
            plugin.setStorageCost(cost);
            cms.setData(Path.NODE_PEER.getFullPath(plugin.getId()), plugin.toString());
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
}
