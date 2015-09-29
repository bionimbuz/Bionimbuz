package br.unb.cic.bionimbus.client.shell.commands;


import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.plugin.PluginFile;
import com.google.common.base.Joiner;

import java.util.List;

public class VerifyFiles implements Command {

    public static final String NAME = "integrity";
    private final SimpleShell shell;
    private List<NodeInfo> nodeList;

    public VerifyFiles(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {        
        if (!shell.isConnected()) {
            throw new IllegalStateException("This command should be used with an active connection!");
        }
        
        nodeList = shell.getRpcClient().getProxy().getPeersNode();
        for(NodeInfo node : nodeList) {            
            RpcClient rpcClient = new AvroClient("http", node.getAddress(), 8080);          
            //TO-DO: Implementar método no bioproto.avdl
            //TO-DO: Examinar cada arquivo de acordo com oq está salvo no zookeeper(arquivo criptografado)
            //List<PluginFile> files = rpcClient.getProxy().getFilesPeer(node.getPeerId());                        
            //for(PluginFile file : files) {
            // String filePeerHash = rpcClient.getProxy().getFileHash(file.getName());
            //String zkFileHash = ???
        }
        
        //TO-DO: Retornar dados referentes aos arquivos do zookeeper
        //return true;
        
        return "\n\n Erro no upload !!";
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
