package br.unb.cic.bionimbus.services.sched;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskState;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.services.sched.policy.impl.AcoSched;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.utils.Get;
import br.unb.cic.bionimbus.utils.Pair;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SchedService extends AbstractBioService implements Service, P2PListener, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedService.class.getSimpleName());
    private final ConcurrentHashMap<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    private final ScheduledExecutorService schedExecService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
            .namingPattern("SchedService-%d").build());
    private final Queue<PluginTask> relocateTasks = new ConcurrentLinkedQueue<PluginTask>();
    private final Map<String, JobInfo> pendingJobs = new ConcurrentHashMap<String, JobInfo>();
    private Map<String, Pair<PluginInfo, PluginTask>> waitingTask = new ConcurrentHashMap<String, Pair<PluginInfo, PluginTask>>();
    private Map<String, PluginFile> mapFilesPlugin;
    private final Map<String, JobInfo> jobsWithNoService = new ConcurrentHashMap<String, JobInfo>();
//    private final Queue<PluginTask> runningJobs = new ConcurrentLinkedQueue<PluginTask>();
    private final Map<String, PluginInfo> cancelingJobs = new ConcurrentHashMap<String, PluginInfo>();
    private RpcClient rpcClient;

    private final Integer policy = 0;
    private P2PService p2p = null;
    private String idPlugin;
    private LinuxPlugin myLinuxPlugin;
    private SchedPolicy schedPolicy;
    private static final String STATUS = "/STATUS";
    private static final String STATUSWAITING = "/STATUSWAITING";
    private static final String ROOT_PEER = "/peers";
    private static final String SCHED = "/sched";
    private static final String JOBS = "/jobs";
    private static final String LATENCY = "/latency";
    private static final String TASKS = "/tasks";
    private static final String PREFIX_JOB = "/job_";
    private static final String PREFIX_TASK = "/task_";
    private static final String SEPARATOR = "/";

    @Inject
    public SchedService(final ZooKeeperService service) {
        Preconditions.checkNotNull(service);
        this.zkService = service;


    }

    public synchronized SchedPolicy getPolicy() {
        if (schedPolicy == null) {
            schedPolicy = SchedPolicy.getInstance(policy, cloudMap);
        }

        schedPolicy.setCloudMap(cloudMap);
        return schedPolicy;
    }

    /**
     * Altera a política de escalonamento para executar os jobs.
     */
    private void setPolicy() {
        try {
            String dataPolicy = zkService.getData(JOBS, null);
            if (dataPolicy != null && !dataPolicy.isEmpty()) {
                schedPolicy = SchedPolicy.getInstance(new Integer(dataPolicy), cloudMap);
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        checkTasks();
    }
    // TO DO retirar serviço P2P?

    @Override
    public void start(P2PService p2p) {
        this.p2p = p2p;
        if (p2p != null) {
            p2p.addListener(this);
        }
        idPlugin = this.p2p.getConfig().getId();

        //inicia o valor do zk na politica de escalonamento
        getPolicy().schedule(null, zkService);

        zkService.createPersistentZNode(JOBS, policy.toString());
        zkService.createPersistentZNode(zkService.getPath().SCHED.getFullPath(idPlugin, "", ""), null);
        zkService.createPersistentZNode(zkService.getPath().TASKS.getFullPath(idPlugin, "", ""), null);
        zkService.createPersistentZNode(zkService.getPath().SIZE_JOBS.getFullPath(idPlugin, "", ""), null);

        cloudMap.putAll(getPeers());
        try {
            //adicona watchers para receber um alerta quando um novo job for criado para ser escalonado, e uma nova requisição de latência existir
            zkService.getChildren(JOBS, new UpdatePeerData(zkService, this));
            zkService.getData(JOBS, new UpdatePeerData(zkService, this));
            zkService.getChildren(ROOT_PEER, new UpdatePeerData(zkService, this));
            zkService.getChildren(LATENCY, new UpdatePeerData(zkService, this));


            checkMyPlugin();
            checkWaitingTasks();
            checkPeers();

        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        schedExecService.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Executa a rotina de escalonamento, após o zookeeper disparar um aviso que
     * um novo job foi criado para ser escalonado.
     */
    private void scheduleJobs() throws InterruptedException, KeeperException {
        HashMap<JobInfo, PluginInfo> schedMap = null;

        // Caso nao exista nenhum job pendente da a chance do escalonador
        // realocar as tarefas.
        if (!getPendingJobs().isEmpty()) {
            cloudMap.clear();
            cloudMap.putAll(getPeers());
            LOGGER.info("Número de plugins: "+cloudMap.size());
            //realiza a requisicao dos valores da lantênia antes de escalonar um job
            schedMap = getPolicy().schedule(getPendingJobs().values(), zkService);

            for (Map.Entry<JobInfo, PluginInfo> entry : schedMap.entrySet()) {
                JobInfo jobInfo = entry.getKey();

                LOGGER.info(">>>  Tempo de escalonamento: " + (System.currentTimeMillis() - jobInfo.getTimestamp())+" milesegundos");

                PluginInfo pluginInfo = entry.getValue();
                PluginTask task = new PluginTask();
                task.setJobInfo(jobInfo);
                if (pluginInfo != null) {
                    LOGGER.info(">> SCHEDULE JobID: " + jobInfo.getId()+ " , saida:("+jobInfo.getOutputs()+") escalonado para peer_" + pluginInfo.getId());

                    task.setState(PluginTaskState.PENDING);
                    task.setPluginExec(pluginInfo.getId());
                    task.setPluginTaskPathZk(pluginInfo.getPath_zk() + SCHED + TASKS + PREFIX_TASK + task.getId());
                    //adiciona o job na lista de execução do servidor zookeeper
                    zkService.createPersistentZNode(task.getPluginTaskPathZk(), task.toString());

                    //retira o job da lista de jobs para escanolamento no zookeeper
                    zkService.delete(JOBS + PREFIX_JOB + task.getJobInfo().getId());
                    //retira o job da lista de jobs para escalonamento
                    getPendingJobs().remove(task.getJobInfo().getId());
//                    //adiciona a lista de jobs que aguardam execução
//                    waitingTask.put(task.getJobInfo().getId(), new Pair<PluginInfo, PluginTask>(pluginInfo,task));

                } else {
                    LOGGER.info("JobID: " + jobInfo.getId() + " não escalonado");
                }
            }
        }
    }

    /**
     * Realiza a definição dos valor da latência dos plugins em relação ao
     *
     * @param ip informado, se esse ip for correspondente ao plugin atual.
     * @param ip que deve calcular a latência
     */
    private void setLatencyPlugins(String ip) {
        if (myLinuxPlugin.getMyInfo().getHost().getAddress().equals(ip)) {
            try {
                for (PluginInfo plugin : getPeers().values()) {
                    plugin.setLatency(Ping.calculo(plugin.getHost().getAddress()));
                    
                    zkService.setData(zkService.getPath().PREFIX_PEER.getFullPath(plugin.getId(), "", ""), plugin.toString());
                }

                zkService.delete(LATENCY + SCHED);
            } catch (KeeperException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void requestLatency(String ip) {
        try {
            while (zkService.getZNodeExist(LATENCY + SCHED, false)) {
                TimeUnit.SECONDS.sleep(2);
            }
            zkService.createEphemeralZNode(LATENCY + SCHED, ip);
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Rotinas para auxiliar escalonamento,chamado caso seja necessário cancelar
     * um job. Não remove job em execução.
     *
     * OBS: não utilizado
     *
     * @param origin
     * @param jobId
     */
    public void cancelJob(String jobId) {
        if (getPendingJobs().containsKey(jobId)) {
            getPendingJobs().remove(jobId);
            //excluir o job do zookeeper TO DO
        } else if (waitingTask.containsKey(jobId)) {
            waitingTask.remove(jobId);
        }
        try {
            zkService.delete(JOBS + PREFIX_JOB + jobId);

        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Remove a tarefa da lista de jobs cancelados.Job permanece na lista de
     * jobs a serem escalonados.
     *
     * @param task
     */
    private synchronized void finishCancelJob(PluginTask task) {
        getCancelingJobs().remove(task.getId());
        System.out.println("Task canceled " + task.getId());

        relocateTasks.add(task);
        // Verifica se todas as requisicoes de cancelamento foram realizadas.
        // TODO: Provavelmente se o usuario cancelar o job na mao essa
        // funcao vai buggar. Mas dado o tempo que temos acho que eh a melhor
        // solucao.
    }

    /**
     * Realiza a requisição do(s) arquivo(s) que não existe(m) no plugin.
     *
     * @param listFiles lista de arquivos que devem conter no plugin
     */
    private void requestFile(List<Pair<String, Long>> listFiles) {
        for (Pair<String, Long> pair : listFiles) {
            if (!mapFilesPlugin.containsKey(pair.first)) {
                String ipContainsFile = getFilesIP(pair.first);
                Get conexao = new Get();
                try {
                    conexao.startSession(pair.first, ipContainsFile);
                } catch (JSchException ex) {
                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SftpException ex) {
                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                }


                //zkService.createPersistentZNode(zkService.getPath().PREFIX_PENDING_FILE.getFullPath("", pair.first, ""), pluginFile.toString());
            }

        }
        checkFilesPlugin();
    }

    /**
     * Recebe uma lista de PluginsTasks para serem relançadas ao escalonamento.
     *
     * @param tasks lista das tarefas
     */
    public void relocateTasks(Collection<PluginTask> tasks) throws KeeperException, InterruptedException {
        relocateTasks.addAll(tasks);

        //Adiciona os jobs cancelados a lista de jobs a serem escalonados no servidor zookeeper
        while (!relocateTasks.isEmpty()) {
            PluginTask task = relocateTasks.remove();
            try {
                zkService.createPersistentZNode(JOBS + PREFIX_JOB + task.getJobInfo().getId(), task.getJobInfo().toString());

            } catch (Exception ex) {

//                zkService.getData(zkService.getPath().PREFIX_TASK.getFullPath(task.getPluginExec(), "", task.getId()), null);
                StringBuilder datas = new StringBuilder(zkService.getData(zkService.getPath().STATUSWAITING.getFullPath(task.getPluginExec(), "", ""), null));
                zkService.setData(zkService.getPath().STATUSWAITING.getFullPath(task.getPluginExec(), "", ""), datas.append("E").toString());
                tasks.add(task);
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Realiza a recuperação das tarefas que estavam em execução no peer que
     * ficou off-line, colocando-as para serem reescalonadas. Ao final exclui o
     * zNode STATUSWAITING para que o peer possa ser excluído do servidor
     * zookeeper
     *
     * @param peerPath
     */
    private void repairTask(String peerPath) {
        Collection<PluginTask> repairTasks = new LinkedList<PluginTask>();
        try {
            StringBuilder dataStatus = new StringBuilder();

            if (!zkService.getZNodeExist(peerPath + STATUSWAITING, false)) {
                zkService.createPersistentZNode(peerPath + STATUSWAITING, "");
            }

            dataStatus.append(zkService.getData(peerPath + STATUSWAITING, null));

            //verifica se recurso já foi recuperado ou está sendo recuperado por outro recurso
            if (dataStatus.toString().contains("E") || dataStatus.toString().contains("B")) {
                return;
            }

            //bloqueio para recuperar tarefas sem que outros recursos realizem a mesma operação
            zkService.setData(peerPath + STATUSWAITING, dataStatus.append("B").toString());

            List<String> tasksChildren = zkService.getChildren(peerPath + SCHED + TASKS, null);

            for (String taskChild : tasksChildren) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(zkService.getData(peerPath + SCHED + TASKS + SEPARATOR + taskChild, null), PluginTask.class);
                if (pluginTask.getState() != PluginTaskState.DONE && pluginTask.getState() != PluginTaskState.ERRO) {
                    repairTasks.add(pluginTask);
                }
            }
            relocateTasks(repairTasks);

            //retira bloqueio de uso e sinaliza que as tarefas do plugin foram recuperadas
            dataStatus.deleteCharAt(dataStatus.indexOf("B")).toString();
            zkService.setData(peerPath + STATUSWAITING, dataStatus.append("E").toString());

        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Verifica qual é o Plugin referente ao mesmo do recurso.
     */
    private void checkMyPlugin() {
        List<P2PListener> listeners = p2p.getListener();
        for (P2PListener listener : listeners) {
            if (listener instanceof LinuxPlugin) {
                this.myLinuxPlugin = (LinuxPlugin) listener;
            }
        }
    }

    /**
     * Realiza a verificação dos peers existentes identificando se existe algum
     * peer aguardando recuperação, se o peer estiver off-line e a recuperação
     * já estiver sido feito, realiza a limpeza do peer. Realizada apenas quando
     * o módulo inicia.
     */
    private void checkPeers() {
        try {
            List<String> listPeers = zkService.getChildren(ROOT_PEER, null);
            for (String peerPath : listPeers) {

                if (!zkService.getZNodeExist(ROOT_PEER + SEPARATOR + peerPath + STATUS, false)
                        && !zkService.getZNodeExist(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, false)) {
                    zkService.createPersistentZNode(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, "");
                }
                if (zkService.getZNodeExist(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, false)) {
                    if (!zkService.getData(ROOT_PEER + SEPARATOR + peerPath + STATUSWAITING, null).contains("E")) {
                        repairTask(ROOT_PEER + SEPARATOR + peerPath);
                    }
                }

            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Verifica quais são as tarefas que já estão escanoladas e adiciona um
     * watcher em cada conjunto de tarefas dos plugins e nas tarefas caso
     * existam. Método é chamado ao iniciar o módulo de escalonamento.
     *
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    private void checkWaitingTasks() throws KeeperException, InterruptedException, IOException {
        List<PluginInfo> plgs = new ArrayList<PluginInfo>(getPeers().values());
        List<String> listTasks;
        Watcher watcher;
        System.out.println("..checkWaitingTasks..");

        for (PluginInfo plugin : plgs) {

            //cria watch para ser adicionado no znode que contém as tarefas escanoladas desse plugin
            if (myLinuxPlugin.getMyInfo().getId().equals(plugin.getId())) {
                System.out.println("checkWaitingTasks : watcher adicionado no plugin " + plugin.getId());
                watcher = new UpdatePeerData(zkService, this);
            } else {
                watcher = null;
            }
            listTasks = zkService.getChildren(zkService.getPath().TASKS.getFullPath(plugin.getId(), "", ""), watcher);

            for (String task : listTasks) {
                ObjectMapper mapper = new ObjectMapper();
                PluginTask pluginTask = mapper.readValue(zkService.getData(zkService.getPath().PREFIX_TASK.getFullPath(plugin.getId(), "", task.substring(5, task.length())), null), PluginTask.class);

                waitingTask.put(pluginTask.getJobInfo().getId(), new Pair<PluginInfo, PluginTask>(plugin, pluginTask));
                if (pluginTask.getState() == PluginTaskState.DONE) {
                    finalizeTask(pluginTask);
                }

            }

            //adiconando watch para cada peer, realizará recuperação de task escalonadas caso o plugin fique off-line
            zkService.getData(plugin.getPath_zk() + STATUS, new UpdatePeerData(zkService, this));
        }

    }

    /**
     * Verifica no zookeeper qual foi a nova tarefa colocada para execução,
     * adiciona um watcher na tarefa e adiciona a tarefa na lista de tarefas que
     * estão aguardando execução.
     *
     * @param peerPath caminho do local que foi criado a tarefa no zookeeper.
     * @return a pluginTask da tarefas adicionada para execução
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    private PluginTask getNewTask(String taskPath) throws KeeperException, InterruptedException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        PluginTask pluginTask = null;
        List<String> tasksChildren = zkService.getChildren(taskPath, null);

        for (String taskChild : tasksChildren) {
            String datasTask = zkService.getData(taskPath + SEPARATOR + taskChild, null);

            PluginTask task = mapper.readValue(datasTask, PluginTask.class);
            //verifica se a tarefa já estava na lista para não adicionar mais de um watcher
            if (!waitingTask.containsKey(task.getJobInfo().getId())) {
                //adiciona um watcher na task que foi escanolada
                zkService.getData(taskPath + SEPARATOR + taskChild, new UpdatePeerData(zkService, this));
                if (task.getState() == PluginTaskState.PENDING) {
                    waitingTask.put(task.getJobInfo().getId(), new Pair<PluginInfo, PluginTask>(cloudMap.get(task.getPluginExec()), task));
                    pluginTask = task;
                }
            }
        }
        return pluginTask;
    }

    /**
     * Método que realiza a verificação dos arquivos existentes no plugin para
     * possíveis utilizações durante a execução das tarefas.
     *
     */
    // TODO ajustar a Storage para realizar alguma foram de atualização dos arquivos antes e depois de uma execução e então inutilizar esse método
    private void checkFilesPlugin() {
        try {
            //realiza uma chama rpc para atualizar a lista de arquivos no zookeeper
            rpcClient = new AvroClient(p2p.getConfig().getRpcProtocol(), myLinuxPlugin.getMyInfo().getHost().getAddress(), myLinuxPlugin.getMyInfo().getHost().getPort());
            rpcClient.getProxy().listFilesName();
            rpcClient.close();
            if (zkService.getZNodeExist(myLinuxPlugin.getMyInfo().getPath_zk() + zkService.getPath().FILES.toString(), false)) {
                List<String> filesChildren;
                //verifica se é a primeira vez que é executado e então cria o watcher e inicia a lista
                if (mapFilesPlugin == null) {
                    mapFilesPlugin = new HashMap<String, PluginFile>();
                    filesChildren = zkService.getChildren(myLinuxPlugin.getMyInfo().getPath_zk() + zkService.getPath().FILES.toString(), new UpdatePeerData(zkService, this));
                } else {
                    filesChildren = zkService.getChildren(myLinuxPlugin.getMyInfo().getPath_zk() + zkService.getPath().FILES.toString(), null);
                }
                ObjectMapper mapper = new ObjectMapper();

                for (String fileChild : filesChildren) {
                    String datasFile = zkService.getData(myLinuxPlugin.getMyInfo().getPath_zk() + zkService.getPath().FILES.toString() + SEPARATOR + fileChild, null);
                    PluginFile file = mapper.readValue(datasFile, PluginFile.class);
                    //Verificar o que é esse LONG TO DO
                    Pair<String, Long> pair = new Pair<String, Long>(file.getId(), file.getSize());
                    //verifica se o arquivo já estava na lista 
                    if (!mapFilesPlugin.containsKey(pair.first)) {
                        mapFilesPlugin.put(pair.first, file);
                    }
                }
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /**
     * Verifica o status da tarefa e se ela pertence ao plugin e executa se
     * satisfizer essas condições.
     */
    private void checkStatusTask() {
        for (Pair<PluginInfo, PluginTask> pair : waitingTask.values()) {
            if (myLinuxPlugin.getMyInfo().getId().equals(pair.first.getId()) && pair.second.getState() == PluginTaskState.PENDING) {
                try {
                    executeTasks(pair.second);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    /**
     * Verifica se as tarefas que estavam aguardando algum arquivo já foram
     * executadas, se não solicita execução.
     */
    private void checkTasks() {
        try {
            if (!waitingTask.isEmpty()) {
                for (Pair<PluginInfo, PluginTask> pair : waitingTask.values()) {
                    if (pair.first.equals(myLinuxPlugin.getMyInfo())) {
                        if (pair.second.getState() == PluginTaskState.WAITING) {
                            executeTasks(pair.second);
                        }
                    }
                    if (pair.second.getState() == PluginTaskState.DONE) {
                        finalizeTask(pair.second);
                    }
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Recebe a última tarefa enviada para execução
     *
     * @param task
     */
    private void executeTasks(PluginTask task) throws Exception {
        //TODO otimiza chamada de checage dos arquivos
        checkFilesPlugin();
        //verifica se o arquivo existe no plugin se não cria a solicitação de transfêrencia do arquivo
        if (!existFilesCloud(task.getJobInfo().getInputs())) {
            if (task.getState() == PluginTaskState.PENDING) {
                task.setState(PluginTaskState.WAITING);
            }
            return;
        }
        if (!existFiles(task.getJobInfo().getInputs())) {
            requestFile(task.getJobInfo().getInputs());
        }
        if (existFiles(task.getJobInfo().getInputs())) {
            myLinuxPlugin.startTask(task, zkService);
        } else {
            task.setState(PluginTaskState.WAITING);
        }

    }

    /**
     * Verifica a existência dos arquivos de entrada no plugin, lista de
     * arquivos é atualizada ao plugin ser iniciado e quando um novo arquivo é
     * adicionado ao plugin.
     *
     * @param listInputFiles
     * @return
     */
    private boolean existFiles(List<Pair<String, Long>> listInputFiles) {

        for (Pair<String, Long> fileInput : listInputFiles) {
            if (mapFilesPlugin.containsKey(fileInput.first)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica a existência dos arquivos de entrada na federação, caso não
     * exista retorna false.
     *
     * @param listInputFiles
     * @return false se não existir algum arquivo no zoonimbus
     */
    private boolean existFilesCloud(List<Pair<String, Long>> listInputFiles) {

        for (Pair<String, Long> fileInput : listInputFiles) {
            if (getFilesIP(fileInput.first) == null) {
                LOGGER.info("Arquivo :" + fileInput.first + " não existe no Zoonimbus !!!");
                return false;
            }
        }

        return true;
    }

    /**
     * Realiza a chamada dos métodos para a finalização da execução do job.
     *
     * @param pathTask endereço do znode, task executada.
     */
    private void finalizeTask(PluginTask task) {
        if (waitingTask.containsKey(task.getJobInfo().getId())) {
            waitingTask.remove(task.getJobInfo().getId());
        }

        try {
            getPolicy().jobDone(task);
            zkService.delete(task.getPluginTaskPathZk());
            //chamada de método para atualizar a lista de arquivos
            // TODO ajustar a Storage para realizar alguma foram de atualização dos arquivos após uma execução.
            checkFilesPlugin();
            //cria um znode efêmero para exibir jobs finalizados, é efêmero para poder ser apagado quando peer ficar off-line
            zkService.createEphemeralZNode(task.getPluginTaskPathZk(), task.toString());

            LOGGER.info("Tempo de execução job -"+ task.getJobInfo().getOutputs()+"- Segundos: " + task.getTimeExec() + " , Minutos: " + task.getTimeExec() / 60);
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /**
     * Metodo para pegar o Ip de cada peer na federação e verificar se um
     * arquivo está com este peer, se o arquivo for encontrado retorna o Ip do
     * peer, caso contrário retorna null.
     *
     * @param file
     * @return
     */
    public String getFilesIP(String file) {
        List<String> listFiles;
        // Map<String,List<String>> mapFiles = new HashMap<String, List<String>>();
        try {
            for (Iterator<PluginInfo> it = getPeers().values().iterator(); it.hasNext();) {
                PluginInfo plugin = it.next();
                listFiles = zkService.getChildren(plugin.getPath_zk() + zkService.getPath().FILES.toString(), null);
                for (String checkfile : listFiles) {

                    //atualizar

                    String idfile = checkfile.substring(5, checkfile.length());
                    if (file.equals(idfile)) {
                        return plugin.getHost().getAddress();
                    }
                }
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;

    }

    /**
     * Trata os watchers enviados pelas mudanças realizadas no zookeeper.
     *
     * @param eventType evento recebido do zookeeper
     */
    @Override
    public void event(WatchedEvent eventType) {
        try {
            switch (eventType.getType()) {

                case NodeChildrenChanged:

                    String datas;
                    if (eventType.getPath().contains(JOBS)) {

                        List<String> children;
                        children = zkService.getChildren(eventType.getPath(), null);
                        if (!children.isEmpty()) {
                            for (String child : children) {
                                ObjectMapper mapper = new ObjectMapper();
                                datas = zkService.getData(eventType.getPath() + "/" + child, null);
                                JobInfo job = mapper.readValue(datas, JobInfo.class);

                                //rotina para criar bloqueio para que nenhuma outra máquina selecione o job para escalonar
                                if (!zkService.getZNodeExist(zkService.getPath().LOCK_JOB.getFullPath("", "", job.getId()), true)) {
                                    String result = zkService.createEphemeralZNode(zkService.getPath().LOCK_JOB.getFullPath("", "", job.getId()), "");
                                    if (result != null) {
                                        if (!getPendingJobs().containsKey(job.getId())) {
                                            getPendingJobs().put(job.getId(), job);
                                        }
                                    }
                                }
                            }
                            if (!getPendingJobs().isEmpty()) {
                                requestLatency(getFilesIP(AcoSched.getBiggerInputJob(AcoSched.getBiggerJob(getPendingJobs().values()))));
                            }

                        }

                    } else if (eventType.getPath().contains(SCHED + TASKS)) {
                        //verifica qual foi o job colocado para ser executado 
                        PluginTask pluginTask = getNewTask(eventType.getPath());
                        //verifica se um existe algum novo pluginTask
                        if (pluginTask != null) {
                            if (pluginTask.getState() == PluginTaskState.PENDING) {
                                executeTasks(pluginTask);
                            }
                        } else if (!waitingTask.isEmpty()) {
                            checkStatusTask();
                        }

                    } else if (eventType.getPath().contains(zkService.getPath().FILES.toString())) {
                            checkFilesPlugin();
                    } else if (eventType.getPath().contains(LATENCY) && (zkService.getZNodeExist(LATENCY + SCHED, false))) {
                                setLatencyPlugins(zkService.getData(LATENCY + SCHED, null));
                                scheduleJobs();

                    } else if (eventType.getPath().equals(ROOT_PEER)) {
                        if (cloudMap.size() < getPeers().size()) {
                            verifyPlugins();
                        }
                    }

                    break;
                case NodeDataChanged:
                    if (eventType.getPath().contains(JOBS) && !eventType.getPath().contains(PREFIX_JOB)) {
                        setPolicy();
                    } else if (zkService.getZNodeExist(eventType.getPath(), false)) {
                        datas = zkService.getData(eventType.getPath(), null);
                        PluginTask pluginTask = (PluginTask) convertString(PluginTask.class, datas);

                        //retirar depois testes, exibi a tarefa que está em execução
                        if (pluginTask.getState() == PluginTaskState.RUNNING) {
                            System.out.println("Task está rodando: " + pluginTask.getPluginTaskPathZk());
                        }
                        if (pluginTask.getState() == PluginTaskState.DONE) {
                            finalizeTask(pluginTask);
                        }
                    }
                    break;

                case NodeDeleted:

                    //Trata o evento de quando o zNode status for apagado, ou seja, quando um peer estiver off-line, deve recuperar os jobs
                    //que haviam sido escalonados para esse peer
                    if (eventType.getPath().contains(STATUS)) {
//                        cloudMap.clear();
//                        cloudMap.putAll(getPeers());
//                        schedPolicy.setCloudMap(cloudMap);
                        repairTask(eventType.getPath().subSequence(0, eventType.getPath().indexOf("STATUS") - 1).toString());
                    }

                    break;
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void verifyPlugins() {
        Collection<PluginInfo> temp = getPeers().values();
        temp.removeAll(cloudMap.values());
        for (PluginInfo plugin : temp) {
            try {
                if (zkService.getZNodeExist(zkService.getPath().STATUS.getFullPath(plugin.getId(), null, null), false)) {
                    zkService.getData(zkService.getPath().STATUS.getFullPath(plugin.getId(), "", ""), new UpdatePeerData(zkService, this));
                }
            } catch (KeeperException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private Object convertString(Class classe, String datas) {
        Object object = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            object = mapper.readValue(datas, classe);

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SchedService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return object;

    }

    public synchronized Map<String, JobInfo> getPendingJobs() {
        return pendingJobs;
    }

    public synchronized Map<String, PluginInfo> getCancelingJobs() {
        return cancelingJobs;
    }

    public synchronized Map<String, JobInfo> getJobsWithNoService() {
        return jobsWithNoService;
    }

    @Override
    public void shutdown() {
        p2p.remove(this);
        schedExecService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub
    }

    @Override
    public synchronized void onEvent(P2PEvent event) {
    }
}
