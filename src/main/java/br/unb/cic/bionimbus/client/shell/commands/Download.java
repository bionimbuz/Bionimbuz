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
        destino = shell.getRpcClient().getProxy().getIpFile(filerequest);

        Get conexao = new Get();
        /*
         * Tenta se conectar ao destino para baixar o arquivo.
         * Caso sucesso, irá retornar true.
         */
        if (destino.isEmpty() || !conexao.startSession(filerequest, destino)) {
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
