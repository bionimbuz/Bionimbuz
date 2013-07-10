package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;



public class Connect implements Command {

    public static final String NAME = "connect";
    private final SimpleShell shell;
    private int port = 9999;

    public Connect(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {

        if (params.length != 1) {
            shell.setConnected(false);
            return "Invalid arguments\nusage: connect <address> ";
        }

        
        String hostname = params[0];
        RpcClient rpcClient = new AvroClient("http", hostname, port);

        // test to see if hostname is reachable
        
        if(rpcClient.getProxy().ping()){
            shell.print("Client is true");
        }
        
        shell.setRpcClient(rpcClient);

        
        shell.setConnected(true);

        return "client is connected.";
    }



    @Override
    public String usage() {
        return NAME  + " <address>";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
