package br.unb.cic.bionimbus.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;

import br.unb.cic.bionimbus.services.storage.StorageService;
import br.unb.cic.bionimbus.utils.GetIpMac;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import org.apache.zookeeper.KeeperException;

@Singleton
public class ServiceManager {

    private final Set<Service> services = new HashSet<Service>();
    private final ZooKeeperService zkService;

    private final RpcServer rpcServer;
    
    private static final String ROOT_PEER = "/peers";
    
    private static final String SEPARATOR = "/";
    private static final String PEERS = ROOT_PEER+SEPARATOR+"peer_";
    private static final String STATUS = "STATUS";
    private final HttpServer httpServer;

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public ServiceManager(Set<Service> services, ZooKeeperService zkService, RpcServer rpcServer, HttpServer httpServer) {
        this.zkService = zkService;
        this.rpcServer = rpcServer;
        this.httpServer = httpServer;

        this.services.addAll(services);
    }

    public void connectZK(String hosts) throws IOException, InterruptedException {
        System.out.println("conectando ao ZooKeeperService...");
        if (zkService.getStatus() != ZooKeeperService.Status.CONNECTED
                && zkService.getStatus() != ZooKeeperService.Status.CONNECTING) {
            zkService.connect(hosts);
        }
    }
    
    public void createZnodeZK(String id) throws IOException, InterruptedException, KeeperException {
        if (zkService.getStatus() == ZooKeeperService.Status.CONNECTED) {
           
            //inves de criar direto verifica se existe senão cria? Breno
            zkService.createPersistentZNode(ROOT_PEER, null);
                
           //criando zNode persistente para cada novo peer
           zkService.createPersistentZNode(PEERS+ id, null);
          
           //criando status efemera para verificar se o servidor esta rodando
           zkService.createEphemeralZNode(PEERS+ id+SEPARATOR+STATUS, null);
                   
            //System.out.println("Criado e registrado peer com path " + PEERS+ id);
        
        }
    }

    public void register(Service service) {
        services.add(service);
    }

    public void startAll(P2PService p2p) {
        try {
            rpcServer.start();
            httpServer.start();
            connectZK(p2p.getConfig().getZkHosts());
            //Breno modifiquei aqui para criar o peer com o valor randomico comum para todos.
//            LinuxGetInfo getinfo=new LinuxGetInfo();
//            LinuxPlugin linuxPlugin = new LinuxPlugin(p2p);
//          
//            PluginInfo infopc= getinfo.call();
//            infopc.setId(p2p.getConfig().getId());
//            infopc.setHost(p2p.getConfig().getHost());
//            infopc.setUptime(p2p.getPeerNode().uptime());
////            infopc.setPath_zk(ROOT_PEER+SEPARATOR+PREFIX_PEER+infopc.getId());
//           
//            //definindo myInfo após a leitura dos dados
//            linuxPlugin.setMyInfo(infopc); 
            
            createZnodeZK(p2p.getConfig().getId());
//            //armazenando dados do plugin no zookeeper
//            zkService.setData(infopc.getPath_zk(), infopc.toString());          
            for (Service service : services) {
                //perguntar pro edward pq é separado a chamada do storage
                if (service instanceof StorageService) {
                    ((StorageService) service).run();
                }
                service.start(p2p);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }
}
