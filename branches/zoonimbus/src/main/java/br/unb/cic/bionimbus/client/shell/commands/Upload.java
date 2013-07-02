
package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.gen.FileInfo;



import java.io.File;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.utils.Put;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

public class Upload implements Command {

    public static final String NAME = "upload";
    private final SimpleShell shell;
    private List<NodeInfo> pluginList;
    private List<NodeInfo> nodesdisp = new ArrayList<NodeInfo>();
    private Double MAXCAPACITY = 0.9;
    private int flag = 0;

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
                if ((long)(plugin.getFreesize()*MAXCAPACITY)>info.getSize()){
                    plugin.setLatency(Ping.calculo(plugin.getAddress()));
                    nodesdisp.add(plugin);
                }    
            }
            //Retorna a lista dos nos ordenados como melhores, passando a latência calculada
            nodesdisp = new ArrayList<NodeInfo>(shell.getRpcClient().getProxy().callStorage(nodesdisp)); 
           
            NodeInfo no=null;
            Iterator<NodeInfo> it = nodesdisp.iterator();
            while (it.hasNext() && no == null) {
                 NodeInfo node = (NodeInfo)it.next();
                 
                 Put conexao = new Put(node.getAddress(),path,flag);                
                 if(conexao.startSession()){
                       no = node;
                }
            }
            if(no != null){
                List<String> dest = new ArrayList<String>();
                dest.add(no.getPeerId());
                nodesdisp.remove(no);             
                shell.getRpcClient().getProxy().fileSent(info,dest);
                return "\n Upload Completed!!";
            }
         }
         
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
