package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.gen.FileInfo;



import java.io.File;

//import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.utils.Put;
import java.util.Iterator;

import java.util.List;
import java.util.UUID;

public class Upload implements Command {

//    private final Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    public static final String NAME = "upload";
    private final SimpleShell shell;
//    private long cost = 0;
//    private PluginInfo bestplugin;
    private List<NodeInfo> pluginList;
    private List<NodeInfo> nodes;
//    private ConcurrentMap<String, PluginInfo> map = Maps.newConcurrentMap();
    private Double MAXCAPACITY = 0.9;

    public Upload(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {
        /*
         * Verifica se o arquivo existe
         */        
        
         File file = new File(params[0]);
         if (file.exists()) {
            String dest = null;
            FileInfo info = new FileInfo();
            String path =file.getPath();
            
            info.setFileId(file.getName());
            info.setName(file.getName());
            info.setSize(file.length());
            System.out.println("\n Calculando Latencia.....");
            pluginList = shell.getRpcClient().getProxy().getPeersNode();
            shell.getRpcClient().getProxy().setFileInfo(info);
            for (Iterator<NodeInfo> it = pluginList.iterator(); it.hasNext();) {
                NodeInfo plugin = it.next();
                if ((long)(it.next().getFreesize()*MAXCAPACITY)>info.getSize())
                    plugin.setLatency(Ping.calculo(plugin.getAddress()));
            }
        
        //Seta o os nodes na bioproto
            shell.getRpcClient().getProxy().setNodes(pluginList);
            nodes = shell.getRpcClient().getProxy().callStorage(); 
            
            for(NodeInfo node : nodes){
            
            Put conexao = new Put(node.getAddress(),path);
            
            if(conexao.startSession()){
                  dest = node.getPeerId();
                  return "Upload complete!!!!!";
            }
            //Se a conexão retornou com sucesso;
                       
           shell.getRpcClient().getProxy().fileSent(info,dest);
         }
         //apos o envio do arquivo tudo com sucesso
         
         }
         
        return "teste feito";
//  shell.getRpcClient().getProxy().getPeers();
// verificar diferença sem rpccliente shell.getProxy().getPeers();
//            for(NodeInfo plugin : shell.getRpcClient().getProxy().getPeers()){
//                plugin.setLatency(Ping.calculo(plugin.getAddress()));
//            }
//            
//            //Seta o os nodes na bioproto
//            shell.getRpcClient().getProxy().setNodes(pluginList);
//            
//            //Enviar os nodes para o Bionimbus
//           // shell.getRpcClient().getProxy().sendPlugins(pluginList);
//            return "File" + file.getPath() + "uploaded with success. ";
//         }
//         return "File " + file.getPath() + " don't exists.";
         
         
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
         this.map = getPeers();
         //              Iterator<Map.Entry<String, PluginIndo>> entries = map.entrySet().iterator();
         //              while (entries.hasNext()) {
         //                Map.Entry<String, PluginInfo> entry = entries.next();
         //                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
         //              }
            
         *//*
         * Percorre todos os plugin e calcula a sua latencia e o melhor custo
         *//*
   
         for (PluginInfo plugin : map.values()) {
         if(info.getSize< plugin.getFsFreeSize()*MAXCAPACITY){
         latency = Ping.calculo(plugin.getHost().getAddress());
         plugin.setLatency(latency);// ou map.values().iterator().next().setLatency(Ping.calculo(plugin.getHost().getAddress()));
         storagecost = policy.calcBestCost(plugin);
         plugin.setStorageCost(storagecost);// map.values().iterator().next().setStorageCost(policy.calcBestCost(policy.calcBestCost(plugin)));
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
         //perguntar edward como mandar o 
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
}
