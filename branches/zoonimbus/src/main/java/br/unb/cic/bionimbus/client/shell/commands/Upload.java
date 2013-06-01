package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.services.AbstractBioService;

import java.io.File;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreRespMessage;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.services.storage.StoragePolicy;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.zookeeper.KeeperException;
import org.codehaus.jackson.map.ObjectMapper;

public class Upload extends AbstractBioService implements Command {
    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();

    public static final String NAME = "upload";

    private final SimpleShell shell;

    private long latency;

    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private static final String PREFIX_PEER = "peer_";
    private long cost=0;
    private PluginInfo bestplugin;
    private String peerName;
    private List<String> children;
    private ConcurrentMap<String, PluginInfo> map = Maps.newConcurrentMap();

    public Upload(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {
        return null;
/*        if (!shell.isConnected())
            throw new IllegalStateException("This command should be used with an active connection!");

//        P2PService p2p = shell.getP2P();
//        SyncCommunication comm = new SyncCommunication(p2p);

        shell.print("Uploading file...");
        *//*
         * Instacia um objeto StoragePolicy para realizar o calculo do custo de armazenamento
         *//*
        StoragePolicy policy = new StoragePolicy();
        Ping ping = new Ping();
        long storagecost;

        *//*
         * Verifica se o arquivo existe
         *//*
        File file = new File(params[0]);
        if (file.exists()) {
            FileInfo info = new FileInfo();
            info.setName(params[0]);
            info.setSize(file.length());

            this.map = getPeers();
            *//*
             * Percorre todos os plugin e calcula a sua latencia
             *//*
            for (PluginInfo plugin : map.values()) {
                latency = Ping.calculo(plugin.getHost().getAddress());
                plugin.setLatency(latency);
                storagecost = policy.calcBestCost(plugin);
                plugin.setStorageCost(storagecost);
            }


             String childStr;
        for (String child : children) {
                   try {
                        childStr = zkService.getData(ROOT_PEER + SEPARATOR + child, null);
                        System.out.println(childStr); 
                        ObjectMapper mapper = new ObjectMapper();
                        PluginInfo myInfo = mapper.readValue(childStr, PluginInfo.class);                         
                        map.put(myInfo.getId(), myInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
       
           *//*
            * Ordena os custos de armazenamento atraves do QuickSort
            *//*
           List pluginlist = policy.SwapTypePlugin(map.values());
           policy.SortPlugins(pluginlist);
           
           for (PluginInfo plugin : map.values()) {
                System.out.println("\n\n Plugins para armazenamento : ");
                System.out.println("\n IP : "+plugin.getHost().getAddress()+"  StorageCost: "+plugin.getStorageCost());
            }
           System.out.println("\n Melhor plugin para o armazenamento: " +bestplugin.getHost().getAddress());

           
            comm.sendReq(new StoreReqMessage(p2p.getPeerNode(), info, ""), P2PMessageType.STORERESP);
            StoreRespMessage resp = (StoreRespMessage) comm.getResp();
            PluginInfo pluginInfo = resp.getPluginInfo();
            p2p.sendFile(pluginInfo.getHost(), resp.getFileInfo().getName());
           
           
           *//*
            * Envia o arquivo para o melhor plugin
            *//*
          
                

            return "File " + resp.getFileInfo().getName() + " succesfully uploaded.";
        }
        return "File " + file.getPath() + " don't exists.";*/
    }

    @Override
    public String usage() {
        return NAME + " <filepath>";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

    @Override
    public void start(P2PService p2p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onEvent(P2PEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ConcurrentMap<String, PluginInfo> getPeers() {

        System.out.println(peerName);
        try {

            children = zkService.getChildren(ROOT_PEER, null);

            map.clear();
            for (String child : children) {
                // if (!peerName.contains(child)){
                try {
                    String childStr = zkService.getData(ROOT_PEER + SEPARATOR + child, null);
                    System.out.println(childStr);
                    ObjectMapper mapper = new ObjectMapper();
                    PluginInfo myInfo = mapper.readValue(childStr, PluginInfo.class);
                    map.put(myInfo.getId(), myInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

}