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
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package br.unb.cic.bionimbuz.services.sched.policy.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.plugin.PluginTaskState;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbuz.utils.Pair;

/**
 * @author gabriel
 */
public class AcoSched extends SchedPolicy {
    
    private HashMap<String, ArrayList<Double>> mapAcoDatas;
    private HashMap<String, Double> mapPluginLatency;
    private List<PluginInfo> listPlugin;
    private Double biggestProbability = Double.MIN_VALUE;
    private final Map<PluginTask, Integer> blackList = new HashMap<>();
    private static final int BLACKLIST_LIMIT = 12;
    private static final String DIR_SIZEALLJOBS = "/size_jobs";
    private static final String SCHED = "/sched";
  //  private static final String LATENCY = "/latency";
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AcoSched.class.getSimpleName());
    
    @Override
    public HashMap<Job, PluginInfo> schedule(List<Job> jobs) {
        HashMap<Job, PluginInfo> jobCloud = new HashMap<>();
        Job biggerJob = getBiggerJob(jobs);
        biggerJob.setTimestamp(System.currentTimeMillis());
        
        jobCloud.put(biggerJob, scheduleJob(biggerJob));
        
        return jobCloud;
    }
    
    /**
     * Recebe um job a ser escalonado, envia o job para a execução do algoritmo
     * ACO onde os recursos irão ser classificados e retornados para que o
     * melhor entre eles seja retornado.
     *
     * @param jobInfo contém as informações do job que deve ser escalonado
     * @return o melhor recurso disponível para executar o job informado
     */
    private PluginInfo scheduleJob(Job jobInfo) {
        listPlugin = getExactClouds(jobInfo);
        
        //realiza a chamada do método para a leitura dos dados no servidor zookeeper
        mapAcoDatas = getMapAcoDatasZooKeeper(listPlugin);
//        mapPluginLatency = getMapLatency(jobInfo);
        
        if (listPlugin.isEmpty()) {
            return null;
        }
//        if(mapPluginLatency.isEmpty()){
//            return null;
//        }
        
        //inicia o ACO para encontrar melhor PC dentro das nuvens escolhidas para o job
        AlgorithmAco(listPlugin);
        
        System.out.println("AcoSched");
        PluginInfo plugin = listPlugin.get(0);
        
        for (PluginInfo plg : listPlugin) {
            if (plg.getRanking() > plugin.getRanking()) {
                plugin = plg;
            }
        }
        
        //armazena as informações utilizadas e atualizadas para o escalonamento no servidor zookeeper
        setMapAcoDatasZooKeeper(listPlugin);
        
        
        return plugin.getId() == null ? null : plugin;
    }
    
    /**
     * Retorna as melhores nuvens para realizar o serviço, nuvens que rodam o
     * tipo de serviço requerido e se devem ser privadas ou publicas.
     *
     * @param jobInfo
     * @return lista com as nuvens que rodam o serviço requrido
     */
    public List getExactClouds(Job jobInfo) {
        //seleciona as nuvens disponíveis para o tipo informado
        List cloudList = filterTypeCloud(getCloudMap().values(), 2);
        
        //configurar o tipo de serviço requerido
        filterByService(jobInfo.getServiceId(), cloudList);
        
        return cloudList;
        
    }
    
    @Override
    public synchronized List<PluginTask> relocate(Collection<Pair<Job, PluginTask>> taskPairs) {
        List<PluginTask> tasksToCancel = new ArrayList<PluginTask>();
        for (Pair<Job, PluginTask> taskPair : taskPairs) {
            PluginTask task = taskPair.getSecond();
            Job job = taskPair.getFirst();
            
            if (PluginTaskState.RUNNING.equals(task.getState())) {
                if (blackList.containsKey(task)) {
                    blackList.remove(task);
                }
            }
            
            if (!PluginTaskState.WAITING.equals(task.getState())) {
                continue;
            }
            
            int count = 0;
            if (blackList.containsKey(task)) {
                count = blackList.get(task);
            }
            
            blackList.put(task, count + 1);
            
            if (blackList.get(task) >= BLACKLIST_LIMIT) {
                if (job != null) {
                    tasksToCancel.add(task);
                }
            }
        }
        
        for (PluginTask task : tasksToCancel) {
            blackList.remove(task);
        }
        
        return tasksToCancel;
    }
    
    @Override
    public void cancelJobEvent(PluginTask task) {
        blackList.remove(task);
    }
    
    @Override
    public void jobDone(PluginTask task) {
         
        String datas = getDatasZookeeper(Path.NODE_TASK.getFullPath(task.getPluginExec(),task.getJobInfo().getId()));
        
        ArrayList<Double> listAcoDatas;
        ObjectMapper mapper = new ObjectMapper();
        try {
            listAcoDatas = mapper.readValue(datas, ArrayList.class);
            
            //define o tempo de execução do ultimo job
            listAcoDatas.set(7, task.getTimeExec().doubleValue());
            //define o tamanho do ultimo job executado, transforma bytes em GB
            listAcoDatas.set(8, (getTotalSizeOfJobsFiles(task.getJobInfo()).doubleValue() / (1024 * 1024 * 1024)));
            
            //soma o tamanho total do job executado com o tamanho dos demais jobs executados no plugin
            listAcoDatas.set(9, (listAcoDatas.get(8) + (listAcoDatas.get(9))));
            
            //grava novamente os dados no zookeeper
//            setDatasZookeeper(Path.NODE_PEER.getFullPath(task.getPluginExec()), SCHED, listAcoDatas.toString());
        } catch (IOException ex) {
            Logger.getLogger(AcoSched.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Retorna o job com o maior arquivo de entrada e o arquivo mais antigo.
     * Todo os arquivos de entrada são considerados.
     *
     * @param jobInfos
     * @return
     */
    public static Job getBiggerJob(Collection<Job> jobInfos) {
        if (jobInfos.isEmpty()) {
            return null;
        }
        
        Job bigger = null;
        long biggerTotal = 0L;
        long timestamp = 0L;
        for (Job jobInfo : jobInfos) {
            long total = getTotalSizeOfJobsFiles(jobInfo);
            if (bigger == null) {
                bigger = jobInfo;
                biggerTotal = total;
                timestamp = jobInfo.getTimestamp();
            } else if (getTotalSizeOfJobsFiles(jobInfo) > biggerTotal || ((System.currentTimeMillis() - timestamp) - (System.currentTimeMillis() - jobInfo.getTimestamp()) > 5)) {
                bigger = jobInfo;
                biggerTotal = total;
            }
        }
        return bigger;
    }
    
    /**
     * Retorna o tamanho total dos arquivos de entrada de um job.
     *
     * @param jobInfo
     * @return
     */
    private static Long getTotalSizeOfJobsFiles(Job jobInfo) {
        long sum = 0;
        
        for (FileInfo info : jobInfo.getInputFiles()) {
            sum += info.getSize();
        }
        
        return sum;
    }
    
    /**
     * Retorna o nome do maior arquivo de entrda do job.
     *
     * @param jobInfo
     * @return
     */
    public static String getBiggerInputJob(Job jobInfo) {
        FileInfo file = null;
        
        for (FileInfo info : jobInfo.getInputFiles()) {
            if (file == null || file.getSize() < info.getSize()) {
                file = info;
            }
        }
        
        return file.getName();
    }
    
    /**
     * Seleciona o tipo de nuvem para escalonar, pública, privada ou ambas. 0 -
     * pública 1 - privada 2 - hibrida
     * @param plugins
     * @param type
     * @return 
     */
    public List<PluginInfo> filterTypeCloud(Collection<PluginInfo> plugins, int type) {
        if (type == 2) {
            return new ArrayList<PluginInfo>(plugins);
        }
        
        List<PluginInfo> clouds = new ArrayList<PluginInfo>();
        for (PluginInfo pluginInfo : plugins) {
            //COndição para verificar se nuvem é do tipo solicitada, privada(1), pública(0) ou hibrida(2),
            if (pluginInfo.getPrivateCloud() == type) {
                clouds.add(pluginInfo);
            }
        }
        return clouds;
    }
    
    /**
     * Seleciona apenas as nuvens informadas na Collection
     *
     * @param plgs e que tem o serviço informado no
     * @param serviceId.
     *
     * @param plgs
     * @param serviceId
     */
    private void filterByService(String serviceId, Collection<PluginInfo> plgs) {
        ArrayList<PluginInfo> plugins = new ArrayList<PluginInfo>();
        
        for (PluginInfo pluginInfo : plgs) {
            if (pluginInfo.getService(serviceId) != null) {
                plugins.add(pluginInfo);
            }
        }
        plgs.retainAll(plugins);
        
    }
    
    /**
     * Implementação do algoritmo ant colony optimization.
     *
     * @param plugins
     */
    private void AlgorithmAco(List<PluginInfo> plugins) {
        //Laço para define o feronômio de cada plugin(VM), inicialização do feromônio das VMs
        for (PluginInfo plugin : plugins) {
            
            updateLocalPheromone(plugin);
        }
        
        //definição do número de formigas estática
        int nAnts = 2, nPlugin, numBiggestProbability;
        
        //nAnts = (plugins.size() - Math.round(new Float(plugins.size() * 0.3)));
        Random selectPlugin = new Random();
        ArrayList<Double> datasVm;
        List<Integer> listVmVisited = null;
        
        int numIterator = cms.getChildren(Path.PEERS.getFullPath(), null).size();
        for (int cont = 0; cont < numIterator; cont++) {
            //itera sobre o número de formigas, para que cada formiga escolha e visite as VMs seleciondas aleatoriamente
            for (int i = 0; i < nAnts; i++) {
                listVmVisited = new ArrayList<Integer>();
                //itera sobre um número 30 % menor do que a quantidade de plugins para que sejam selecionados as VMs que serão visitadas
                for (int j = 0; j < (plugins.size() * 0.7); j++) {
                    //gera um número aleatório para selecionar a nuvem, número dentro do intervalo de VMs existentes na lista de plugins
                    nPlugin = selectPlugin.nextInt(plugins.size());
                    
                    //visita(calcula) os valores da VM selecionada aleatoriamente, calcula a probabilidade de escolha da VM visitada
                    datasVm = mapAcoDatas.get(plugins.get(nPlugin).getId());
                    datasVm.set(1, probabilityNextPlugin(nPlugin, plugins));
                    
                    //condição para verificar a maior probabilidade calculada de todas as VMs visitadas
                    if (datasVm.get(1) > biggestProbability) {
                        biggestProbability = datasVm.get(1);
                    }
                    
                    listVmVisited.add(nPlugin);
                }
                for (Integer number : listVmVisited) {
                    //Atualiza o feromonio local daquela VM visitada
                    updateLocalPheromone(plugins.get(number));
                }
                
                
            }
            //chamada para método que atualiza o valor do feromônio de cada plugin de acordo com o melhor escolhido
            updateGlobalPheromone(plugins, biggestProbability);
        }
        
    }
    
    /**
     * Define o feronômio do plugin passado por parâmetro. Feronômio inicial e
     * atualização de feromônio local. Define os valores dos parâmetros de
     * controle.
     *
     * @param plugin
     */
    private void updateLocalPheromone(PluginInfo plugin) {
        Double p = 0.8, pheronome = 0d;
        DecimalFormat decimal = new DecimalFormat("0.0000000000");
        //atualiza o feromonio local quando ele existe
        if (mapAcoDatas.get(plugin.getId()) != null && !mapAcoDatas.get(plugin.getId()).isEmpty() && !mapAcoDatas.get(plugin.getId()).get(0).equals(0d)) {
            System.out.println("-- (updateLocalPheromone) - Feromonio: " + mapAcoDatas.get(plugin.getId()).get(0) + ", biggestProbability: " + decimal.format(biggestProbability));
            pheronome = mapAcoDatas.get(plugin.getId()).get(0);
            Double probability = new Double(decimal.format(biggestProbability).replace(",", "."));
            pheronome = (1 - p) * pheronome + (1 / probability==0d ? 0d : probability);
            mapAcoDatas.get(plugin.getId()).set(0, new Double(decimal.format(pheronome).replace(",", ".")));
            //cria o feromonio local quando ele não existe
        } else {
            ArrayList<Double> datas = new ArrayList<Double>();
            pheronome = capacityPlugin(plugin);
            /*1,927843066×10¹⁴
            * [96.3921532974843, 0.2171958289717577, 0.0, 2.0, 3.0, 3.0, 2.0, 0.0, 0.0, 0.0]
            [24.44970745526111, 0.3272242384392408, 0.0, 2.0, 3.0, 3.0, 2.0, 0.0, 0.0, 0.0]
            [96.3921532974843, 0.3272242384392408, 0.0, 2.0, 3.0, 3.0, 2.0, 0.0, 0.0, 0.0]
            [23.10651832499031, 0.11688007265104675, 0.0, 2.0, 3.0, 3.0, 2.0, 0.0, 0.0, 0.0]
            * mapeamento dos dados de cada plugin para os valores usados no ACO
            * array posição 0 para o feronômio
            * array posição 1 para a probabilidade da formiga escolher a VM
            ---->>retirar* array posição 2 o valor da heuristica
            * array posição 3,4,5,6 valores dos reguladores do coeficeinte de controle do feronomio(alfa), da capacidade computacional(beta),
            * do carregamento balanceado(gama) e da capacidade de memória
            * obs:  teste dos coeficientes em alfa = 2, beta = 3, gama = 3, delta = 2
            * array posição 7 para o tempo de execução da última iteração
            * array posição 8 para o tamanho da última tarefa executada no plugin
            * array posição 9 para o tamanho total das tarefas executadas no plugin
            *
            */
            datas.add(0, pheronome);
            datas.add(1, 0d);
            datas.add(2, 0d);
            datas.add(3, 2d);
            datas.add(4, 4d);
            datas.add(5, 2d);
            datas.add(6, 2d);
            datas.add(7, 0d);
            datas.add(8, 0d);
            datas.add(9, 0d);
            mapAcoDatas.put(plugin.getId(), datas);
        }
    }
    
    /**
     * Atualiza os valores do feromônio de cada plugin de acordo com a melhor
     * probabilidade encontrada.
     *
     * @param listPlugin lista de plugin utilizado para realizar o escalonamento
     * @param probability melhor probabilidade encontrada
     */
    private void updateGlobalPheromone(List<PluginInfo> listPlugin, Double solution) {
        Double p = 0.8;
        DecimalFormat decimal = new DecimalFormat("0.0000000000");
        for (PluginInfo plugin : listPlugin) {
            LOGGER.info("\n\nAntes do theBestPheromone peer:(" + plugin.getId() + ") - " + mapAcoDatas.get(plugin.getId().toString()));
            Double pheronome = mapAcoDatas.get(plugin.getId()).get(0);
            //Altera somente os valores dos feromonios já calculados
            if (pheronome != null && pheronome != 0d) {
                //TODO testar coeficiente de encorajamento com a probabilidade e com o feromonio
//                pheronome = (1-p)*pheronome+(mapAcoDatas.get(plugin.getId()).get(0) /probability);
                pheronome = (1 - p) * pheronome + (mapAcoDatas.get(plugin.getId()).get(1) / solution);
                
                mapAcoDatas.get(plugin.getId()).set(0, new Double(decimal.format(pheronome).replace(",", ".")));
            }
            LOGGER.info("\nDepois do theBestPheromone peer:(" + plugin.getId() + ") - " + mapAcoDatas.get(plugin.getId().toString()) + "\n");
        }
        
    }
    
    /**
     * Implementação da fórmula que define a probabilidade de escolha do próximo
     * plugin
     *
     * @param number
     * @param plugins
     * @return a probabilidade do plugin
     */
    private Double probabilityNextPlugin(int number, List<PluginInfo> plugins) {
        Double pb;
        PluginInfo plg = plugins.get(number);
        
        //laço para definir o somatorio do feronomio, capacidade computacional e carregamento balanceado
        Double sum = new Double(0);
        for (PluginInfo plugin : plugins) {
            sum = sum + multiplicationDatasPlugin(plugin);
        }
        
        pb = (sum == 0d ? 0d : multiplicationDatasPlugin(plg) / sum);
        plg.setRanking(pb);
        
        return pb;
        
    }
    
    /**
     * Multiplicação dos valores do feromônio, capacidade computacional e
     * carregamento balanceado do plugin informado
     *
     * @param number
     * @param plugins
     * @return
     */
    private Double multiplicationDatasPlugin(PluginInfo plugin) {
        ArrayList<Double> datas = mapAcoDatas.get(plugin.getId());
        DecimalFormat decimal = new DecimalFormat("0.0000000000");
        
        //feronomio elevado a potencia alfa(valor da variavel de controle)
        Double pheromone = new Double(decimal.format(Math.pow(datas.get(0), datas.get(3))).replace(",", "."));
        Double capacityComputing = new Double(decimal.format((Math.pow(capacityPlugin(plugin), datas.get(4)))).replace(",", "."));
        Double loadBalacing = new Double(decimal.format(Math.pow(loadBalancingPlugin(plugin), datas.get(5))).replace(",", "."));
        
        Double capacityMemory = new Double(decimal.format((Math.pow(((plugin.getMemoryFree()) / 1024), datas.get(6)))).replace(",", "."));
        
//        Double capacityMemory = getRound(Math.pow( getRound(plugin.getMemoryFree()),datas.get(6)));
        //((Double)Math.pow(((Float)datas.get(2)).doubleValue(), ((Float)datas.get(5)).doubleValue() )).floatValue()
        
//        return (new Float(formatDecimal.format(pheromone)).floatValue()* new Float(formatDecimal.format(capacityComputing)).floatValue()* new Float(formatDecimal.format(loadBalacing)).floatValue());
        
        LOGGER.info("\nValores do multiplication: pheromone = " + pheromone + " capacityComputing= " + capacityComputing + " loadBalacing= " + loadBalacing
                + " capacityMemory= " + capacityMemory + "\n");
        
        
        
        return pheromone * capacityComputing * loadBalacing * capacityMemory;
        
    }
    
    /*
    *
    *
    *
    * REGULAR FEROMONIO PARA DIMINUIR QUANDO RECURSO ESTIVER "RUIM"
    * VERIFICAR AÇÃO QUANDO PB ESTIVER EM 0, DE TODOS OU SOMENTE UM
    * VERIFICAR O QUE EH MELHOR FAZER AO NAO CONSEGUIR ESCALONAR
    *
    * VER POSSIBILIDADE DE ATUALIZAR OS DADOS SOBRE A VM ANTES DE CADA FORMIGA REALIZAR A VISITA
    *
    *
    */
    /**
     * Define a capacidade computacional do plugin informado
     *
     * @param plugin
     * @return
     */
    private Double capacityPlugin(PluginInfo plugin) {
        DecimalFormat decimal = new DecimalFormat("0.0000000000");
        
        if (!(plugin.getNumCores() - plugin.getNumOccupied() == 0d)) {
//            Double result = new Double(decimal.format((plugin.getNumCores() - plugin.getNumOccupied()) * plugin.getCurrentFrequencyCore() - mapPluginLatency.get(plugin.getId())).replace(",", "."));
            Double result = new Double(decimal.format((plugin.getNumCores() - plugin.getNumOccupied()) * plugin.getCurrentFrequencyCore() - 0).replace(",", "."));
            if (!(result == 0d)) {
                return result;
            }
        }
        return 0.000000001d;
        
        
    }
    
    /**
     * Define o fator do balanceamento de carga do plugin informado
     *
     * @param plugin
     * @return
     */
    private Double loadBalancingPlugin(PluginInfo plugin) {
        Double timeExpected = timeExpectedExecJob(plugin);
        Double timeAverage = timeLastAverageJob();
        Double temp1 = (timeExpected - timeAverage < 0 ? (-(timeExpected - timeAverage)) : (timeExpected - timeAverage));
        Double temp2 = timeExpected + timeAverage;
        Double um = 1d;
        
        if (temp2 != 0d && !temp2.isNaN()) {
            return um - (temp1 / temp2);
            
        } else {
            return um;
        }
        
        
        
    }
    
    /**
     * Define o tempo de execução esperado para a tarefa.
     *
     * @param plugin
     * @param job
     * @return
     */
    private Double timeExpectedExecJob(PluginInfo plugin) {
        //(total do tamanho das tarefas executadas na VM)/capacidade computacional + tamanho da tarefa executada anteriormente/ latency
//        return (capacityPlugin(plugin) == 0d ? 0d : (mapAcoDatas.get(plugin.getId()).get(9) / capacityPlugin(plugin))
//                + (mapPluginLatency.get(plugin.getId()) == 0d ? 0d : mapAcoDatas.get(plugin.getId()).get(8) / (mapPluginLatency.get(plugin.getId()) * 1000)));
        return (capacityPlugin(plugin) == 0d ? 0d : (mapAcoDatas.get(plugin.getId()).get(9) / capacityPlugin(plugin))
                + (0 == 0d ? 0d : mapAcoDatas.get(plugin.getId()).get(8) / (0 * 1000)));
        
    }
    
    /**
     * Define o tempo médio, em segundos, de execução da última iteração nas
     * VMS.
     *
     * @param plugin
     * @return tempo
     */
    private Double timeLastAverageJob() {
        Double time = 0d;
        
        int cont = 0;
        for (PluginInfo plugin : listPlugin) {
            
            time = time + mapAcoDatas.get(plugin.getId()).get(7);
            
            cont++;
        }
        return (time / 1000) / cont;
        
    }
    
    /**
     * Define o grau de desbalanceamento das vms
     *
     * @param listPlugin
     * @return DI
     */
    private Double degreeImbalance() {
        Double timeMax = Double.MIN_NORMAL, timeMin = Double.MAX_VALUE, time = null, timeAverage = 0d;
        
        int cont = 0;
        for (PluginInfo plugin : listPlugin) {
            
            time = new Double(getDatasZookeeper(Path.SIZE_JOBS.getFullPath(plugin.getId()))) / ((plugin.getNumCores() - plugin.getNumOccupied()) * plugin.getCurrentFrequencyCore());
            
            if (time < timeMin) {
                timeMin = time;
            } else if (time > timeMax) {
                timeMax = time;
            }
            timeAverage = timeAverage + time;
            cont++;
        }
        
        timeAverage = timeAverage / cont;
        
        
        
        return (timeMax - timeMin) / timeAverage;
        
    }
    
    private Double getRound(Double value) {
        
        return Math.rint(value * 1000) / 1000;
    }
    
    /**
     * Retorna os valores armazenados no zookeeper contendo os dados utilizados
     * pelo ACO de cada plugin.
     *
     * @param listClouds a lista de plugins que podem conter dados
     * @return o mapa com os dados ACO de cada plugin
     */
    private HashMap getMapAcoDatasZooKeeper(List<PluginInfo> listClouds) {
        HashMap map = new HashMap<>();
        String datasString;
        for (PluginInfo plugin : listClouds) {
            datasString = getDatasZookeeper(Path.TASKS.getFullPath(plugin.getId()));
            ObjectMapper mapper = new ObjectMapper();
            try {
                if (datasString != null && !datasString.isEmpty()) {
                    ArrayList array = mapper.readValue(datasString, ArrayList.class);
                    map.put(plugin.getId(), array);
                } else {
                    map.put(plugin.getId(), null);
                }
                
            } catch (Exception ex) {
                Logger.getLogger(AcoSched.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return map;
    }
    
    /**
     * Grava os valores utilizados pelo ACO de cada plugin no zookeeper.
     */
    private void setMapAcoDatasZooKeeper(List<PluginInfo> listClouds) {
        for (PluginInfo plugin : listClouds) {
//            LOGGER.info("\nValores do AcoSched - "+mapAcoDatas.get(plugin.getId()).toString()+"\n");
            setDatasZookeeper(Path.NODE_PEER.getFullPath(plugin.getId()), SCHED, mapAcoDatas.get(plugin.getId()).toString());
            
        }
    }
    
    /**
     * Recupera os dados do
     *
     * @param plugin, pluginInfo, armazenados no zookeeper de acordo com o
     * @param dir, diretorio, informado.
     * @param plugin identificação do recurso que deve ser retirado os dados.
     * @param dir diretório do zookeeper que contém as informações desejadas.
     * @return dados contidos no diretorio
     */
    private String getDatasZookeeper(String zkPath) {
        String datas = "";
        if (cms.getZNodeExist(zkPath, null)) {
            datas = cms.getData(zkPath, null);
        }
        
        return datas;
    }
    
    /**
     * Recupera os dados do
     *
     * @param plugin, pluginInfo, armazenados no zookeeper de acordo com o
     * @param dir, diretorio, informado.
     * @param plugin identificação do recurso que deve ser retirado os dados.
     * @param dir diretório do zookeeper que contém as informações desejadas.
     * @return dados contidos no diretorio
     */
    private void setDatasZookeeper(String zkPath, String dir, String datas) {
        if (cms.getZNodeExist(zkPath + dir, null)) {
//            cms.setData(zkPath + dir, datas);
        }
    }
    
    @Override
    public String getPolicyName() {
        return "Name: " + AcoSched.class.getSimpleName() + " - Número: 0";
    }
}
//2181 porta zookeper
