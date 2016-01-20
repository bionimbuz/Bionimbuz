/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */

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
    
    private final double peso_latency = 0.5;
    private final double peso_space = 0.2;
    private final double peso_uptime = 0.3;
    private final List<NodeInfo> nodes = new ArrayList<>();
    Collection<PluginInfo> best = new ArrayList<>();
	
    /**
     * Calcular o custo de armazenamento de uma nuvem //ta passando so 1 plugin
     * @param cms
     * @param pluginList
     * @return 
     */
    @Override
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
            String datastring = cms.getData(Path.NODE_PEER.getFullPath(plugin.getId()), null);
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
