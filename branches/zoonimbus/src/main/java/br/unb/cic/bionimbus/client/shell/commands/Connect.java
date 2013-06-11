package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.services.storage.Ping;
import java.util.List;


public class Connect implements Command {

    public static final String NAME = "connect";
    private final SimpleShell shell;
    private List<NodeInfo> pluginList;

    public Connect(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {

        if (params.length != 2) {
            shell.setConnected(false);
            return "Invalid arguments\nusage: connect <address> <port>";
        }

        String hostname = params[0];
        int port = Integer.parseInt(params[1]);
        RpcClient rpcClient = new AvroClient("http", hostname, port);

        // test to see if hostname is reachable
//        rpcClient.getProxy().ping();
        
        shell.setRpcClient(rpcClient);

        shell.setConnected(true);
        
        /*
         * \teste de calculo da politica
         
        for(NodeInfo plugin : shell.getProxy().getPeers()){
                plugin.setLatency(Ping.calculo(plugin.getAddress()));
                pluginList.add(plugin);
                System.out.println("Latencia do plugin "+plugin.getAddress()+"  esta em:"+plugin.getLatency());
        
        }*/
            
        //Seta o os nodes na bioproto
        //shell.getRpcClient().getProxy().setNodes(pluginList);

        return "client is connected.";
        }
//     shell.setConnected(true);


    @Override
    public String usage() {
        return NAME  + " <address> <port>";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
