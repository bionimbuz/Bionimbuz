package br.unb.cic.bionimbus.plugin.linux;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import br.unb.cic.bionimbus.plugin.AbstractPlugin;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskRunner;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import java.io.IOException;

public class LinuxPlugin extends AbstractPlugin{

    private final ExecutorService executorService = Executors.newCachedThreadPool(new BasicThreadFactory.Builder().namingPattern("LinuxPlugin-workers-%d").build());

    public LinuxPlugin(final BioNimbusConfig conf) throws IOException {
        super(conf);
    }
    
    public LinuxPlugin() throws IOException{
        this(null);
    }

    @Override
    protected Future<PluginInfo> startGetInfo() {
        
        return executorService.submit(new LinuxGetInfo());
    }

    @Override
    public Future<PluginTask> startTask(PluginTask task, CloudMessageService zk) {
        PluginService service = getMyInfo().getService(task.getJobInfo().getServiceId());
        if (service == null)
            return null;

        return executorService.submit(new PluginTaskRunner(this, task, service, getConfig().getServerPath(), zk));
    }

}
