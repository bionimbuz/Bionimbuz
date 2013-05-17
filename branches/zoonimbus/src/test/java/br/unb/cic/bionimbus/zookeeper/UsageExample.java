package br.unb.cic.bionimbus.zookeeper;

import br.unb.cic.bionimbus.network.utils.NetUtils;
import br.unb.cic.bionimbus.services.file.FileService;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 */
public class UsageExample {

    private static CopyOnWriteArraySet<String> peers = new CopyOnWriteArraySet<String>();

    private static ZooKeeperService zkService = new ZooKeeperService();

    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private static final String PREFIX_PEER = "peer_";

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        String peerID = UUID.randomUUID().toString();

        zkService.connect("localhost:2181");
        zkService.createPersistentZNode("/peers", null);

        String data = "id: " + UUID.randomUUID() + "\n" +
                "net-address: " + NetUtils.getAddress("wlan0") + "\n" +
                "cpu-cores: " + Runtime.getRuntime().availableProcessors() + "\n" +
                "disk-space: " + FileService.getFreeSpace("/");

        String peer = zkService.createEphemeralSequentialZNode(ROOT_PEER + SEPARATOR + PREFIX_PEER, data);

        System.out.println("Criado e registrado peer com id " + peer);

        List<String> list = zkService.getChildren(ROOT_PEER, new UpdatePeersZNode());
        if (list!= null) peers.addAll(list);

        System.out.println("_----------------------------------_");
        System.out.println(peers);
        System.out.println("_----------------------------------_");

        while (true) {

            TimeUnit.SECONDS.sleep(2);
            data = "id: " + peerID + "\n" +
                          "net-address: " + NetUtils.getAddress("wlan0") + "\n" +
                          "cpu-cores: " + Runtime.getRuntime().availableProcessors() + "\n" +
                          "disk-space: " + FileService.getFreeSpace("/");

            for (String p: peers) {
                if (peer.contains(p)){
                    System.out.println("Alterando dados => " + p);
                    zkService.setData(ROOT_PEER + SEPARATOR + p, data);
                }
                else {
                    System.out.println("Recuperando dados => " + p);
                    String res = zkService.getData(ROOT_PEER + SEPARATOR + p, new UpdatePeerData());
                    System.out.println("___________________________");
                    System.out.println(res);
                }
            }
        }
    }

    public static class UpdatePeerData implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            System.out.println(event);

        }
    }

    public static class UpdatePeersZNode implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            try {
                System.out.println("_----------------------------------_");
                System.out.println("WATCHER DISPARADO: " + event);
                List<String> list = zkService.getChildren(ROOT_PEER, this);
                if (list!= null) peers.addAll(list);
                System.out.println(peers);
                System.out.println("_----------------------------------_");

            } catch (KeeperException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
