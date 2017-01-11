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
package br.unb.cic.bionimbuz.plugin.linux;

import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class LinuxGetInfo implements Callable<PluginInfo> {

    private static final String SERVICE_DIR = "services";
    public static final String PATH = "files";
    public static final String CORES = "dstat -cf";
    public static final String CPUMHZ = "grep -m 1 MHz /proc/cpuinfo";
    public static final String CPUGHZ = "grep -m 1 GHz /proc/cpuinfo";
    public static final String MEMTOTAL = "grep -m 1 MemTotal /proc/meminfo";
    public static final String MEMFREE = "grep -m 1 MemFree /proc/meminfo";
    private final PluginInfo pluginInfo = new PluginInfo();

    public LinuxGetInfo() {
    }

    @Override
    public PluginInfo call() {
        try {
            getCpuInfo();
            getMemoryInfo();
            getDiskInfo();
            getServices();
            getUptime();
        }catch(Exception ex){
           Logger.getLogger(LinuxGetInfo.class.getName()).log(Level.SEVERE,null,ex);
        }
        return pluginInfo;
    }

    /**
     * Obtem as informações do processador do recurso e realiza o setter dessa informações na classe pluginInfo.
     * Número de cores, frequencia do processador(GHz) e quantidade de cores ocupados.
     */
    private void getCpuInfo() {
        int nCpus = Runtime.getRuntime().availableProcessors();
        pluginInfo.setNumCores(nCpus);
        // TODO: correct numNodes
        pluginInfo.setNumNodes(1);
        pluginInfo.setNumOccupied(getCoresOccupied(nCpus));
        String cpuInfo = execCommand(CPUMHZ);
        pluginInfo.setCurrentFrequencyCore((new Double(cpuInfo.substring(cpuInfo.indexOf(":") + 1, cpuInfo.length()).trim())) / 100000);
        cpuInfo = execCommand(CPUGHZ);
        Double freq = new Double(cpuInfo.substring(cpuInfo.indexOf("@") + 1, cpuInfo.length()-3).trim())*1000000000;
        pluginInfo.setFactoryFrequencyCore(freq);
    }

    /**
     * Retorna o número de cores ocupados no recuso caso seu processamento estaja acima de 70 porcento.
     * @return número de cores ocupados
     */
    private int getCoresOccupied(int numCpu){
        int nCpuOccupied=0;
        try {
            InputStreamReader read ;
            BufferedReader buffer ;
            List<Integer> linesCPU= new ArrayList<>(numCpu);
            String[] columns,lines;
            String line;
            int count=0,i=0;
            Process p = Runtime.getRuntime().exec(CORES);
            read = new InputStreamReader(p.getInputStream());
            buffer = new BufferedReader(read);
            // magic number                                 numCpu+(4)
            while((line = buffer.readLine())!=null && count<(numCpu+4)){
                if(count>=3){
                    columns = line.trim().split(":");
                    for(int j=0; j<numCpu;j++){
                        lines = columns[j].trim().split(" ");
                        if(i!=0){
                            linesCPU.set(j,new Integer(lines[0])+linesCPU.get(j));
                        }else{
                            linesCPU.add(j,new Integer(lines[0]));
                        }
                    }
                i++;
                }
                count++;
            }
            //finaliza a execução
            p.destroy();
            for(i=0; i<numCpu;i++){
                if((linesCPU.get(i)/3)>80){
                    nCpuOccupied++;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LinuxGetInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nCpuOccupied;
    }
    
    /**
     * Obtem as informações da memória RAM do recurso e realiza o setter dessa informações na classe pluginInfo.
     * Memória total e memória livre em GigaBytes(Retorno da eexcução em KB / 1024²).
     */
    private void getMemoryInfo() {
        String mem = execCommand(MEMTOTAL);
        pluginInfo.setMemoryTotal((new Double(mem.substring(mem.indexOf(":") + 1, mem.length() - 2).trim()) / 1048576));
        mem = execCommand(MEMFREE);
        pluginInfo.setMemoryFree((new Double(mem.substring(mem.indexOf(":") + 1, mem.length() - 2).trim()) / 1048576));
    }

    /**
     * Obtem as informações do espaço e, disco do recurso e realiza o setter dessa informações na classe pluginInfo.
     * Espaço total e espaço livre.
     */
    private void getDiskInfo() {
        File path = new File(PATH);
        for (File root : File.listRoots()) {
            if (path.getAbsolutePath().contains(root.getAbsolutePath())) {
                pluginInfo.setFsFreeSize((float) root.getFreeSpace());
                pluginInfo.setFsSize((float) root.getTotalSpace());
                return;
            }
        }
    }

    private void getServices() throws Exception {
        final List<PluginService> list = new CopyOnWriteArrayList<>();
        File dir = new File(SERVICE_DIR);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.canRead() && file.getName().endsWith(".json")) {
                    ObjectMapper mapper = new ObjectMapper();
                    PluginService service = mapper.readValue(file, PluginService.class);
                    list.add(service);
                }
            }
        }
        pluginInfo.setServices(list);
    }
    
    private void getUptime(){
        pluginInfo.setUptime(System.currentTimeMillis());
    }


    /**
     * Retorna os valores da execução do comando informado pelo parâmetro
     *
     * @param Command comando a ser executado
     * @return string resultado da execução
     */
    private String execCommand(String Command) {
        String line = null;
        InputStreamReader read;
        try {
            read = new InputStreamReader(Runtime.getRuntime().exec(Command).getInputStream());
            BufferedReader buffer = new BufferedReader(read);
            line = buffer.readLine();
        } catch (IOException ex) {
            Logger.getLogger(LinuxGetInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return line;
    }
}
