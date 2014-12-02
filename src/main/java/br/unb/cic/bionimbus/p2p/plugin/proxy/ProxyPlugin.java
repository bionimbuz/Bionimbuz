package br.unb.cic.bionimbus.p2p.plugin.proxy;

import java.util.concurrent.Future;

import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.AbstractPlugin;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginGetFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import java.io.IOException;

public class ProxyPlugin extends AbstractPlugin {

    public ProxyPlugin(P2PService p2p) throws IOException {
        super(p2p);

    }

    @Override
    protected Future<PluginInfo> startGetInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Future<PluginFile> saveFile(String filename) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Future<PluginGetFile> getFile(Host origin, PluginFile file,
                                            String taskId, String savePath) {
        // TODO Auto-generated method stub
        return null;
    }

        protected Future<PluginTask> startTask(PluginTask task) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<PluginTask> startTask(PluginTask task, ZooKeeperService zk) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
