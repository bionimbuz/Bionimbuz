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
package br.unb.cic.bionimbus.plugin.linux;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.model.Workflow;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinuxPlugin extends AbstractPlugin{

    private final ExecutorService executorService = Executors.newCachedThreadPool(new BasicThreadFactory.Builder().namingPattern("LinuxPlugin-workers-%d").build());
    private static Logger LOGGER = LoggerFactory.getLogger(LinuxPlugin.class);
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
    public Future<PluginTask> startTask(PluginTask task, CloudMessageService zk, Workflow workflow) {
        PluginService service = getMyInfo().getService(task.getJobInfo().getServiceId());
        if (service == null) {
            LOGGER.info("[LinuxPlugin] Task's service is not installed on this instance.");
            return null;
        }
        return executorService.submit(new PluginTaskRunner(this, task, service, getConfig().getServerPath(), zk, workflow));
    }
}
