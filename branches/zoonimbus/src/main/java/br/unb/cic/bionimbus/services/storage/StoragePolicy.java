/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.storage;


import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.BioProtoImpl;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.ZooKeeperService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.AvroRemoteException;
import org.apache.zookeeper.KeeperException;


/**
 * @author deric
 */
public class StoragePolicy{

    private double peso_latency = 0.5;
    private double peso_space = 0.2;
    private double peso_uptime = 0.3;
    private List<NodeInfo> nodes = new ArrayList<NodeInfo>();

    Collection<PluginInfo> best = new ArrayList<PluginInfo>();

    /**
     * Calcular o custo de armazenamento de uma nuvem //ta passando so 1 plugin
     * @param pluginList 
     */
    
    public List<NodeInfo> calcBestCost(ZooKeeperService zkService,Collection<PluginInfo> pluginList) {

        double cost;
        double uptime;
        double freesize;
        
        /*Calculando os custos de armazenamento dos peers
         *Custo = (Espaço livre + Uptime) * Latencia
         */
       
        
        for(PluginInfo plugin : pluginList){
             uptime = plugin.getUptime() / 1000;
             freesize = (plugin.getFsFreeSize() / 1024 / 1024 / 1024);
             cost = (((freesize * peso_space) + 
                (uptime * peso_uptime)) * 
                (plugin.getLatency() * peso_latency));
             plugin.setStorageCost(cost);             
             try {
                   zkService.setData(plugin.getPath_zk(), plugin.toString());
               } catch (KeeperException ex) {
                   Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
               } catch (InterruptedException ex) {
                   Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
               }
             
        }
        List<PluginInfo> plugin = SwapTypePlugin(pluginList);
        System.out.println("\n Antes do ordenamento");
        for(PluginInfo plug : plugin){
            System.out.println("ID: "+plug.getId()+"\n Custo de armazenamento: "+plug.getStorageCost());
        }
        sortPlugins(plugin);
        
        for(PluginInfo plug : plugin){
            NodeInfo node = new NodeInfo();
            node.setAddress(plug.getHost().getAddress());
            node.setLatency(plug.getLatency());
            node.setPeerId(plug.getId());
            nodes.add(node);
        }
        
        
        return nodes;

    }

    /**
     * Quicksort para ordenar as melhores nuvens para armazenar os dados de acordo com o 
     * custo de armazenamento das nuvens
     */
    public List<PluginInfo> sortPlugins(List<PluginInfo> plugins) {  
        
        /*
         * Metodo Sort para ordenar os plugins de acordo com o custo de armazenamento
         */
        Collections.sort(plugins,new Comparator() {

         
          
            @Override
            public int compare(Object o1, Object o2) {
                PluginInfo p1 = (PluginInfo) o1;
                PluginInfo p2 = (PluginInfo) o2;
                return p1.getStorageCost() < p2.getStorageCost()? -1 : (p1.getStorageCost() > p2.getStorageCost() ? +1 : 0);    
            }
        });
        System.out.println("\n DEPOIS DO ORDENAMENTO");
        for(PluginInfo plugin : plugins){
            System.out.println("ID: "+plugin.getId()+"\n Custo de armazenamento: "+plugin.getStorageCost());
        }
        return plugins;
    } 
        
    /*
     * Método para receber a map com os plugins e converter em List, para ficar mais fácil o tratamento dos dados
     */
    public List<PluginInfo> SwapTypePlugin(Collection<PluginInfo> plugins){
        List<PluginInfo> plugin = new ArrayList<PluginInfo>();

        for (PluginInfo pluginInfo : plugins) {
            plugin.add(pluginInfo);
        }
        return plugin;
    } 
    
    
    public void sendFile(){
        
        
    }
}