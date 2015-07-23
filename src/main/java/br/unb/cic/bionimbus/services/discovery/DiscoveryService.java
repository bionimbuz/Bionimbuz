package br.unb.cic.bionimbus.services.discovery;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbus.services.AbstractBioService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.toSort.Listeners;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.WatchedEvent;

@Singleton
public class DiscoveryService extends AbstractBioService {

    private static final int PERIOD_SECS = 10;
    private final ScheduledExecutorService schedExecService;
    private static final String SEPARATOR = "/";
    private static final String STATUS = "STATUS";
    private static final String STATUSWAITING = "STATUSWAITING";
    private final ConcurrentMap<String, PluginInfo> map = Maps.newConcurrentMap();

    @Inject
    public DiscoveryService(final CloudMessageService cms) {

        Preconditions.checkNotNull(cms);
        this.cms = cms;

        schedExecService = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("DiscoveryService-%d")
                .build());
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getStackTrace());
        setDatasPluginInfo(false);
    /**
     * TODO: substituir por Guava Cache com expiração
     */

     }
    public void setDatasPluginInfo(boolean start) {
        try {
            
            LinuxGetInfo getinfo=new LinuxGetInfo();
            PluginInfo infopc= getinfo.call();
            
            infopc.setId(config.getId());
            
            if(start){
            // LinuxPlugin está contido nesse metodo, e deveria ser mandado 
            // para o linuxplugin Bionimbus.java
                LinuxPlugin linuxPlugin = new LinuxPlugin(config);

                infopc.setHost(config.getHost());
                
// Update uptime information to origin from zookeeper ---------------------------------------------------------------------------
                //infopc.setUptime(p2p.getPeerNode().uptime());
                
                infopc.setPrivateCloud(config.getPrivateCloud());

                //definindo myInfo após a primeira leitura dos dados
                linuxPlugin.setMyInfo(infopc);
                listeners.add(linuxPlugin);
            }else{
                String data = cms.getData(infopc.getPath_zk(), null);
                if (data == null || data.trim().isEmpty()){
                    System.out.println("znode vazio para path " + infopc.getPath_zk());
                    return;
                }
               
                    
                PluginInfo plugin = new ObjectMapper().readValue(data, PluginInfo.class);
                plugin.setFsFreeSize(infopc.getFsFreeSize());
                plugin.setMemoryFree(infopc.getMemoryFree());
                plugin.setNumOccupied(infopc.getNumOccupied());
                infopc.setUptime(plugin.getUptime());
                infopc = plugin;
            }
            //armazenando dados do plugin no zookeeper
            cms.setData(infopc.getPath_zk(), infopc.toString());
            
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void start(BioNimbusConfig config, List<Listeners> listeners) {
        try {
            Preconditions.checkNotNull(listeners);
            this.config = config;
            this.listeners = listeners;
            
            setDatasPluginInfo(true);
            
            listeners.add(this);
          
            schedExecService.scheduleAtFixedRate(this, 0, PERIOD_SECS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void getStatus() {
    }
    
    /**
     * Trata os watchers enviados da implementação da classe Watcher que recebe uma notificação do zookeeper
     * @param eventType evento recebido do zookeeper
     */
    @Override
    public void event(WatchedEvent eventType) {
        
         }


    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verifyPlugins() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
