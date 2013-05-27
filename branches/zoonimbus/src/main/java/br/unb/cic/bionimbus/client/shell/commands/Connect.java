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

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.jackson.map.ObjectMapper;

public class Connect implements Command {

    public static final String NAME = "connect";
    private ZooKeeperService zkService;
    private final SimpleShell shell;
    private long latency;
    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private List<String> children;
    private ConcurrentMap<String, PluginInfo> map = Maps.newConcurrentMap();

    public Connect(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {
        String configFile = System.getProperty("config.file", "conf/client.json");
        BioNimbusConfig config = BioNimbusConfigLoader.loadHostConfig(configFile);

        P2PService p2p = new P2PService(config);
        p2p.start();
        shell.setP2P(p2p);
        zkService = new ZooKeeperService();
        zkService.connect(p2p.getConfig().getZkHosts());

        children = zkService.getChildren(ROOT_PEER, null);
        
      /*  while(zkService.getStatus()!=zkService.getStatus().CONNECTED)
            ;
        while (p2p.getPeers().isEmpty())
			;
        */
        String childStr;
        for (String child : children) {
            try {
                childStr = zkService.getData(ROOT_PEER + SEPARATOR + child, null);
                System.out.println(childStr);
                ObjectMapper mapper = new ObjectMapper();
                PluginInfo myInfo = mapper.readValue(childStr, PluginInfo.class);
                map.put(myInfo.getId(), myInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (PluginInfo a : map.values()) {
            System.out.println("no" + a.getHost().getAddress());
            System.out.println("espaço" + a.getFsSize());

            //instaciar objetos
            StoragePolicy policy = new StoragePolicy();
            Ping ping = new Ping();

            //calculo da latencia
            latency = Ping.calculo(a.getHost().getAddress());
            a.setLatency(latency);

            //calculo dos custo de armazenamento
            a.setStorageCost(policy.calcBestCost(latency));
            System.out.println("\n Ip: " + a.getHost().getAddress() + "\n Custo de armazenamento: " + a.getStorageCost());
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
