/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.avro.rpc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.avro.AvroRemoteException;
import org.apache.zookeeper.KeeperException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import br.unb.cic.bionimbuz.avro.gen.BioProto;
import br.unb.cic.bionimbuz.avro.gen.FileInfo;
import br.unb.cic.bionimbuz.avro.gen.NodeInfo;
import br.unb.cic.bionimbuz.avro.gen.Workflow;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.slacontroller.SlaController;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.plugin.PluginFile;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginService;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.security.AESEncryptor;
import br.unb.cic.bionimbuz.security.HashUtil;
import br.unb.cic.bionimbuz.services.discovery.DiscoveryService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.services.monitor.MonitoringService;
import br.unb.cic.bionimbuz.services.sched.SchedService;
import br.unb.cic.bionimbuz.services.storage.StorageService;

/**
 * Classe de Implementação dos métodos criados na bioproto.avdl, rpc
 *
 * @author zoonimbus
 *
 *         OBSERVAÇÃO: Não esquecer de passar todas atualizações feitas aqui para o
 *         arquivo bioproto.avdl
 *
 *         Caso isso não seja feito a geração do BioProto.java estará errada e o sistema
 *         não compilará no futuro.
 */
public class BioProtoImpl implements BioProto {
    
    private final DiscoveryService discoveryService;
    private final StorageService storageService;
    private final MonitoringService monitoringService;
    private final CloudMessageService cms;
    private final UserController userController;
    private final SchedService schedService;
    private final SlaController slaController;
    private final Map<String, NodeInfo> nodes = new HashMap<>();
    
    @Inject
    public BioProtoImpl(DiscoveryService discoveryService, StorageService storageService, SchedService schedService,MonitoringService monitoringService, UserController userController ,CloudMessageService cms, SlaController slaController) {
        this.discoveryService = discoveryService;
        this.storageService = storageService;
        this.monitoringService = monitoringService;
        this.schedService =schedService;
        this.cms = cms;
        this.userController = userController;
        this.slaController = slaController;
    }
    
    @Override
    public boolean ping() throws AvroRemoteException {
        return true;
    }
    
    /**
     * Retorna o status do job solicitado.
     *
     * @param pipelineId
     *            id do pipeline contendo o job
     * @param jobId
     *            id do job que deve ser consultado
     * @return string com o status do job
     * @throws AvroRemoteException
     */
    @Override
    public String statusJob(String pipelineId, String jobId) throws AvroRemoteException {
        // try {
        // if(cms.getChildren(cms.getPath().JOBS.getFullPath("", "", "", pipelineId), null).contains("job_"+jobId)){
        // return "Job "+jobId+" ainda não foi escalonado";
        // }else {
        // String datas =null;
        // ObjectMapper mapper = new ObjectMapper();
        // for(PluginInfo plugin : storageService.getPeers().values()){
        // for(String task : cms.getChildren(cms.getPath().TASKS.getFullPath(plugin.getId(), "", "", ""), null)){
        // datas = cms.getData(cms.getPath().NODE_TASK.getFullPath(plugin.getId(),"",task.substring(5, task.length()), ""),null);
        // if(datas!=null){
        // PluginTask pluginTask = mapper.readValue(datas, PluginTask.class);
        // if(pluginTask.getJobInfo().getId().equals(jobId))
        // return "Job: "+pluginTask.getState().toString();
        // }
        // }
        // }
        //
        // }
        // } catch (IOException ex) {
        // java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return "Job "+jobId+" não encontrado!";
        throw new AvroRemoteException("IMPLEMENTATION REMOVED/COMMENTED");
    }
    
    /**
     * Retorna o status de todos os jobs existentes.
     *
     * @return string com o status de todos os jobs
     * @throws AvroRemoteException
     */
    @Override
    public String statusAllJob() throws AvroRemoteException {
        final StringBuilder allJobs = new StringBuilder();
        int i = 1;
        try {
            final ObjectMapper mapper = new ObjectMapper();
            this.cms.getPath();
            final List<String> pipelines = this.cms.getChildren(Path.PIPELINES.getFullPath(), null);
            for (final String pipeline : pipelines) {
                // verificação dos jobs ainda não escalonados
                // List<String> jobs = cms.getChildren(cms.getPath().JOBS.getFullPath("", "", "", pipeline), null);
                // if(jobs!=null && !jobs.isEmpty()){
                // for(String job : jobs){
                // String jobData = cms.getData(cms.getPath().PREFIX_JOB.getFullPath("","",job.substring(4,job.length()), pipeline),null);
                // if(jobData!=null){
                // JobInfo jobInfo = mapper.readValue(jobData, JobInfo.class);
                // allJobs.append(i).append(" - Job ").append(jobInfo.getId()).append(" Ainda não escalonado.\n ");
                // }
                // i++;
                // }
                // }
                allJobs.append(i).append(" - Pipeline ").append(pipeline).append(" Ainda não escalonado.\n ");
                i++;
            }
            
            // verificação dos jobs escalonados
            String datasTask = null;
            for (final PluginInfo plugin : this.storageService.getPeers().values()) {
                this.cms.getPath();
                for (final String task : this.cms.getChildren(Path.TASKS.getFullPath(plugin.getId()), null)) {
                    this.cms.getPath();
                    datasTask = this.cms.getData(Path.NODE_TASK.getFullPath(plugin.getId(), task.substring(5, task.length())), null);
                    if (datasTask != null) {
                        final PluginTask pluginTask = mapper.readValue(datasTask, PluginTask.class);
                        allJobs.append(i).append(" - Job ").append(pluginTask.getId().toString()).append(" : ").append(pluginTask.getState().toString()).append("\n ");
                    }
                    i++;
                }
            }
        } catch (final IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return allJobs.toString().isEmpty() ? "Não existem jobs." : "Jobs :\n " + allJobs;
    }
    
    /**
     * Retorna uma lista com o nome dos arquivos pertencentes a toda a
     * federação.
     *
     * @return lista com nome dos arquivos
     * @throws AvroRemoteException
     */
    @Override
    public List<String> listFilesName() throws AvroRemoteException {
        final ArrayList<String> listFile = new ArrayList<>();
        try {
            for (final Collection<String> collection : this.storageService.getFiles().values()) {
                listFile.removeAll(collection);
                listFile.addAll(collection);
            }
        } catch (final IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return listFile;
    }
    
    /**
     *
     * @param filename
     */
    @Override
    public void decryptPluginFile(String filename) {
        try {
            final String path = BioNimbusConfig.get().getDataFolder();
            final AESEncryptor aes = new AESEncryptor();
            // Not decrypt inputfiles.txt
            // if(!filename.contains("inputfiles.txt")) {
            // TO-DO: Remove comment after William Final Commit
            // aes.decrypt(path + filename);
            // }
        } catch (final Exception ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *
     * @param fileName
     * @return lista com nome dos arquivos
     * @throws AvroRemoteException
     */
    @Override
    public String getFileHash(String fileName) throws org.apache.avro.AvroRemoteException {
        try {
            final String path = BioNimbusConfig.get().getDataFolder();
            final String hash = HashUtil.computeNativeSHA3(path + fileName);
            return hash;
        } catch (final InterruptedException | IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     *
     * @return @throws AvroRemoteException
     */
    @Override
    public List<br.unb.cic.bionimbuz.avro.gen.PluginFile> listFiles() throws AvroRemoteException {
        
        final HashSet<br.unb.cic.bionimbuz.avro.gen.PluginFile> listFiles = new HashSet<>();
        br.unb.cic.bionimbuz.avro.gen.PluginFile file = null;
        for (final PluginInfo plugin : this.discoveryService.getPeers().values()) {
            for (final PluginFile fileInfo : this.storageService.getFilesPeer(plugin.getId())) {
                file = new br.unb.cic.bionimbuz.avro.gen.PluginFile();
                file.setId(fileInfo.getId());
                file.setName(fileInfo.getName());
                // retorno com getName para o path porque avro não reconhece barra(/), adicionar data-folder/
                // ao receber o retono deste método
                file.setHash(fileInfo.getHash());
                file.setPath(fileInfo.getName());
                file.setPluginId(fileInfo.getPluginId());
                file.setSize(fileInfo.getSize());
                listFiles.add(file);
            }
        }
        final List<br.unb.cic.bionimbuz.avro.gen.PluginFile> listFile = new ArrayList<>(listFiles);
        
        return listFile;
    }
    
    /**
     *
     * @param pluginId
     * @return
     * @throws AvroRemoteException
     */
    @Override
    public List<br.unb.cic.bionimbuz.avro.gen.PluginFile> listFilesPlugin(String pluginId) throws AvroRemoteException {
        final HashSet<br.unb.cic.bionimbuz.avro.gen.PluginFile> listFiles = new HashSet<>();
        br.unb.cic.bionimbuz.avro.gen.PluginFile file;
        for (final PluginFile fileInfo : this.storageService.getFilesPeer(pluginId)) {
            file = new br.unb.cic.bionimbuz.avro.gen.PluginFile();
            file.setId(fileInfo.getId());
            file.setName(fileInfo.getName());
            file.setPath(fileInfo.getName());
            file.setPluginId(fileInfo.getPluginId());
            file.setSize(fileInfo.getSize());
            file.setHash(fileInfo.getHash());
            listFiles.add(file);
        }
        final List<br.unb.cic.bionimbuz.avro.gen.PluginFile> listFile = new ArrayList<>(listFiles);
        
        return listFile;
    }
    
    /**
     * Retorna o ip que contém o arquivo informado @param file. Se não encontrar
     * o arquivo retorna null
     *
     * @param file
     *            - Nome do arquivo requisitado
     * @return - Ip de onde o arquivo se encontra, ou caso não encontre retorna
     *         null;
     */
    @Override
    public String getIpFile(String file) {
        
        String destino = "";
        try {
            destino = this.storageService.getIpContainsFile(file);
        } catch (final IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return destino;
    }
    
    @Override
    public List<String> listServices() throws AvroRemoteException {
        final Collection<PluginInfo> list = this.discoveryService.getPeers().values();
        final List<String> listNameIdService = new ArrayList<>();
        for (final PluginInfo plugin : list) {
            for (final PluginService pluginService : plugin.getServices()) {
                if (!listNameIdService.contains(pluginService.toString())) {
                    listNameIdService.add(pluginService.toString());
                }
            }
        }
        
        return listNameIdService;
    }
    
    /**
     * Altera a política de escalonamento para todas os jobs. E retorna a
     * política de escalonamento alterado ou em uso.
     *
     * @param numPolicy,
     *            numero da política de escalonamento desejada. 0-
     *            AcoSched (Padrão), 1- AHPPolicy, 2- RRPolicy. Quando valor for -1 deve
     *            retornar qual a política de escalonamento está sendo utilizada.
     * @return política de escalonamento
     */
    @Override
    public String schedPolicy(int numPolicy) {
        // //verifica se escolher informar a política ou identificar qual é a política
        // if(numPolicy==-1){
        // numPolicy = new Integer(cms.getData(cms.getPath().JOBS.toString(), null));
        // }else{
        // Integer policy = numPolicy;
        // if(cms.getZNodeExist(cms.getPath().JOBS.toString(), false)){
        // cms.setData(cms.getPath().JOBS.toString(), policy.toString());
        // }else{
        // return "\nNão foi possível alterar política de escalonamento. Tente mais tarde.";
        // }
        // }
        //
        // StringBuilder politicys = new StringBuilder();
        // List<SchedPolicy> listPolicy = SchedPolicy.getInstances();
        // for(SchedPolicy policy:listPolicy){
        // politicys.append("\n ").append(policy.getPolicyName());
        // }
        //
        //
        // return "\nPolítica Atual: "+listPolicy.get(numPolicy).getPolicyName()+"\n\nPolíticas Disponíveis: "+politicys;
        throw new UnsupportedOperationException("METOD COMMENTED");
    }
    
    @Override
    public String startJobName(String param, String ip) throws AvroRemoteException {
        // final String path = "/jobs/job_";
        // JobInfo job = new JobInfo();
        // String params[] = param.split(" ");
        // String jobId = params[0];
        // int i=1;
        //
        // job.setServiceId(Long.parseLong(jobId));
        // job.setLocalId(ip);
        // job.setTimestamp(System.currentTimeMillis());
        // while (i < params.length) {
        // if (i == 1) {
        // job.setArgs(params[i]);
        // i++;
        // } else if (params[i].equals("-i")) {
        // i++;
        // while (i < params.length && !params[i].equals("-o")) {
        // //verifica a existência dos arquivos de entrada na federação
        // if(!listFilesName().contains(params[i]))
        // return "Job não foi escalonado, arquivo de entrada não existe.";
        //
        // job.addInput(params[i], getPluginFile(params[i]).getSize());
        // i++;
        // }
        // } else if (params[i].equals("-o")) {
        // i++;
        // while (i < params.length) {
        // job.addOutput(params[i]);
        // i++;
        // }
        // }
        // }
        // LOGGER.info("Tempo de inicio do job -"+ job.getOutputs()+"- MileSegundos: " + job.getTimestamp());
        // //inclusão do job para ser escalonado
        // cms.createZNode(CreateMode.PERSISTENT, path+job.getId(), job.toString());
        //
        // return "Job enviado para o escalonamento, Id : "+job.getId()+".\nAguarde...";
        throw new UnsupportedOperationException("Function commented. Also, if needed, this must be updated to new pipeline model");
    }
    
    /**
     * Create the workflow and users infos on zookeeper
     * @param workflow
     * @return
     * @throws AvroRemoteException 
     */
    @Override
    public String startWorkflow(Workflow workflow) throws AvroRemoteException {
        // generate pipeline register
        this.schedService.registerPipeline(workflow);
        // Create /users
        this.userController.registerUserWorkflow(workflow); 
        ArrayList<br.unb.cic.bionimbuz.model.Instance> instList = new ArrayList<>();
           
        for(br.unb.cic.bionimbuz.avro.gen.Instance iAvro : workflow.getIntancesWorkflow()){
            br.unb.cic.bionimbuz.model.Instance i = new br.unb.cic.bionimbuz.model.Instance(iAvro);
            instList.add(i);
        }
        //Compara as instancias para cada workflow
        this.slaController.compareHardware(instList, workflow.getUserId(), workflow.getId());
        return "Pipeline enviado para o escalonamento. Aguarde...";
    }
    
    private br.unb.cic.bionimbuz.avro.gen.PluginFile getPluginFile(String fileName) {
        try {
            for (final br.unb.cic.bionimbuz.avro.gen.PluginFile file : this.listFiles()) {
                if (file.getName().equals(fileName)) {
                    return file;
                }
            }
        } catch (final AvroRemoteException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    @Override
    public String cancelJob(String jobID) throws AvroRemoteException {
        // schedService.cancelJob(jobID);
        return "Not enabled";
    }
    
    /**
     * Método RPC que pega os peers do zoonimbus e retorna uma lista do tipo
     * NodeInfo
     *
     * @return lista de NodeInfo PeerId, Address, Freesize;
     * @throws AvroRemoteException
     */
    @Override
    public synchronized List<NodeInfo> getPeersNode() throws AvroRemoteException {
        
        NodeInfo nodeaux;
        this.nodes.clear();
        for (final PluginInfo info : this.discoveryService.getPeers().values()) {
            nodeaux = new NodeInfo();
            if (info != null) {
                final String address = info.getHost().getAddress();
                nodeaux.setAddress(address);
                
                nodeaux.setPeerId(info.getId());
                nodeaux.setFreesize(info.getFsFreeSize());
                this.nodes.put(address, nodeaux);
            }
        }
        
        return new ArrayList<>(this.nodes.values());
    }
    
    /**
     * Passa PluginList para StorageService aqui
     *
     * @param list
     * @return bestnodes lista do tipo NodeInfo, retornando os melhores nós da
     *         federação
     * @throws AvroRemoteException
     */
    @Override
    public List<NodeInfo> callStorage(List<NodeInfo> list) throws AvroRemoteException {
        
        final List<NodeInfo> bestnodes = this.storageService.bestNode(list);
        return bestnodes;
    }
    
    /**
     * Método que cria o znode do arquivo no diretório /pending_save/file_"id_do
     * arquivo" com as informações de arquivos que clientes querem enviar;
     *
     * @param file
     *            informações do arquivo:id,nome, tamanho e hash
     * @param kindString
     *            Tipo de serviço que está requisitando o arquivo
     */
    @Override
    public void setFileInfo(FileInfo file, String kindString) {
        final PluginFile filePlugin = new PluginFile(file);
        filePlugin.setService(kindString);
        // *Alterar depois caminho para o zookeeperservice
        // verificar se a pasta pending_save existe
        this.storageService.setPendingFile(filePlugin);
    }
    
    /**
     * Método RPC que retorna o tamanho do arquivo verificado em outro peer;
     *
     * @param file
     *            Nome do arquivo
     * @return size o tamanho do arquivo
     */
    @Override
    public long checkFileSize(String file) {
        
        final long size = this.storageService.getFileSize(file);
        
        return size;
    }
    
    /**
     * Método avro que chama o método fileuploaded da storage para avisar que o
     * arquivo foi enviado.
     *
     * @param fileSucess
     *            informações do arquivo:id,nome e tamanho
     * @param dest
     *            lista com os plugins de destino
     * @return
     */
    @Override
    public String fileSent(FileInfo fileSucess, List<String> dest) {
        final PluginFile file = new PluginFile(fileSucess);
        file.setPluginId(dest);
        file.setPath(BioNimbusConfig.get().getDataFolder() + file.getName());
        String retorno = "File uploaded.";
        try {
            retorno = this.storageService.fileUploaded(file);
            return retorno;
        } catch (KeeperException | InterruptedException | IOException | NoSuchAlgorithmException | SftpException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }
    
    /**
     * Método que notifica o peer para fazer a replicação
     *
     * @param filename
     *            nome do arquivo a ser replicado
     * @param address
     *            endereço do peer que possui o arquivo
     */
    @Override
    public void notifyReply(String filename, String address) {
        try {
            this.storageService.replication(filename, address);
        } catch (IOException | JSchException | SftpException | NoSuchAlgorithmException | InterruptedException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Método que verifica se o arquivo existe nó de destino
     *
     * @param fileSucess
     * @param dest
     *            lista de ids
     * @return
     */
    @Override
    public boolean verifyFile(FileInfo fileSucess, List dest) {
        final PluginFile fileS = new PluginFile(fileSucess);
        fileS.setPluginId(dest);
        return this.storageService.checkFilePeer(fileS);
    }
    
    @Override
    public void setWatcher(String idPlugin) {
        // storageService.starWatchers(idPlugin);
    }
    
    /**
     * Send a FileInfo to ZooKeeper.
     *
     * @param path
     * @param file
     * @return
     * @throws AvroRemoteException
     */
    @Override
    public boolean uploadFile(String path, FileInfo file) throws AvroRemoteException {
        try {
            this.storageService.writeFileToZookeeper(path, file);
            
            return true;
        } catch (IOException | JSchException | SftpException | NoSuchAlgorithmException | InterruptedException e) {
            e.printStackTrace();
            
            return false;
        }
    }
    
    /**
     * Iterates over the peers to find a file. If found, return peer_id;
     *
     * @param filename
     * @return
     * @throws AvroRemoteException
     */
    @Override
    public br.unb.cic.bionimbuz.avro.gen.PluginFile getFileFromPeers(String filename) throws AvroRemoteException {
        final br.unb.cic.bionimbuz.avro.gen.PluginFile pluginFile = this.storageService.getFileInfoByFilename(filename);
        
        if (pluginFile == null) {
            final br.unb.cic.bionimbuz.avro.gen.PluginFile pfile = new br.unb.cic.bionimbuz.avro.gen.PluginFile();
            
            pfile.setName("");
            
            return pfile;
        } else {
            return pluginFile;
        }
    }
}
