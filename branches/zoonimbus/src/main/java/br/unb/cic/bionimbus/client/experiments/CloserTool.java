package br.unb.cic.bionimbus.client.experiments;

import br.unb.cic.bionimbus.avro.gen.JobInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.gen.Pair;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.shell.commands.SyncCommunication;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.JobRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreRespMessage;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.utils.Put;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class CloserTool {

    private static final Logger LOG = LoggerFactory.getLogger(CloserTool.class);
    private RpcClient rpcClient;

    private int getFileNumLines(File file) throws Exception {
        int numLines = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        while (br.readLine() != null) {
            numLines++;
        }
        br.close();
        return numLines;
    }

    private void writeSmallerFiles(File file, int numLines) throws Exception {
        int numReads = numLines / 4;
        int readChunk = (numReads / 3);
        BufferedWriter w1 = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath() + ".1")));
        BufferedWriter w2 = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath() + ".2")));

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        int count = 0;
        int readsWritten = 0;
        while ((readsWritten < readChunk * 2)
                && ((line = br.readLine()) != null)) {
            if (readsWritten < readChunk)
                w1.write(line + "\n");
            w2.write(line + "\n");

            count++;
            if (count == 4) {
                readsWritten++;
                count = 0;
            }
        }
        br.close();
        w1.close();
        w2.close();
    }

//    private void sendFile(P2PService p2p, SyncCommunication comm, File file) throws Exception {
//        FileInfo info = new FileInfo();
//        info.setName(file.getName());
//        info.setSize(file.length());
//        LOG.info("Enviando arquivo " + info.getName() + " de tamanho " + info.getSize() + " bytes.");
//
//        comm.sendReq(new StoreReqMessage(p2p.getPeerNode(), info, ""), P2PMessageType.STORERESP);
//        StoreRespMessage resp = (StoreRespMessage) comm.getResp();
//        PluginInfo pluginInfo = resp.getPluginInfo();
//        p2p.sendFile(pluginInfo.getHost(), resp.getFileInfo().getName());
//    }

    public void uploadFiles(String filename) throws Exception {
        List<NodeInfo> pluginList;
        List<NodeInfo> nodesdisp = new ArrayList<NodeInfo>();
        Double MAXCAPACITY = 0.9;
        List<File> files = new  ArrayList<File>();
        
        File file0 = new File(filename);
        
        int numLines = getFileNumLines(file0);
        writeSmallerFiles(file0, numLines);
        File file1 = new File(file0.getAbsolutePath() + ".1");
        File file2 = new File(file0.getAbsolutePath() + ".2");
        files.add(file0);
        files.add(file1);
        files.add(file2);

        for(File file : files){
            if (file.exists()){

                br.unb.cic.bionimbus.avro.gen.FileInfo info = new br.unb.cic.bionimbus.avro.gen.FileInfo();
                String path = file.getPath();

                info.setFileId(file.getName());
                info.setName(file.getName());
                info.setSize(file.length());

                //verifica se existi, e se existir vefica se é do mesmo tamanho
            if (rpcClient.getProxy().getIpFile(info.getName()).isEmpty() || (!rpcClient.getProxy().getIpFile(info.getName()).isEmpty()
                    && rpcClient.getProxy().checkFileSize(info.getName()) != info.getSize())){
                    System.out.println("\n Calculando Latencia.....");
                    pluginList = rpcClient.getProxy().getPeersNode();
                    rpcClient.getProxy().setFileInfo(info,"upload!");
                    for (Iterator<NodeInfo> it = pluginList.iterator(); it.hasNext();) {
                        NodeInfo plugin = it.next();
                        /*
                         * Adiciona na lista de possiveis peers de destino somente os que possuem
                         * espaço livre para receber o arquivo
                         */
                        if ((long) (plugin.getFreesize() * MAXCAPACITY) > info.getSize()) {
                            plugin.setLatency(Ping.calculo(plugin.getAddress()));
                            nodesdisp.add(plugin);
                        }
                    }

                    /*
                     * Retorna a lista dos nos ordenados como melhores, passando a latência calculada
                     */
                    nodesdisp = new ArrayList<NodeInfo>(rpcClient.getProxy().callStorage(nodesdisp));


                    NodeInfo no = null;
                    Iterator<NodeInfo> it = nodesdisp.iterator();
                    while (it.hasNext() && no == null) {
                        NodeInfo node = (NodeInfo) it.next();
                        /*
                         * Tenta enviar o arquivo a partir do melhor peer que está na lista
                         */
                        Put conexao = new Put(node.getAddress(), path);
                        if (conexao.startSession()) {
                            no = node;
                        }
                    }
                    if (no != null) {
                        List<String> dest = new ArrayList<String>();
                        dest.add(no.getPeerId());
                        nodesdisp.remove(no);
                        /*
                         * Envia RPC para o peer em que está conectado, para que ele sete no Zookeeper
                         * os dados do arquivo que foi upado.
                         */
                        rpcClient.getProxy().fileSent(info, dest);
                        LOG.info( "\n Upload Completed!!");
                    }

                } else {
                    LOG.info("\n\n Já existe o arquivo"+ file.getName()+"com mesmo nome e tamanho na federação !!!");
                }   
            }

        }
        
        

        
    }

    public void startJobs(int numJobs, String idFull, String idMedium, String idSmall) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        BioNimbusConfig config;
        try {
            config = mapper.readValue(new File("conf/node.yaml"), BioNimbusConfig.class);
            rpcClient = new AvroClient(config.getRpcProtocol(), config.getHost().getAddress(), config.getRpcPort());
            if(rpcClient.getProxy().ping()){
                LOG.info("client is connected.");
            }else{
                LOG.info("client isn't connected.");
                return;
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MscTool.class.getName()).log(Level.SEVERE, null, ex);
        }

//        P2PService p2p = new P2PService(config);
//        p2p.start();
        TimeUnit.SECONDS.sleep(40);
         
//        SyncCommunication comm = new SyncCommunication(p2p);

        List<JobInfo> jobList = new ArrayList<JobInfo>();
        for (int i = 0; i < numJobs; i++) {
            JobInfo job = new JobInfo();
            List<Pair> pairs = new ArrayList<Pair>();
            job.setId(null);
            job.setServiceId(1001l);
            job.setArgs("%O1 e_coli %I1");
            if ((i % 3) == 0){
                pairs.add(new Pair(idFull, Long.valueOf(0)));
            }else if ((i % 3) == 1){
                pairs.add(new Pair(idMedium, Long.valueOf(0)));
            }else if ((i % 3) == 2){
                pairs.add(new Pair(idSmall, Long.valueOf(0)));
                
            }
            
            job.setInputs(pairs);
            
            List<String>  listOut =new ArrayList<String>();
            listOut.add("output-" + i + ".txt");
            job.setOutputs(listOut);
            
            
            jobList.add(job);
            
        }

        LOG.info("Enviando " + jobList.size() + " jobs.");
//        comm.sendReq(new JobReqMessage(p2p.getPeerNode(), jobList), P2PMessageType.JOBRESP);
        String saida = rpcClient.getProxy().startJob(jobList,"");
        
        LOG.info("Job " + saida + " started succesfully");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        CloserTool tool = new CloserTool();

        if (args[0].equals("upload")) {
            tool.uploadFiles(args[1]);
        } else if (args[0].equals("run")) {
            int numJobs = Integer.parseInt(args[1]);
            String id1 = args[2];
            String id2 = args[3];
            String id3 = args[4];
            tool.startJobs(numJobs, id1, id2, id3);
        }
    }

}
