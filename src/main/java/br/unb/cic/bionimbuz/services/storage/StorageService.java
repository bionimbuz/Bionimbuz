/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.services.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.avro.AvroRemoteException;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import br.unb.cic.bionimbuz.avro.gen.FileInfo;
import br.unb.cic.bionimbuz.avro.gen.NodeInfo;
import br.unb.cic.bionimbuz.avro.rpc.AvroClient;
import br.unb.cic.bionimbuz.avro.rpc.RpcClient;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.plugin.PluginFile;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.security.HashUtil;
import br.unb.cic.bionimbuz.security.Integrity;
import br.unb.cic.bionimbuz.services.AbstractBioService;
import br.unb.cic.bionimbuz.services.UpdatePeerData;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.services.sched.SchedService;
import br.unb.cic.bionimbuz.services.storage.bandwidth.BandwidthCalculator;
import br.unb.cic.bionimbuz.services.storage.policy.StoragePolicy;
import br.unb.cic.bionimbuz.services.storage.policy.impl.BioCirrusPolicy;
import br.unb.cic.bionimbuz.toSort.Listeners;
import br.unb.cic.bionimbuz.utils.Nmap;
import br.unb.cic.bionimbuz.utils.Put;

@Singleton
public class StorageService extends AbstractBioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("StorageService-%d").build());

    private Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<>();
    private final Map<String, PluginFile> savedFiles = new ConcurrentHashMap<>();
    // private Set<String> pendingSaveFiles = new HashSet<String>();
    private final File dataFolder = new File(BioNimbusConfig.get().getDataFolder());
    private final Double MAXCAPACITY = 0.9;
    private final int PORT = 8080;
    private final int REPLICATIONFACTOR = 2;

    @Inject
    public StorageService(final CloudMessageService cms, MetricRegistry metricRegistry) {

        Preconditions.checkNotNull(cms);
        this.cms = cms;
    }

    @Override
    public void run() {
    }

    /**
     * Método que inicia a storage
     *
     * @param config
     * @param listeners
     */
    @Override
    public void start(List<Listeners> listeners) {
        this.listeners = listeners;
        if (listeners != null) {
            listeners.add(this);
        }
        // Criando pastas zookeeper para o módulo de armazenamento
        if (!this.cms.getZNodeExist(Path.PENDING_SAVE.getFullPath(), null)) {
            this.cms.createZNode(CreateMode.PERSISTENT, Path.PENDING_SAVE.getFullPath(), null);
        }
        if (!this.cms.getZNodeExist(Path.FILES.getFullPath(BioNimbusConfig.get().getId()), null)) {
            this.cms.createZNode(CreateMode.PERSISTENT, Path.FILES.getFullPath(BioNimbusConfig.get().getId()), "");
        }

        // watcher para verificar se um pending_save foi lançado
        this.cms.getChildren(Path.PENDING_SAVE.getFullPath(), new UpdatePeerData(this.cms, this, null));
        this.cms.getChildren(Path.PEERS.getFullPath(), new UpdatePeerData(this.cms, this, null));

        // NECESSARIO atualizar a lista de arquivo local , a lista do zookeeper com os arquivos locais.
        // checkFiles();
        this.checkPeers();
        try {
            if (this.getPeers().size() != 1) {
                this.checkReplicationFiles();
            }
        } catch (final Exception ex) {
            LOGGER.error("[Exception] - " + ex.getMessage());
        }
        this.executorService.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
        this.listeners.remove(this);
        this.executorService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub
    }

    /**
     * Verifica os peers(plugins) existentes e adiciona um observador(watcher)
     * no zNode STATUS de cada plugin.
     */
    public void checkPeers() {
        for (final PluginInfo plugin : this.getPeers().values()) {
            if (this.cms.getZNodeExist(Path.STATUS.getFullPath(plugin.getId()), null)) {
                this.cms.getData(Path.STATUS.getFullPath(plugin.getId()), new UpdatePeerData(this.cms, this, null));
            }
        }
    }

    /**
     * Verifica os arquivos que existem no recurso e adiciona no Zookeeper os arquivos novos. Alterado para synchronized
     * para evitar condição de corrida.
     */
    public synchronized void checkFiles() {
        try {
            if (!this.dataFolder.exists()) {
                this.dataFolder.mkdirs();
            }
            this.cms.getChildren(Path.FILES.getFullPath(BioNimbusConfig.get().getId()), new UpdatePeerData(this.cms, this, null));
            for (final File file : this.dataFolder.listFiles()) {
                if (!this.savedFiles.containsKey(file.getName())) {
                    final PluginFile pluginFile = new PluginFile();
                    pluginFile.setId(file.getName());
                    pluginFile.setName(file.getName());
                    pluginFile.setPath(file.getPath());

                    final List<String> listIds = new ArrayList<>();
                    listIds.add(BioNimbusConfig.get().getId());

                    pluginFile.setPluginId(listIds);
                    pluginFile.setSize(file.length());
                    // pluginFile.setHash(Hash.calculateSha3(file.getPath()));
                    // cria um novo znode para o arquivo e adiciona o watcher
                    this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_FILE.getFullPath(BioNimbusConfig.get().getId(), pluginFile.getId()), pluginFile.toString());
                    this.cms.getData(Path.NODE_FILE.getFullPath(BioNimbusConfig.get().getId(), pluginFile.getId()), new UpdatePeerData(this.cms, this, null));

                    this.savedFiles.put(pluginFile.getName(), pluginFile);
                }

            }
        } catch (final Exception ex) {
            LOGGER.error("[Exception] - " + ex.getMessage());
        }
    }

    /**
     * Checa quantas cópias existem de um arquivo, caso existam menos cópias do
     * que REPLICATIONFACTOR inicia a replicação deste arquivo; Este método
     * checa todos os arquivos da federação.
     *
     * @throws Exception
     */
    public void checkReplicationFiles() throws Exception {
        for (final Collection<String> collection : this.getFiles().values()) {
            /*
             * Percorre cada arquivo e o IP que possui ele
             */
            for (final String fileNamePlugin : collection) {
                if (!this.existReplication(fileNamePlugin)) {
                    /*
                     * Caso não exista um número de cópias igual a REPLICATIONFACTOR inicia as cópias,
                     * enviando uma RPC para o peer que possui o arquivo, para que ele replique.
                     */
                    final String ipPluginFile = this.getIpContainsFile(fileNamePlugin);
                    if (!ipPluginFile.isEmpty() && !ipPluginFile.equals(BioNimbusConfig.get().getAddress())) {
                        final RpcClient rpcClient = new AvroClient("http", ipPluginFile, this.PORT);
                        rpcClient.getProxy().notifyReply(fileNamePlugin, ipPluginFile);
                        rpcClient.close();
                    } else {
                        this.replication(fileNamePlugin, ipPluginFile);
                    }
                }
            }
        }
    }

    /**
     * Verifica a existência da replicação do arquivo na federação. Se a
     * replicação estiver feita retona true; Fator de replicação igual a 2;
     * Retorna true se existir replicação.
     */
    private boolean existReplication(String fileName) throws IOException {
        int cont = 0;
        for (final Collection<String> collection : this.getFiles().values()) {
            for (final String fileNamePlugin : collection) {
                if (fileName.equals(fileNamePlugin)) {
                    cont++;
                }
            }
        }
        return cont >= this.REPLICATIONFACTOR;
    }

    /**
     * Cria map com endereço dos peers(plugins) e seus respectivos arquivos
     * baseado nos dados do zookeeper.
     *
     * @return map de endereço e lista de arquivos.
     * @throws java.io.IOException
     */
    public Map<String, List<String>> getFiles() throws IOException {
        final Map<String, List<String>> mapFiles = new HashMap<>();
        List<String> listFiles;
        this.checkFiles();

        for (final PluginInfo plugin : this.getPeers().values()) {
            listFiles = new ArrayList<>();
            for (final String file : this.cms.getChildren(Path.FILES.getFullPath(plugin.getId()), new UpdatePeerData(this.cms, this, null))) {
                listFiles.add(file);
            }
            mapFiles.put(plugin.getHost().getAddress(), listFiles);
        }

        return mapFiles;

    }

    /**
     * Metodo para pegar o Ip de cada peer na federação e verificar em qual peer
     * o arquivo está, se o arquivo for encontrado retorna o Ip do peer, caso
     * contrário retorna null.
     *
     * @param file
     * @return Ip que possui o arquivo ou null
     * @throws java.io.IOException
     */
    public String getIpContainsFile(String file) throws IOException {
        List<String> listFiles;
        // NECESSARIO atualizar a lista de arquivo local , a lista do zookeeper com os arquivos locais. Não é feito em nenhum momento
        // caso não seja chamado a checkFiles();
        this.checkFiles();

        for (final Iterator<PluginInfo> it = this.getPeers().values().iterator(); it.hasNext();) {
            final PluginInfo plugin = it.next();
            listFiles = this.cms.getChildren(Path.FILES.getFullPath(plugin.getId()), null);
            for (final String checkfile : listFiles) {
                if (file.equals(checkfile)) {
                    return plugin.getHost().getAddress();
                }
            }
        }
        return "";
    }

    /**
     * Retorna o tamanho do arquivo, dado o nome do mesmo.
     * NOTE: listFiles never used. Revise this code.
     * Refactor message of integrity verification
     *
     * @param file
     *            O nome do arquivo
     * @return O tamanho do arquivo
     */
    public long getFileSize(String file) {

        try {
            for (final Iterator<PluginInfo> it = this.getPeers().values().iterator(); it.hasNext();) {
                final PluginInfo plugin = it.next();
                final PluginFile files = new ObjectMapper().readValue(this.cms.getData(Path.NODE_FILE.getFullPath(plugin.getId(), file), null), PluginFile.class);
                return files.getSize();
            }
        } catch (final IOException ex) {
            LOGGER.error("[IOException] - " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Recebe uma list com todos os peers da federação e seta o custo de
     * armazenamento em cada plugin
     *
     * @param list
     *            - Lista com todos os plugins da federação
     * @return - Lista com todos os plugins com seus custos de armazenamento
     *         inseridos
     */
    public List<NodeInfo> bestNode(List<NodeInfo> list) {

        List<NodeInfo> plugins;
        this.cloudMap = this.getPeers();
        for (final NodeInfo node : list) {
            this.cloudMap.get(node.getPeerId()).setLatency(node.getLatency());
            this.cloudMap.get(node.getPeerId()).setFsFreeSize(node.getFreesize());
        }
        // TODO: Permitir a escolha entre a BioCirrus e a ZooClouS
        final StoragePolicy policy = new BioCirrusPolicy();
        /*
         * Dentro da Storage Policy é feito o ordenamento da list de acordo com o custo de armazenamento
         */
        plugins = policy.calcBestCost(this.cms, this.cloudMap.values());

        return plugins;
    }

    /**
     * Verifica se um arquivo existe em um peer e seta o seu Znode no Zookeeper
     *
     * @param file
     *            - Arquivo a ser verifcado
     * @return true caso o arquivo exista e tenha sido setado
     */
    public boolean checkFilePeer(PluginFile file) {
        LOGGER.info("Verifying if file (filename=" + file.getName() + ") exists on peer");

        final File localFile = new File(BioNimbusConfig.get() + file.getName());

        if (localFile.exists()) {
            this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_FILE.getFullPath(BioNimbusConfig.get().getId(), file.getId()), file.toString());
            this.cms.getData(Path.NODE_FILE.getFullPath(BioNimbusConfig.get().getId(), file.getId()), new UpdatePeerData(this.cms, this, null));
            return true;
        }

        LOGGER.info("File not found on Peer");
        return false;
    }

    /**
     * Método que manda o comando dizendo que o arquivo foi upado com o intuito
     * de replicar esse arquivo pelos nós.
     *
     * @param fileUploaded
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     * @throws java.security.NoSuchAlgorithmException
     * @throws com.jcraft.jsch.SftpException
     */
    public synchronized String fileUploaded(PluginFile fileUploaded) throws KeeperException, InterruptedException, IOException, NoSuchAlgorithmException, SftpException {
        LOGGER.info("Checking if there is request on PENDING_SAVE: " + fileUploaded.getName());

        Boolean successUpload = false;
        if (this.cms.getZNodeExist(Path.NODE_PENDING_FILE.getFullPath(fileUploaded.getId()), null)) {
            String ipPluginFile;
            ipPluginFile = this.getIpContainsFile(fileUploaded.getName());
            final FileInfo file = new FileInfo();
            file.setId(fileUploaded.getId());
            file.setName(fileUploaded.getName());
            file.setSize(fileUploaded.getSize());
            file.setHash(fileUploaded.getHash());
            String idPluginFile = null;

            for (final String idPlugin : fileUploaded.getPluginId()) {
                idPluginFile = idPlugin;
                break;
            }

            // Verifica se a máquina que recebeu essa requisição não é a que está armazenando o arquivo
            if (!fileUploaded.getPluginId().contains(BioNimbusConfig.get().getId())) {
                for (final PluginInfo plugin : this.getPeers().values()) {
                    if (plugin.getId().equals(fileUploaded.getPluginId().get(0))) {
                        ipPluginFile = plugin.getHost().getAddress();
                    }
                }
                final RpcClient rpcClient = new AvroClient("http", ipPluginFile, this.PORT);
                final String filePeerHash = rpcClient.getProxy().getFileHash(fileUploaded.getName());

                // Verifica se o arquivo foi corretamente transferido ao nó.
                if (Integrity.verifyHashes(filePeerHash, fileUploaded.getHash())) {
                    successUpload = true;
                    try {
                        if (rpcClient.getProxy().verifyFile(file, fileUploaded.getPluginId()) && this.cms.getZNodeExist(Path.NODE_FILE.getFullPath(idPluginFile, fileUploaded.getId()), null)) {
                            // Remova o arquivo do PENDING FILE já que ele foi upado
                            this.cms.delete(Path.NODE_PENDING_FILE.getFullPath(fileUploaded.getId()));
                        }
                        rpcClient.close();
                    } catch (final Exception ex) {
                        LOGGER.error("[Exception] - " + ex.getMessage());
                    }
                }
            } else {
                if (this.checkFilePeer(fileUploaded)) {
                    final String filePeerHash = HashUtil.computeNativeSHA3(BioNimbusConfig.get().getDataFolder() + fileUploaded.getName());

                    // Verifica se o arquivo foi corretamente transferido ao peer.
                    if (Integrity.verifyHashes(filePeerHash, fileUploaded.getHash())) {
                        successUpload = true;
                        if (this.cms.getZNodeExist(Path.NODE_FILE.getFullPath(idPluginFile, fileUploaded.getId()), null) && !this.existReplication(file.getName())) {
                            try {
                                this.replication(file.getName(), BioNimbusConfig.get().getAddress());
                                if (this.existReplication(file.getName())) {
                                    // Remova o arquivo do PENDING FILE já que ele foi upado
                                    this.cms.delete(Path.NODE_PENDING_FILE.getFullPath(fileUploaded.getId()));
                                } else {
                                    LOGGER.info("Replication failed! File wasn't replicated...");
                                }
                            } catch (final JSchException ex) {
                                LOGGER.error("[JSchException] - " + ex.getMessage());
                            }
                        }
                    }
                }
            }
        } else {
            LOGGER.info("File not found on pending files");
        }

        if (successUpload) {
            LOGGER.info("File integrity verified: File uploaded correctly");

            return "File integrity verified: File uploaded correctly.";
        } else {
            LOGGER.error("File integrity verified: Error on file uploading.");

            return "File integrity verified: Error on file uploading.";
        }
    }

    /**
     * Metodo que checa os znodes filhos da pending_save, para replica-lós
     *
     * @throws java.io.IOException
     * @throws java.security.NoSuchAlgorithmException
     * @throws InterruptedException
     */
    public void checkingPendingSave() throws IOException, NoSuchAlgorithmException, InterruptedException {

        final ObjectMapper mapper = new ObjectMapper();
        int cont = 0;
        final List<String> pendingSave = this.cms.getChildren(Path.PENDING_SAVE.getFullPath(), null);
        // pendingSaveFiles.addAll(pendingSave);
        for (final String files : pendingSave) {
            try {
                final String data = this.cms.getData(Path.NODE_PENDING_FILE.getFullPath(files.substring(13, files.length())), null);
                // verifica se arquivo existe
                if (data == null || data.trim().isEmpty()) {
                    LOGGER.info("========> There is no data for Path: " + Path.PENDING_SAVE.getFullPath());

                    continue;
                }
                final PluginFile fileplugin = mapper.readValue(data, PluginFile.class);

                // Verifica se é um arquivo de saída de uma execução e se o arquivo foi gerado nesse recurso
                if (fileplugin.getService() != null && fileplugin.getService().equals(SchedService.class.getSimpleName()) && fileplugin.getPluginId().get(0).equals(BioNimbusConfig.get().getId())) {
                    // Adiciona o arquivo a lista do zookeeper
                    this.checkFiles();
                }
                while (cont < 6) {
                    if (fileplugin.getPluginId().size() == this.REPLICATIONFACTOR) {
                        this.cms.delete(Path.PENDING_SAVE.getFullPath(fileplugin.getId()));
                        break;
                    }
                    final String address = this.getIpContainsFile(fileplugin.getName());
                    if (!address.isEmpty() && !address.equals(BioNimbusConfig.get().getAddress())) {
                        final RpcClient rpcClient = new AvroClient("http", address, this.PORT);
                        rpcClient.getProxy().notifyReply(fileplugin.getName(), address);
                        try {
                            rpcClient.close();
                            if (this.existReplication(fileplugin.getName())) {
                                this.cms.delete(Path.NODE_PENDING_FILE.getFullPath(fileplugin.getId()));
                                break;
                            } else {
                                cont++;
                            }
                        } catch (final Exception ex) {
                            LOGGER.error("[Exception] - " + ex.getMessage());
                        }
                    } else {
                        try {
                            this.replication(fileplugin.getName(), address);
                        } catch (final JSchException ex) {
                            LOGGER.error("[JSchException] - " + ex.getMessage());
                        } catch (final SftpException ex) {
                            LOGGER.error("[SftpException] - " + ex.getMessage());
                        }
                        if (this.existReplication(fileplugin.getName())) {
                            this.cms.delete(Path.NODE_PENDING_FILE.getFullPath(fileplugin.getId()));
                            break;
                        }
                    }
                }
            } catch (final IOException ex) {
                LOGGER.error("[IOException] - " + ex.getMessage());
            }
            // verifica se exite replicação quando houver mais de um peer
            if (this.getPeers().size() != 1) {
                this.existReplication(files);
            }
        }
    }

    /**
     * Realiza a replicação de arquivos, sejam eles enviados pelo cliente ou
     * apenas gerados na própria federação
     *
     * @param filename
     *            - nome do arquivo
     * @param address
     *            - endereço do peer que possui o arquivo
     * @throws IOException
     * @throws JSchException
     * @throws SftpException
     * @throws java.io.FileNotFoundException
     * @throws java.security.NoSuchAlgorithmException
     * @throws InterruptedException
     */
    public synchronized void replication(String filename, String address) throws IOException, JSchException, SftpException, FileNotFoundException, NoSuchAlgorithmException, InterruptedException {
        LOGGER.info("Replicating file (filename=" + filename + ") from peer (peer=" + address + ")");

        List<NodeInfo> pluginList = new ArrayList<>();
        final List<String> idsPluginsFile = new ArrayList<>();
        final File file = new File(BioNimbusConfig.get().getDataFolder() + filename);

        final int filesreplicated = 1;

        // Verifica se o arquivo existe no peer
        if (file.exists()) {
            final FileInfo info = new FileInfo();
            info.setId(file.getName());
            info.setName(file.getName());
            info.setSize(file.length());
            info.setHash(HashUtil.computeNativeSHA3(file.getAbsolutePath()));

            final PluginFile pluginFile = new PluginFile(info);
            /*
             * PLuginList ira receber a lista dos Peers disponiveis na federação
             * e que possuem espaço em disco para receber o arquivo a ser replicado
             */
            pluginFile.setPath(BioNimbusConfig.get().getDataFolder() + info.getName());
            NodeInfo no = null;

            /*
             * While para que o peer pegue o próprio endereço e ele seja removido da lista de peers,
             * isso é feito para evitar que ele tente replicar
             * o arquivo para ele mesmo.
             */
            for (final NodeInfo node : this.getNodeDisp(info.getSize())) {
                if (node.getAddress().equals(address)) {
                    no = node;
                    break;
                }
            }
            if (no != null) {
                pluginList.remove(no);
                idsPluginsFile.add(BioNimbusConfig.get().getId());
                pluginList = new ArrayList<>(this.bestNode(pluginList));
                for (final NodeInfo curr : pluginList) {
                    if (no.getAddress().equals(curr.getAddress())) {
                        no = curr;
                        break;
                    }
                }

                pluginList.remove(no);
            }
            pluginList = new ArrayList<>(this.bestNode(pluginList));
            pluginList.remove(no);
            final Iterator<NodeInfo> bt = pluginList.iterator();
            while (bt.hasNext() && filesreplicated != this.REPLICATIONFACTOR) {
                final NodeInfo node = bt.next();
                if (!node.getAddress().equals(address)) {
                    // Descoberto um peer disponivel, tenta enviar o arquivo
                    final Put conexao = new Put(node.getAddress(), this.dataFolder + "/" + info.getName());
                    if (conexao.startSession()) {
                        idsPluginsFile.add(node.getPeerId());

                        pluginFile.setPluginId(idsPluginsFile);

                        final RpcClient rpcClient = new AvroClient("http", node.getAddress(), this.PORT);
                        final String fileReplicatedHash = rpcClient.getProxy().getFileHash(pluginFile.getName());
                        // rpcClient.close();

                        // Verifica se o arquivo foi corretamente transferido ao nó.
                        if (Integrity.verifyHashes(pluginFile.getHash(), fileReplicatedHash)) {
                            // Com o arquivo enviado, seta os seus dados no Zookeeper
                            for (final String idPlugin : idsPluginsFile) {
                                if (this.cms.getZNodeExist(Path.NODE_FILE.getFullPath(idPlugin, filename), null)) {
                                    this.cms.setData(Path.NODE_FILE.getFullPath(idPlugin, filename), pluginFile.toString());
                                } else {
                                    this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_FILE.getFullPath(idPlugin, filename), pluginFile.toString());
                                }
                                this.cms.getData(Path.NODE_FILE.getFullPath(idPlugin, filename), new UpdatePeerData(this.cms, this, null));
                            }
                        } else {
                            LOGGER.info("Error replicating the file to the peer (peer=" + node.getAddress() + ")");
                        }
                    }
                }
            }
        }
    }

    /**
     * Pega uma lista com todos os peers da federação e separa eles de acordo
     * com o tamanho do arquivo, criando uma lista somente com os peers que
     * possuem condições de receber o arquivo
     *
     * @param lengthFile
     * @return - Lista com peers que podem receber o arquivo
     */
    public List<NodeInfo> getNodeDisp(long lengthFile) {
        final List<NodeInfo> nodesdisp = new ArrayList<>();
        final Collection<PluginInfo> cloudPlugin = this.getPeers().values();
        nodesdisp.clear();
        for (final PluginInfo plugin : cloudPlugin) {
            try {
                final NodeInfo node = new NodeInfo();

                if ((long) (plugin.getFsFreeSize() * this.MAXCAPACITY) > lengthFile && plugin.getId().equals(BioNimbusConfig.get().getId())) {
                    node.setLatency(Ping.calculo(plugin.getHost().getAddress()));
                    if (node.getLatency().equals(Double.MAX_VALUE)) {
                        node.setLatency(Nmap.nmap(plugin.getHost().getAddress()));
                    }
                    node.setBandwidth(BandwidthCalculator.linkSpeed(plugin.getHost().getAddress(), node.getLatency()));
                    node.setAddress(plugin.getHost().getAddress());
                    node.setFreesize(plugin.getFsFreeSize());
                    node.setPeerId(plugin.getId());
                    nodesdisp.add(node);
                }
            } catch (final IOException ex) {
                LOGGER.error("[IOException] - " + ex.getMessage());
            } catch (final InterruptedException ex) {
                LOGGER.error("[InterruptedException] - " + ex.getMessage());
            }
        }
        return nodesdisp;
    }

    /**
     * Seta no Zookeeper os dados de um arquivo que foi requisitado por um
     * cliente para ser submetido na federação
     *
     * @param file
     *            - Arquivo a ser submetido
     */
    public void setPendingFile(PluginFile file) {
        this.cms.createZNode(CreateMode.PERSISTENT, Path.NODE_PENDING_FILE.getFullPath(file.getId()), file.toString());
    }

    /**
     * Cria uma Map com o ID de um peer e seus respectivos arquivos
     *
     * @param pluginId
     *            id do plugin para pegar os arquivos do plugin
     * @return Map com os plugins e seus arquivos
     */
    public List<PluginFile> getFilesPeer(String pluginId) {
        List<String> children;
        final List<PluginFile> filesPeerSelected = new ArrayList<>();
        // NECESSARIO atualizar a lista de arquivo local , a lista do zookeeper com os arquivos locais. Não é feito em nenhum momento
        // caso não seja chamado a checkFiles();
        this.checkFiles();
        try {
            children = this.cms.getChildren(Path.FILES.getFullPath(pluginId), null);
            for (final String fileId : children) {
                final String fileName = fileId.substring(5, fileId.length());
                final ObjectMapper mapper = new ObjectMapper();
                final PluginFile file = mapper.readValue(this.cms.getData(Path.NODE_FILE.getFullPath(pluginId, fileName), null), PluginFile.class);
                filesPeerSelected.add(file);
            }
        } catch (final IOException ex) {
            LOGGER.error("[IOException] - " + ex.getMessage());
        }

        return filesPeerSelected;
    }

    @Override
    public void verifyPlugins() {
        final Collection<PluginInfo> temp = this.getPeers().values();
        temp.removeAll(this.cloudMap.values());
        for (final PluginInfo plugin : temp) {
            if (this.cms.getZNodeExist(Path.STATUS.getFullPath(plugin.getId(), null, null), null)) {
                this.cms.getData(Path.STATUS.getFullPath(plugin.getId()), new UpdatePeerData(this.cms, this, null));
            }
        }
    }

    /**
     * Sends a file to ZooKeeper
     *
     * @param filepath
     * @param fileInfo
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws JSchException
     * @throws java.security.NoSuchAlgorithmException
     * @throws SftpException
     */
    public boolean writeFileToZookeeper(String filepath, FileInfo fileInfo) throws IOException, JSchException, SftpException, NoSuchAlgorithmException, InterruptedException {

        List<NodeInfo> pluginList;
        List<NodeInfo> nodesdisp = new ArrayList<>();
        final RpcClient rpcClient = new AvroClient("http", BioNimbusConfig.get().getAddress(), 8080);
        final File file = new File(filepath);

        // Verifica se o arquivo existe
        if (file.exists()) {
            pluginList = rpcClient.getProxy().getPeersNode();

            // Insere o arquivo na pasta PENDING SAVE do Zookeeper
            rpcClient.getProxy().setFileInfo(fileInfo, "upload!");
            for (final Iterator<NodeInfo> it = pluginList.iterator(); it.hasNext();) {
                final NodeInfo plugin = it.next();

                // Adiciona na lista de possiveis peers de destino somente os que possuem espaço livre para receber o arquivo
                if ((long) (plugin.getFreesize() * this.MAXCAPACITY) > fileInfo.getSize()) {
                    plugin.setLatency(Ping.calculo(plugin.getAddress()));
                    if (plugin.getLatency().equals(Double.MAX_VALUE)) {
                        plugin.setLatency(Nmap.nmap(plugin.getAddress()));
                    }
                    nodesdisp.add(plugin);
                }
            }

            // Retorna a lista dos nos ordenados como melhores, passando a latência calculada
            nodesdisp = new ArrayList<>(rpcClient.getProxy().callStorage(nodesdisp));

            NodeInfo no = null;
            final Iterator<NodeInfo> it = nodesdisp.iterator();

            // Tenta enviar o arquivo para o melhor peer que está na lista
            while (it.hasNext() && no == null) {
                final NodeInfo node = it.next();
                final Put conexao = new Put(node.getAddress(), filepath);
                if (conexao.startSession()) {
                    no = node;
                    break;
                }
            }
            // Conserta o nome do arquivo encriptado
            // TODO: Remove comment after William Final Commit
            // final AESEncryptor aes = new AESEncryptor();
            // aes.setCorrectFilePath(path);
            if (no != null) {
                final List<String> dest = new ArrayList<>();
                dest.add(no.getPeerId());
                nodesdisp.remove(no);

                // Envia RPC para o peer em que está conectado, para que ele sete no Zookeeper os dados do arquivo que foi upado.
                rpcClient.getProxy().fileSent(fileInfo, dest);

                // File uploaded
                LOGGER.info("File uploaded!");
                return true;
            }

        }

        // Upload error
        LOGGER.error("File not found");

        return false;
    }

    /**
     * Cria Map com ID dos plugins e seus respectivos arquivos baseado nos dados
     * do zookeeper.
     *
     * @return Map<Id_Plugin, Lista de Arquivos>
     * @throws java.io.IOException
     */
    public Map<String, List<String>> getAllPluginFiles() throws IOException {
        final Map<String, List<String>> mapFiles = new HashMap<>();
        List<String> listFiles;
        this.checkFiles();

        for (final PluginInfo plugin : this.getPeers().values()) {
            listFiles = new ArrayList<>();
            for (final String file : this.cms.getChildren(Path.FILES.getFullPath(plugin.getId()), new UpdatePeerData(this.cms, this, null))) {
                listFiles.add(file);
            }

            mapFiles.put(plugin.getId(), listFiles);
        }

        return mapFiles;

    }

    /**
     * Returns a FileInfo from a given filename. It searches over the ZooKeeper
     * structure to find the peer and the name.
     *
     * @param filename
     * @return
     */
    public br.unb.cic.bionimbuz.avro.gen.PluginFile getFileInfoByFilename(String filename) {
        try {
            // Map --> {peer_id, {file_1, file_2, file_3, ...}, peer_id, {}...}
            final Map<String, List<String>> map = this.getAllPluginFiles();

            // Iterates over the map
            for (final Map.Entry<String, List<String>> entry : map.entrySet()) {
                LOGGER.info("Iterating over peer: " + entry.getKey());

                // Iterates the list of files of a peer
                for (final String file : entry.getValue()) {
                    LOGGER.info("File: " + file);

                    // Verify if it is that searched
                    if (file.equals(filename)) {
                        // Creates Mapper
                        final ObjectMapper mapper = new ObjectMapper();

                        // Retrives from ZooKeeper
                        final String pFile = this.cms.getData(Path.NODE_FILE.getFullPath(entry.getKey(), file), null);

                        // Convert to PluginFile
                        final br.unb.cic.bionimbuz.avro.gen.PluginFile pluginFile = mapper.readValue(pFile, br.unb.cic.bionimbuz.avro.gen.PluginFile.class);

                        final br.unb.cic.bionimbuz.model.FileInfo fileInfo = new br.unb.cic.bionimbuz.model.FileInfo();
                        fileInfo.setId(pluginFile.getId());
                        fileInfo.setName(pluginFile.getName());
                        fileInfo.setHash(pluginFile.getHash());
                        fileInfo.setSize(pluginFile.getSize());
                        fileInfo.setUploadTimestamp("");

                        // Found -> return peer_id
                        return pluginFile;
                    }
                }
            }

        } catch (final IOException ex) {
            LOGGER.error("Error searching files");
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Método que recebe um evento do zookeeper caso os znodes setados nessa
     * classe sofra alguma alteração, criado, deletado, modificado, trata os
     * eventos de acordo com o tipo do mesmo
     *
     * @param eventType
     */
    @Override
    public void event(WatchedEvent eventType) {
        final String path = eventType.getPath();
        switch (eventType.getType()) {

            case NodeChildrenChanged:
                if (eventType.getPath().equals(Path.PEERS.toString())) {

                    if (this.cloudMap.size() < this.getPeers().size()) {
                        this.verifyPlugins();
                    }
                } else if (eventType.getPath().equals(Path.PENDING_SAVE.toString())) {
                    // chamada para checar a pending_save apenas quando uma alerta para ela for lançado
                    // try{
                    // checkingPendingSave();
                    // }
                    // catch (IOException ex) {
                    // Logger.getLogger(StorageService.class.getName()).log(Level.SEVERE, null, ex);
                    // }
                }
                break;
            case NodeDeleted:
                if (eventType.getPath().contains(Path.STATUS.toString())) {
                    LOGGER.info("Erased ZNode Status");

                    final String peerId = path.substring(12, path.indexOf("/STATUS"));
                    if (this.getPeers().values().size() != 1) {
                        try {
                            if (!this.cms.getZNodeExist(Path.STATUSWAITING.getFullPath(peerId), null)) {
                                this.cms.createZNode(CreateMode.PERSISTENT, Path.STATUSWAITING.getFullPath(peerId), "");
                            }

                            final StringBuilder info = new StringBuilder(this.cms.getData(Path.STATUSWAITING.getFullPath(peerId), null));
                            // verifica se recurso já foi recuperado ou está sendo recuperado por outro recurso
                            if (!info.toString().contains("S") /* && !info.toString().contains("L") */) {

                                // bloqueio para recuperar tarefas sem que outros recursos realizem a mesma operação
                                // cms.setData(Path.STATUSWAITING.getFullPath(peerId), info.append("L").toString());
                                // Verificar pluginid para gravar
                                for (final PluginFile fileExcluded : this.getFilesPeer(peerId)) {
                                    String idPluginExcluded = null;
                                    for (final String idPlugin : fileExcluded.getPluginId()) {
                                        if (peerId.equals(idPlugin) && !idPlugin.equals(BioNimbusConfig.get().getId())) {
                                            idPluginExcluded = idPlugin;
                                            break;
                                        }
                                    }

                                    if (fileExcluded.getPluginId().size() > 1) {
                                        fileExcluded.getPluginId().remove(idPluginExcluded);
                                    }

                                    this.setPendingFile(fileExcluded);
                                    fileExcluded.setService("storagePeerDown");
                                    this.fileUploaded(fileExcluded);
                                }

                                // retira bloqueio de uso e adiciona marcação de recuperação
                                // info.deleteCharAt(info.indexOf("L"));
                                info.append("S");
                                this.cms.setData(Path.STATUSWAITING.getFullPath(peerId), info.toString());

                                // nao é necessário chamar esse método aqui, ele será chamado se for necessário ao receber um alerta de watcher
                                // checkingPendingSave();
                            }

                        } catch (final AvroRemoteException ex) {
                            LOGGER.error("[AvroRemoteException] - " + ex.getMessage());
                        } catch (final KeeperException ex) {
                            LOGGER.error("[KeeperException] - " + ex.getMessage());
                        } catch (final InterruptedException ex) {
                            LOGGER.error("[InterruptedException] - " + ex.getMessage());
                        } catch (final IOException ex) {
                            LOGGER.error("[IOException] - " + ex.getMessage());
                        } catch (final NoSuchAlgorithmException ex) {
                            LOGGER.error("[NoSuchAlgorithmException] - " + ex.getMessage());
                        } catch (final SftpException ex) {
                            LOGGER.error("[SftpException] - " + ex.getMessage());
                        }
                    }
                    break;
                }
            default:
                break;
        }
    }
}
