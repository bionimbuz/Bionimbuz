package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.utils.Get;
import java.util.List;

/**
 *
 * @author Deric Esta classe realiza uma chamada RPC para o servidor em que o
 * cliente estiver conectado, esta chamada irá retornar o IP do peer que tiver o
 * arquivo que o cliente deseja baixar.
 */
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
        /*
         * Chamada RPC para o servidor
         * 
         * destino irá receber o IP onde o arquivo se encontra
         * 
         */
        destino = shell.getRpcClient().getProxy().listFilesIp(filerequest);

        Get conexao = new Get();
        /*
         * Tenta se conectar ao destino para baixar o arquivo.
         * Caso sucesso, irá retornar true.
         */
        if (!conexao.startSession(filerequest, destino)) {
            return "Arquivo não encontrado !!";
        }

        return "\n\n Download Completed !!";
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
