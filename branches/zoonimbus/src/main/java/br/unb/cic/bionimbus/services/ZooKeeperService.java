package br.unb.cic.bionimbus.services;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Singleton
public class ZooKeeperService {

    /**
     * URL: http://twitter.github.io/commons/apidocs/com/twitter/common/zookeeper/package-frame.html
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperService.class);

    private ZooKeeper zk;
    private static final int SESSION_TIMEOUT = 3000;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    
    public enum Status {
        NO_CONNECTED, CONNECTING, CONNECTED
    }
    
    public enum Path {
        
        ROOT("/"), PREFIX_PEERS("/peers_"), PEER("/peer"), FILES("/files");
        
        private final String value;
        
        private Path(String value) {
            this.value = value;
        }
        
        public String getFullPath() {
            switch (this) {
                case ROOT: return "" + this;
                case PEER:  return "" + PREFIX_PEERS + PEER;
                // 
            }
            return "";
        }
        
        public String getCodigo() {
            return value;
        }
        
        @Override 
        public String toString() {
            return value;
        }
        
    }
           
    private volatile Status status = Status.NO_CONNECTED;

    public ZooKeeperService() {
        System.out.println("Criando ZK service...");
        
        
    }

    public Status getStatus() {
        return status;
    }


    public synchronized void connect(String hosts) throws IOException, InterruptedException {

        Preconditions.checkNotNull(hosts, "zkHosts cannot be null");

        status = Status.CONNECTING;

        System.out.println("Conectando ao ZK...");
        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

                // Evento que indica conexão ao ensemble
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Conectado ao ZK!");
                    status = Status.CONNECTED;
                    countDownLatch.countDown();
                }
                System.out.println(event);
            }
        });

        // Espera pelo evento de conexão
        countDownLatch.await();
    }

    public String createPersistentZNode(String root, String data) {
        String peer=null;
        if (zk != null) {
            try {
                Stat s = zk.exists(root, false);
                if (s == null) {
                    System.out.println(String.format("znode %s não existe ... criando", root));
                    peer = zk.create(root
                            , (data == null) ? new byte[0] : data.getBytes()
                            , ZooDefs.Ids.OPEN_ACL_UNSAFE // sem segurança
                            , CreateMode.PERSISTENT);
                } else {
                    System.out.println(String.format("znode %s existente", root));
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return peer;
    }

    public String createPersistentSequentialZNode(String root, String data) {
        String peer = null;
        if (zk != null) {
            try {
                Stat s = zk.exists(root, false);
                if (s == null) {
                    System.out.println(String.format("znode %s não existe ... criando", root));
                    peer = zk.create(root
                            , (data == null) ? new byte[0] : data.getBytes()
                            , ZooDefs.Ids.OPEN_ACL_UNSAFE // sem segurança
                            , CreateMode.PERSISTENT_SEQUENTIAL);
                } else {
                    System.out.println(String.format("znode %s existente", root));
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return peer;
    }
    
    public String createEphemeralZNode(final String path, String data) {
        String peer = null;
        byte[] buf = (data != null) ? data.getBytes() : new byte[0];
        try {
            Stat s = zk.exists(path, false);
            if (s == null) {
            peer = zk.create(path
                    , buf
                    , ZooDefs.Ids.OPEN_ACL_UNSAFE
                    , CreateMode.EPHEMERAL);
                System.out.println(String.format("znode %s criado", path));
            } else {
                System.out.println(String.format("znode %s existente", path));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

        return peer;
    }
    
    public String createEphemeralSequentialZNode(final String path, String data) {
        String peer = null;
        byte[] buf = (data != null) ? data.getBytes() : new byte[0];
        try {
            Stat s = zk.exists(path, false);
            if (s == null) {
            peer = zk.create(path
                    , buf
                    , ZooDefs.Ids.OPEN_ACL_UNSAFE
                    , CreateMode.EPHEMERAL_SEQUENTIAL);
            } else {
                System.out.println(String.format("znode %s existente", path));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

        return peer;
    }

    public Boolean getZNodeExist(String path, boolean watch) throws KeeperException, InterruptedException {
       Stat stat = zk.exists(path, watch);
       return (stat==null) ? Boolean.FALSE : Boolean.TRUE;
    }
    
    public List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zk.getChildren(path, watcher, null);
    }

    public String getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
        byte[] data = zk.getData(path, watcher, null);
        return new String(data);
    }
    
    public void setData(String path, String data) throws KeeperException, InterruptedException {
        Stat stat = zk.setData(path, data.getBytes(), -1);
    }

    public void delete(String path) throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(path, null);
        for(String child : children){
            delete(path+"/"+child);
        }
        zk.delete(path, -1);
    }

    public void close() throws InterruptedException {
        try {
            zk.close();
        } finally {
            status = Status.NO_CONNECTED;
        }
    }
}
