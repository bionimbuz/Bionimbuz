package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.services.storage.StoragePolicy;
import br.unb.cic.bionimbus.zookeeper.ZooKeeperService;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.codehaus.jackson.map.ObjectMapper;

public class Connect implements Command {

    public static final String NAME = "connect";
    private ZooKeeperService zkService;
    private final SimpleShell shell;
    private long latency;
    private long cost;
    private String bestplugin;
    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private List<String> children;
    private ConcurrentMap<String, PluginInfo> map = Maps.newConcurrentMap();

    public Connect(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {

        if (params.length < 2) {
            throw new InvalidArgumentException(new String[]{"usage: connect <address> <port>"});
        }
    
        shell.setConnected(true);

        return "client is connected.";
    }

    @Override
    public String usage() {
        return NAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }
}
