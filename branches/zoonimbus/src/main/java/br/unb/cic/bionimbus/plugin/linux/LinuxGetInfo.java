package br.unb.cic.bionimbus.plugin.linux;

import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class LinuxGetInfo implements Callable<PluginInfo> {

    private static final String SERVICE_DIR = "services";

    public static final String PATH = "files";

    public static final String CORES = "grep -m 1 cores /proc/cpuinfo";

    public static final String CPUMHz = "grep -m 1 MHz /proc/cpuinfo";

    public static final String MemTotal = "grep -m 1 MemTotal /proc/meminfo";

    public static final String MemFree = "grep -m 1 MemFree /proc/meminfo";


    private final PluginInfo pluginInfo = new PluginInfo();

    @Override
    public PluginInfo call() throws Exception {
        getCpuInfo();
        getMemoryInfo();
        getDiskInfo();
        getServices();
        getUptime();
        return pluginInfo;
    }

    
    /**
     * Obtem as informações do processador do recurso e realiza o setter dessa informações na classe pluginInfo.
     * Número de cores, frequencia do processador(GHz) e quantidade de cores ocupados.
     */
    private void getCpuInfo() {
        pluginInfo.setNumCores(Runtime.getRuntime().availableProcessors());
        pluginInfo.setNumNodes(1);
        pluginInfo.setNumOccupied(0);
        String cpuInfo = execCommand(CPUMHz);
        pluginInfo.setFrequencyCore((new Double(cpuInfo.substring(cpuInfo.indexOf(":") + 1, cpuInfo.length()).trim())) / 1000);
    }

    /**
     * Obtem as informações da memória RAM do recurso e realiza o setter dessa informações na classe pluginInfo.
     * Memória total e memória livre em MegaBytes.
     */
    private void getMemoryInfo() {
        String mem = execCommand(MemTotal);
        pluginInfo.setMemoryTotal((new Double(mem.substring(mem.indexOf(":") + 1, mem.length() - 2).trim()) / 1024));
        mem = execCommand(MemFree);
        pluginInfo.setMemoryFree((new Double(mem.substring(mem.indexOf(":") + 1, mem.length() - 2).trim()) / 1024));
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
        final List<PluginService> list = new CopyOnWriteArrayList<PluginService>();
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
