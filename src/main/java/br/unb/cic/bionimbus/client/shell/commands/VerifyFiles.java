package br.unb.cic.bionimbus.client.shell.commands;


import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.security.Integrity;

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
            List<br.unb.cic.bionimbus.avro.gen.PluginFile> zkFiles = rpcClient.getProxy().listFilesPlugin(node.getPeerId());                        
            for(br.unb.cic.bionimbus.avro.gen.PluginFile file : zkFiles) {
                String filePeerHash = rpcClient.getProxy().getFileHash(file.getName());
                if(!Integrity.verifyHashes(file.getHash(), filePeerHash)) {                    
                    System.out.println("Erro no armazenamento do arquivo: " + file.getName());
                    System.out.println("Arquivo não é o mesmo que o armazenado inicialmente.");
                }
             }        
        }
        return "\n\n Verificação da integridade dos arquivos finalizada.";
    }

    @Override
    public String usage() {
        return NAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }
}
