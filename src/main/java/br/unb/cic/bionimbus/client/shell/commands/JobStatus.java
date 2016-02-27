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

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;

public class JobStatus implements Command {

    public static final String NAME = "status";

    private final SimpleShell shell;

    public JobStatus(SimpleShell shell) {
        this.shell = shell;
    }
    
    @Override
    public String execute(String... params) throws Exception {
        
        BioProto rpc = shell.getProxy();
        
        // verifica se é a solicitação de status para todos os jobs
        if(params[0].equalsIgnoreCase("all")){
            return rpc.statusAllJob();
        }else{
//            return rpc.statusJob(params[0]);
            throw new UnsupportedOperationException();
        }
        
        
    }

    @Override
    public String usage() {
        return NAME + " <jobId> \n"+NAME+" ALL";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
