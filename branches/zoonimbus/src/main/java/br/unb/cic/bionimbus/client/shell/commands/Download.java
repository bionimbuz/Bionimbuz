package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.utils.Get;
import br.unb.cic.bionimbus.utils.Put;
import java.util.List;

public class Download implements Command {

    public static final String NAME = "download";
    private final SimpleShell shell;
    private List<String> filesList;
    private String destino;
    
    public Download(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {
        
        String filerequest = params[0];
        destino = shell.getRpcClient().getProxy().listFilesIp(filerequest);      
                
        Get conexao = new Get();
          if(conexao.startSession(filerequest)){ 
                     return "Download Completed";
          }else{
                    return "Arquivo não encontrado !!";
          }
    }

    @Override
    public String usage() {
        return NAME + " <fileId>";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
