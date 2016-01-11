package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;

import java.io.File;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.security.AESEncryptor;
import br.unb.cic.bionimbus.security.Hash;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.utils.Nmap;
import br.unb.cic.bionimbus.utils.Put;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

public class Upload implements Command {

    public static final String NAME = "upload";
    private final SimpleShell shell;
    private List<NodeInfo> pluginList;
    private List<NodeInfo> nodesdisp = new ArrayList<>();
    private final Double MAXCAPACITY = 0.9;

    public Upload(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {

        //Verifica se o arquivo existe         
        File file = new File(params[0]);
        AESEncryptor aes = new AESEncryptor();
        if (file.exists()) {
            br.unb.cic.bionimbus.avro.gen.FileInfo info = new br.unb.cic.bionimbus.avro.gen.FileInfo();
            String path = file.getPath();

            //if (!file.getPath().contains("inputfiles.txt")) {
            //TO-DO: Remove comment after William Final Commit
            //aes.encrypt(path);
            //}

            String hashFile = Hash.calculateSha3(path);
            info.setHash(hashFile);
            info.setId(file.getName());
            info.setName(file.getName());
            info.setSize(file.length());
            //Verifica se existe o arquivo, e se existir vefica se é do mesmo tamanho
            if (shell.getProxy().getIpFile(info.getName()).isEmpty() || shell.getProxy().checkFileSize(info.getName()) != info.getSize()) {
                System.out.println("\n Calculando Latencia.....");
                pluginList = shell.getRpcClient().getProxy().getPeersNode();

                //Insere o arquivo na pasta PENDING SAVE do Zookeeper
                shell.getRpcClient().getProxy().setFileInfo(info, "upload!");
                for (Iterator<NodeInfo> it = pluginList.iterator(); it.hasNext();) {
                    NodeInfo plugin = it.next();

                    //Adiciona na lista de possiveis peers de destino somente os que possuem espaço livre para receber o arquivo                    
                    if ((long) (plugin.getFreesize() * MAXCAPACITY) > info.getSize()) {
                        plugin.setLatency(Ping.calculo(plugin.getAddress()));
                        if (plugin.getLatency().equals(Double.MAX_VALUE)) {
                            plugin.setLatency(Nmap.nmap(plugin.getAddress()));
                        }
                        nodesdisp.add(plugin);
                    }
                }

                //Retorna a lista dos nos ordenados como melhores, passando a latência calculada                
                nodesdisp = new ArrayList<>(shell.getRpcClient().getProxy().callStorage(nodesdisp));

                NodeInfo no = null;
                Iterator<NodeInfo> it = nodesdisp.iterator();
                while (it.hasNext() && no == null) {
                    NodeInfo node = (NodeInfo) it.next();

                    //Tenta enviar o arquivo a partir do melhor peer que está na lista                    
                    Put conexao = new Put(node.getAddress(), path);
                    if (conexao.startSession()) {
                        no = node;
                    }
                }
                //Conserta o nome do arquivo encriptado
                //TO-DO: Remove comment after William Final Commit
                //aes.setCorrectFilePath(path);
                if (no != null) {
                    List<String> dest = new ArrayList<>();
                    dest.add(no.getPeerId());
                    nodesdisp.remove(no);
                    //Envia RPC para o peer em que está conectado, para que ele sete no Zookeeper os dados do arquivo que foi upado.                                        
                    return shell.getRpcClient().getProxy().fileSent(info, dest);
                }
            } else {
                //Conserta o nome do arquivo encriptado
                //TO-DO: Remove comment after William Final Commit
                //aes.setCorrectFilePath(path);
                return "\n\n Ja existe um arquivo com mesmo nome e tamanho na federação !!!";
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
