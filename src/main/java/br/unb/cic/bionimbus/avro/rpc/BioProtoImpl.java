package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.plugin.*;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.avro.AvroRemoteException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Classe de Implementação dos métodos criados na bioproto.avdl, rpc
 * @author zoonimbus
 * 
 * OBSERVAÇÃO: Não esquecer de passar todas atualizações feitas aqui
 * para o arquivo bioproto.avdl
 * 
 * Caso isso não seja feito a geração do BioProto.java estará errada e 
 * o sistema não compilará no futuro.
 */
public class BioProtoImpl implements BioProto {

    private final DiscoveryService discoveryService;
    private final StorageService storageService;
    private final CloudMessageService cms;

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedService.class.getSimpleName());
    
    private Map<String, NodeInfo> nodes = new HashMap<String, NodeInfo>();

    @Inject
    public BioProtoImpl(DiscoveryService discoveryService, StorageService storageService, SchedService schedService, CloudMessageService cms) {
        this.discoveryService = discoveryService;
        this.storageService = storageService;
        this.cms =  cms;
    }

    @Override
    public boolean ping() throws AvroRemoteException {
        return true;
    }
    
    /**
     * Retorna o status do job solicitado.
     * @param pipelineId id do pipeline contendo o job
     * @param jobId id do job que deve ser consultado
     * @return string com o status do job
     * @throws AvroRemoteException 
     */
    @Override
    public String statusJob(String pipelineId, String jobId) throws AvroRemoteException {
//        try {
//            if(cms.getChildren(cms.getPath().JOBS.getFullPath("", "", "", pipelineId), null).contains("job_"+jobId)){
//                return  "Job "+jobId+" ainda não foi escalonado";
//            }else {
//                String datas =null;
//                ObjectMapper mapper = new ObjectMapper();
//                for(PluginInfo plugin : storageService.getPeers().values()){
//                    for(String task : cms.getChildren(cms.getPath().TASKS.getFullPath(plugin.getId(), "", "", ""), null)){
//                        datas = cms.getData(cms.getPath().PREFIX_TASK.getFullPath(plugin.getId(),"",task.substring(5, task.length()), ""),null);
//                        if(datas!=null){
//                            PluginTask pluginTask = mapper.readValue(datas, PluginTask.class);
//                            if(pluginTask.getJobInfo().getId().equals(jobId))
//                                return "Job: "+pluginTask.getState().toString();
//                        }
//                    }
//                }
//                    
//            }
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return "Job "+jobId+" não encontrado!";
        throw new AvroRemoteException("IMPLEMENTATION REMOVED/COMMENTED");
    }

    /**
     * Retorna o status de todos os jobs existentes.
     * @return string com o status de todos os jobs
     * @throws AvroRemoteException 
     */
    @Override
    public String statusAllJob() throws AvroRemoteException {
        StringBuilder allJobs = new StringBuilder();
        int i=1;
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> pipelines = cms.getChildren(cms.getPath().PIPELINES.getFullPath(), null);
            for(String pipeline : pipelines) {
                //verificação dos jobs ainda não escalonados
//                List<String> jobs = cms.getChildren(cms.getPath().JOBS.getFullPath("", "", "", pipeline), null);
//                if(jobs!=null && !jobs.isEmpty()){
//                    for(String job : jobs){
//                        String jobData = cms.getData(cms.getPath().PREFIX_JOB.getFullPath("","",job.substring(4,job.length()), pipeline),null);
//                        if(jobData!=null){
//                            JobInfo jobInfo = mapper.readValue(jobData, JobInfo.class);
//                            allJobs.append(i).append(" - Job ").append(jobInfo.getId()).append(" Ainda não escalonado.\n ");
//                        }
//                        i++;
//                    }
//                }
                allJobs.append(i).append(" - Pipeline ").append(pipeline).append(" Ainda não escalonado.\n ");
                i++;
            }
            
            //verificação dos jobs escalonados
            String datasTask =null;
            for(PluginInfo plugin : storageService.getPeers().values()){
                for(String task : cms.getChildren(cms.getPath().TASKS.getFullPath(plugin.getId()), null)){
                    datasTask = cms.getData(cms.getPath().PREFIX_TASK.getFullPath(plugin.getId(),task.substring(5, task.length())),null);
                    if(datasTask!=null){
                        PluginTask pluginTask = mapper.readValue(datasTask, PluginTask.class);
                        allJobs.append(i).append(" - Job ").append(pluginTask.getId().toString()).append(" : ").append(pluginTask.getState().toString()).append("\n ");
                    }
                    i++;
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return  allJobs.toString().isEmpty() ? "Não existem jobs." : "Jobs :\n "+allJobs;
    }
    
    
    
    /**
     * Retorna uma lista com o nome dos arquivos pertencentes a toda a federação.
     * @return lista com nome dos arquivos
     * @throws AvroRemoteException 
     */
    @Override
    public List<String> listFilesName() throws AvroRemoteException {
        ArrayList<String> listFile = new ArrayList<String>();
        try {
            for(Collection<String> collection : storageService.getFiles().values()){
                listFile.removeAll(collection);
                listFile.addAll(collection);
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return listFile;
    }
    
    
    
     /**
     * 
     * @param fileName
     * @return lista com nome dos arquivos
     * @throws AvroRemoteException 
     */
    @Override
    public String getFileHash(String fileName) throws org.apache.avro.AvroRemoteException{        
        try {
            String pathHome = System.getProperty("user.dir");
            String path =  (pathHome.substring(pathHome.length()).equals("/") ? pathHome+"data-folder/" : pathHome+"/data-folder/");
            String hash = storageService.getFileHash(path + fileName);
            return hash;
        } catch (IOException ex) {        
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }catch (NoSuchAlgorithmException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * 
     * @return
     * @throws AvroRemoteException 
     */
    @Override
    public List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFiles() throws AvroRemoteException {
        
        HashSet<br.unb.cic.bionimbus.avro.gen.PluginFile> listFiles = new HashSet<br.unb.cic.bionimbus.avro.gen.PluginFile>();
        br.unb.cic.bionimbus.avro.gen.PluginFile file=null;
        for(PluginInfo plugin : this.discoveryService.getPeers().values()){
            for(PluginFile fileInfo : storageService.getFilesPeer(plugin.getId())){
                file = new  br.unb.cic.bionimbus.avro.gen.PluginFile();
                file.setId(fileInfo.getId());
                file.setName(fileInfo.getName());
                //retorno com getName  para o path porque avro não reconhece barra(/), adicionar data-folder/
                //ao receber o retono deste método
                file.setPath(fileInfo.getName());
                file.setPluginId(fileInfo.getPluginId());
                file.setSize(fileInfo.getSize());
                listFiles.add(file);
            }    
        }
        List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFile = new ArrayList<br.unb.cic.bionimbus.avro.gen.PluginFile>(listFiles);

        return listFile;
    }
    
    /**
     * 
     * @param pluginId
     * @return
     * @throws AvroRemoteException 
     */
    @Override
    public List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFilesPlugin(String pluginId) throws AvroRemoteException {
            HashSet<br.unb.cic.bionimbus.avro.gen.PluginFile> listFiles = new HashSet<br.unb.cic.bionimbus.avro.gen.PluginFile>();
            br.unb.cic.bionimbus.avro.gen.PluginFile file;
            for(PluginFile fileInfo : storageService.getFilesPeer(pluginId)){
                file = new  br.unb.cic.bionimbus.avro.gen.PluginFile();
                file.setId(fileInfo.getId());
                file.setName(fileInfo.getName());
                file.setPath(fileInfo.getName());
                file.setPluginId(fileInfo.getPluginId());
                file.setSize(fileInfo.getSize());
                file.setHash(fileInfo.getHash());
                listFiles.add(file);
            }    
        List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFile = new ArrayList<br.unb.cic.bionimbus.avro.gen.PluginFile>(listFiles);

        return listFile;
    }
    
    /**
     * Retorna o ip que contém o arquivo informado @param file.
     * Se não encontrar o arquivo retorna null
     * @param file - Nome do arquivo requisitado 
     * @return - Ip de onde o arquivo se encontra, ou caso não encontre retorna null;
     */
    @Override
    public String getIpFile(String file){
        
        String destino="";    
        try {
            destino =   storageService.getIpContainsFile(file);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return destino;
    }
    
    @Override
    public List<String> listServices() throws AvroRemoteException {
        Collection<PluginInfo> list = this.discoveryService.getPeers().values();
        List<String> listNameIdService = new ArrayList<String>();

        for(PluginInfo plugin : list){
            for(PluginService pluginService: plugin.getServices()){
                if(!listNameIdService.contains(pluginService.toString()))
                    listNameIdService.add(pluginService.toString());
                
            }
        }
        
        return listNameIdService;
    }
    /**
     * Altera a política de escalonamento para todas os jobs. E retorna a política de escalonamento alterado ou em uso.
     * @param numPolicy, numero da política de escalonamento desejada.   0- AcoSched (Padrão), 1- AHPPolicy, 2- RRPolicy. 
     * Quando valor for -1 deve retornar qual a política de escalonamento está sendo utilizada.
     * @return política de escalonamento
     */
    @Override
    public String schedPolicy(int numPolicy){
//        //verifica se escolher informar a política ou identificar qual é a política
//        if(numPolicy==-1){
//            numPolicy = new Integer(cms.getData(cms.getPath().JOBS.toString(), null));
//        }else{
//            Integer policy = numPolicy;
//            if(cms.getZNodeExist(cms.getPath().JOBS.toString(), false)){
//                cms.setData(cms.getPath().JOBS.toString(), policy.toString());
//            }else{
//                return "\nNão foi possível alterar  política de escalonamento. Tente mais tarde.";
//            }
//        }
//        
//        StringBuilder politicys = new StringBuilder();
//        List<SchedPolicy> listPolicy = SchedPolicy.getInstances();
//        for(SchedPolicy policy:listPolicy){
//            politicys.append("\n    ").append(policy.getPolicyName());
//        }
//        
//        
//        return "\nPolítica Atual: "+listPolicy.get(numPolicy).getPolicyName()+"\n\nPolíticas Disponíveis: "+politicys;
        throw new UnsupportedOperationException("METOD COMMENTED");
    }
    
    @Override
    public String startJobName(String param, String ip) throws AvroRemoteException {
//        final String path = "/jobs/job_";
//        JobInfo job = new JobInfo();
//        String params[] = param.split(" ");
//        String jobId = params[0];
//        int i=1;
//            
//        job.setServiceId(Long.parseLong(jobId));
//        job.setLocalId(ip);
//        job.setTimestamp(System.currentTimeMillis());
//        while (i < params.length) {
//            if (i == 1) {
//                job.setArgs(params[i]);
//                i++;
//            } else if (params[i].equals("-i")) {
//                i++;
//                while (i < params.length && !params[i].equals("-o")) {
//                    //verifica a existência dos arquivos de entrada na federação
//                    if(!listFilesName().contains(params[i]))
//                        return "Job não foi escalonado, arquivo de entrada não existe.";
//                    
//                    job.addInput(params[i], getPluginFile(params[i]).getSize());
//                    i++;
//                }
//            } else if (params[i].equals("-o")) {
//                i++;
//                while (i < params.length) {
//                    job.addOutput(params[i]);
//                    i++;
//                }
//            }
//        }
//        LOGGER.info("Tempo de inicio do job -"+ job.getOutputs()+"- MileSegundos: " + job.getTimestamp());
//        //inclusão do job para ser escalonado
//        cms.createZNode(CreateMode.PERSISTENT, path+job.getId(), job.toString());
//        
//        return "Job enviado para o escalonamento, Id : "+job.getId()+".\nAguarde...";
        throw  new UnsupportedOperationException("Function commented. Also, if needed, this must be updated to new pipeline model");
    }

    @Override
    public String startPipeline(br.unb.cic.bionimbus.avro.gen.PipelineInfo pipeline) throws AvroRemoteException {
        // generate pipeline register
        cms.createZNode(CreateMode.PERSISTENT, cms.getPath().PREFIX_PIPELINE.getFullPath(pipeline.getId()), pipeline.toString());
        
        return "Pipeline enviado para o escalonamento. Aguarde...";
    }
    
    private br.unb.cic.bionimbus.avro.gen.PluginFile getPluginFile(String fileName){
        try {
            for(br.unb.cic.bionimbus.avro.gen.PluginFile file : listFiles()){
                if(file.getName().equals(fileName))
                    return file;
            }
        } catch (AvroRemoteException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;    
    }
     
    @Override
    public String cancelJob(String jobID) throws AvroRemoteException {
//        schedService.cancelJob(jobID);
        return "Not enabled";
    }
    
    /**
     * Método RPC que pega os peers do zoonimbus e retorna uma lista do tipo NodeInfo
     * @return lista de NodeInfo PeerId, Address, Freesize;
     * @throws AvroRemoteException 
     */
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
     * @param list
     * @return bestnodes lista do tipo NodeInfo, retornando os melhores nós da federação
     * @throws AvroRemoteException 
     */
    @Override
    public List<NodeInfo> callStorage(List<NodeInfo> list) throws AvroRemoteException {
        
        List<NodeInfo> bestnodes = storageService.bestNode(list);
        return bestnodes;
    } 
    
    /**
     * Método que cria o znode do arquivo no diretório /pending_save/file_"id_do arquivo" com as informações de arquivos que clientes querem enviar;
     * @param file informações do arquivo:id,nome, tamanho e hash
     * @param kindString Tipo de serviço que está requisitando o arquivo 
     */  
    @Override
    public void setFileInfo(FileInfo file, String kindString) {
        PluginFile filePlugin= new PluginFile(file);
        filePlugin.setService(kindString);
        //*Alterar depois caminho para o zookeeperservice
        //verificar se a pasta pending_save existe
        storageService.setPendingFile(filePlugin);
    }
    
    /**
     * Método RPC que retorna o tamanho do arquivo verificado em outro peer;
     * @param file Nome do arquivo
     * @return  size o tamanho do arquivo
     */
    @Override
    public long checkFileSize(String file){
        
       long size = storageService.getFileSize(file);
        
       return size;   
    }
    /**
     * Método avro que chama o método fileuploaded da storage para avisar que o arquivo foi enviado.
     * @param fileSucess informações do arquivo:id,nome e tamanho
     * @param dest lista com os plugins de destino
     */
    @Override
    public String fileSent(FileInfo fileSucess, List<String> dest){
        PluginFile file = new PluginFile(fileSucess);
        file.setPluginId(dest);
        String pathHome = System.getProperty("user.dir");
        String path =  (pathHome.substring(pathHome.length()).equals("/") ? pathHome+"data-folder/" : pathHome+"/data-folder/");
        file.setPath(path+file.getName());
        String retorno = "File uploaded.";
        try {
            retorno = storageService.fileUploaded(file);
            return retorno;
        } catch (KeeperException | InterruptedException | IOException | NoSuchAlgorithmException | SftpException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }
    
    /**
     * Método que notifica o peer para fazer a replicação
     * @param filename nome do arquivo a ser replicado
     * @param address  endereço do peer que possui o arquivo
     */
    @Override
    public void notifyReply(String filename, String address) {
        try {
            storageService.replication(filename,address);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSchException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SftpException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *Método que verifica se o arquivo existe nó de destino 
     * @param fileSucess
     * @param dest lista de ids 
     * @return 
     */
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
