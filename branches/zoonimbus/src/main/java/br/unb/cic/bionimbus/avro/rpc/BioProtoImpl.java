package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import com.google.inject.Inject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.AvroRemoteException;
import org.apache.zookeeper.KeeperException;

public class BioProtoImpl implements BioProto {

    private final DiscoveryService discoveryService;
    private final StorageService storageService;
    private final SchedService schedService;
    private final ZooKeeperService zkService;
    
    private Map<String, NodeInfo> nodes = new HashMap<String, NodeInfo>();

    @Inject
    public BioProtoImpl(DiscoveryService discoveryService, StorageService storageService, SchedService schedService, ZooKeeperService zkservice) {
        this.discoveryService = discoveryService;
        this.storageService = storageService;
        this.schedService = schedService;
        this.zkService =  zkservice;
    }

    @Override
    public boolean ping() throws AvroRemoteException {
        return true;
    }
    
    @Override
    public List<String> listFilesName() throws AvroRemoteException {
        List<String> listFile = new ArrayList<String>();
        for(Collection<String> collection : storageService.getFiles().values()){
            listFile.addAll(collection);
        }
        
        return listFile;
    }
    
    @Override
    public List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFiles() throws AvroRemoteException {
        List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFile = new ArrayList<br.unb.cic.bionimbus.avro.gen.PluginFile>();
        br.unb.cic.bionimbus.avro.gen.PluginFile file=null;
        for(PluginInfo plugin : this.discoveryService.getPeers().values()){
            for(PluginFile fileInfo : storageService.getFilesPeer(plugin.getId())){
                file = new  br.unb.cic.bionimbus.avro.gen.PluginFile();
                file.setId(fileInfo.getId());
                file.setName(fileInfo.getName());
                file.setPath(fileInfo.getPath());
                file.setPluginId(fileInfo.getPluginId());
                file.setSize(fileInfo.getSize());
                listFile.add(file);
            }    
        }
        return listFile;
    }
    
    @Override
    public List<String> listServices() throws AvroRemoteException {
        Collection<PluginInfo> list = this.discoveryService.getPeers().values();
        List<String> listNameIdService = new ArrayList<String>();

        for(PluginInfo plugin : list){
            for(PluginService pluginService: plugin.getServices()){
                listNameIdService.add(pluginService.toString());
                
            }
        }
        
        return listNameIdService;
    }

    @Override
    public String startJobName(String param) throws AvroRemoteException {
        final String path = "/jobs/job_";
        JobInfo job = new JobInfo();
        String params[] = param.split(" ");
        String jobId = params[0];
        int i=1;
            
        job.setServiceId(Long.parseLong(jobId));
        job.setTimestamp(System.currentTimeMillis());
        while (i < params.length) {
            if (i == 1) {
                job.setArgs(params[i]);
                i++;
            } else if (params[i].equals("-i")) {
                i++;
                while (i < params.length && !params[i].equals("-o")) {
                    //verifica a existência dos arquivos de entrada na federação
                    if(!listFilesName().contains(params[i]))
                        return "Job não foi escalonado, arquivo de entrada não existe.";
                    
                    job.addInput(params[i], getPluginFile(params[i]).getSize());
                    i++;
                }
            } else if (params[i].equals("-o")) {
                i++;
                while (i < params.length) {
                    job.addOutput(params[i]);
                    i++;
                }
            }
        }
        
        //inclusão do job para ser escalonado
        zkService.createPersistentZNode(path+job.getId(), job.toString());

        return "Job Escalonado.\n Aguardando execução...";
    }

    @Override
    public String startJob(List<br.unb.cic.bionimbus.avro.gen.JobInfo> listJob) throws AvroRemoteException {
        //inclusão do job para ser escalonado
        
        for (br.unb.cic.bionimbus.avro.gen.JobInfo job: listJob){
            zkService.createEphemeralZNode(zkService.getPath().PREFIX_JOB.getFullPath("", "", job.getId()) , job.toString());
        }
        
        return "Jobs Escalonados.\n Aguardando execução...";
    }
    
    private br.unb.cic.bionimbus.avro.gen.PluginFile getPluginFile(String fileName){
        try {
            for(br.unb.cic.bionimbus.avro.gen.PluginFile file : listFiles()){
                if(file.getName().equals(fileName))
                    return file;
            }
        } catch (AvroRemoteException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    
    }
     
    @Override
    public String cancelJob(String jobID) throws AvroRemoteException {
        //TODO: call schedService
        return "OK";
    }

    @Override
    public synchronized List<NodeInfo> getPeersNode() throws AvroRemoteException {

        NodeInfo nodeaux;
        nodes.clear();
        for(PluginInfo info : discoveryService.getPeers().values()){
           nodeaux= new NodeInfo();
           if(info!=null){
                String address = info.getHost().getAddress();
                nodeaux.setAddress(address);

                nodeaux.setPeerId(info.getId());
                nodeaux.setFreesize(info.getFsFreeSize());
                nodes.put(address, nodeaux);
           }       
        }
        
        return new ArrayList<NodeInfo>(nodes.values());
    }

    /**
     * Passa PluginList para StorageService aqui
     * @return
     * @throws AvroRemoteException 
     */
    @Override
    public List<NodeInfo> callStorage(List<NodeInfo> list) throws AvroRemoteException {
        
        List<NodeInfo> bestnodes = storageService.bestNode(list);
        return bestnodes;
    } 
    
    /**
     * Método que cria o znode do arquivo no diretório /pending_save/file_"id_do arquivo" com as informações de arquivos que clientes querem enviar;
     * @param file
     * @return
     * @throws AvroRemoteException 
     */
    
    @Override
    public void setFileInfo(FileInfo file) {
        PluginFile filePlugin= new PluginFile(file);
        //*Alterar depois caminho para o zookeeperservice
        //verificar se a pasta pending_save existe
        storageService.setPendingFile(filePlugin);
    }

    @Override
    public synchronized void fileSent(FileInfo fileSucess, List<String> dest){
        PluginFile file = new PluginFile(fileSucess);
        file.setPluginId(dest);
        file.setPath("/home/zoonimbus/NetBeansProjects/zoonimbus/data-folder/"+file.getName());
        try {
            storageService.fileUploaded(file);
        } catch (KeeperException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *
     * @param pluginList //Lista com os plugins disponiveis para armazenamento
     * @param nodedest // Node de destino onde foi armazenado o primeiro arquivo
     * @param path // Caminho do arquivo
     * @return
     * @throws JSchException
     * @throws SftpException
     */
    

//    @Override
//        public synchronized Void transferFile(List<NodeInfo> plugins, String path, int copies,List<String> destprimary) throws AvroRemoteException{
//        try {
//            storageService.transferFiles(plugins, path, copies,destprimary);
//        } catch (KeeperException ex) {
//            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }


    /**
     * Metodo irá chamar a storage service passando o nome do arquivo solicitado para download,
     * caso a storage service encontre o arquivo irá retornar o Ip de onde aquele arquivo se encontra,
     * se não encontra retorna a string "Arquivo nao encontrado"
     * @param file - Nome do arquivo requisitado para download.
     * @return - Ip de onde o arquivo se encontra ou "Arquivo nao encontrado"
     * @throws AvroRemoteException
     */
    @Override
    public String listFilesIp(String file){
        
        String destino;    
            
        destino = storageService.getFilesIP(file);
        
        return destino;
    }

    @Override
    public void transferFile(List<NodeInfo> plugins, String path, int copies, List<String> destprimary) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyReply(String filename, String address) {
        try {
            storageService.replication(filename,address);
        } catch (IOException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSchException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SftpException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean verifyFile(FileInfo fileSucess,List dest) {
        PluginFile fileS = new PluginFile(fileSucess);
        fileS.setPluginId(dest);
        return storageService.checkFilePeer(fileS); 
    }

    @Override
    public void setWatcher(String idPlugin) {
//        storageService.starWatchers(idPlugin);
    }


}
