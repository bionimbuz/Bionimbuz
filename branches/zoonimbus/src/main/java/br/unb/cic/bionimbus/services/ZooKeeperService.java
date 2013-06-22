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
    
    private volatile Status status = Status.NO_CONNECTED;
    
    private volatile Path path = Path.ROOT;
    
    public enum Status {
        NO_CONNECTED, CONNECTING, CONNECTED
    }
    
    
    /**
     * Classe interna do ZookeeperService que possui, o método construtor Path(String value), 
     * método getFullPath(String pluginid,String fileid,String taskid), que retorna o caminho completo do znode   
     */
    public enum Path {
        
        ROOT("/"), PREFIX_PEER("/peer_"), PEERS("/peers"), FILES("/files"),PENDING_SAVE("/pending_save"),PREFIX_PENDING_FILE("pending_file_"),
        JOBS("/jobs"),PREFIX_FILE("/file_"),STATUS("/STATUS"),STATUSWAITING("/STATUSWAITING"),SCHED("/sched"),
        SIZE_JOBS("/size_jobs"),TASKS("/tasks"), PREFIX_TASK("/task_");
        
        private final String value;
        
        private Path(String value) {
            this.value = value;
        }
        
        /**
         * 
         * @param pluginid Os Enums, PREFIX_PEER, STATUS, STATUSWAITING, SCHED, SIZE_JOBS, TASKS, PREFIX_TASK, FILES, PREFIX_FILE utilizam como parametro
         * @param fileid OS enums, PREFIX_PENDING_FILE, PREFIX_FILE, utilizam o id dos file
         * @param taskid Os enums, PREFIX_TASK, utilizam
         * @return 
         */
        public String getFullPath(String pluginid,String fileid,String taskid) {
            switch (this) {
                case ROOT: return "" + this;
                    
                    case PENDING_SAVE: return "" +PENDING_SAVE;
                        case PREFIX_PENDING_FILE: return ""+PENDING_SAVE+PREFIX_PENDING_FILE+fileid;
                    case JOBS: return ""+JOBS;   
                    case PEERS:  return "" + PEERS;
                        case PREFIX_PEER: return ""+PEERS+PREFIX_PEER+pluginid;
                            case STATUS: return ""+PEERS+PREFIX_PEER+pluginid+STATUS;
                            case STATUSWAITING: return ""+PEERS+PREFIX_PEER+pluginid+STATUSWAITING;
                            case SCHED: return ""+PEERS+PREFIX_PEER+pluginid+SCHED;
                                case SIZE_JOBS: return ""+PEERS+PREFIX_PEER+pluginid+SCHED+SIZE_JOBS;
                                case TASKS: return ""+PEERS+PREFIX_PEER+pluginid+SCHED+TASKS;  
                                    case PREFIX_TASK: return ""+PEERS+PREFIX_PEER+pluginid+SCHED+TASKS+PREFIX_TASK+taskid;
                            case FILES: return ""+PEERS+PREFIX_PEER+pluginid+FILES;
                                case PREFIX_FILE: return ""+PEERS+PREFIX_PEER+pluginid+FILES+PREFIX_FILE+fileid;
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
    
    public ZooKeeperService() {
        System.out.println("Criando ZK service...");  
    }
    
    public Path getPath(){       
        return path;
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
    /**
     * Cria um znode persistent
     * @param root Path do znode
     * @param data Dado a ser gravado no znode
     * @return 
     */
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
    
    /**
     * 
     * @param root
     * @param data
     * @return 
     */
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
    /**
     * 
     * @param path
     * @param data
     * @return 
     */
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
    /**
     * 
     * @param path
     * @param data
     * @return 
     */
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
    
    /**
     * 
     * @param path
     * @param watch
     * @return
     * @throws KeeperException
     * @throws InterruptedException 
     */
    public Boolean getZNodeExist(String path, boolean watch) throws KeeperException, InterruptedException {
       Stat stat = zk.exists(path, watch);
       return (stat==null) ? Boolean.FALSE : Boolean.TRUE;
    }
    
    /**
     * 
     * @param path
     * @param watcher
     * @return
     * @throws KeeperException
     * @throws InterruptedException 
     */
    public List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zk.getChildren(path, watcher, null);
    }
    
    /**
     * 
     * @param path
     * @param watcher
     * @return
     * @throws KeeperException
     * @throws InterruptedException 
     */
    public String getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
        byte[] data = zk.getData(path, watcher, null);
        return new String(data);
    }
    
    /**
     * 
     * @param path
     * @param data
     * @throws KeeperException
     * @throws InterruptedException 
     */
    public void setData(String path, String data) throws KeeperException, InterruptedException {
        Stat stat = zk.setData(path, data.getBytes(), -1);
    }
    /**
     * 
     * @param path
     * @throws KeeperException
     * @throws InterruptedException 
     */
    public void delete(String path) throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(path, null);
        for(String child : children){
            delete(path+"/"+child);
        }
        zk.delete(path, -1);
    }
    
    /**
     * Método que fecha a conexão com o zookeeper 
     * @throws InterruptedException 
     */
    public void close() throws InterruptedException {
        try {
            zk.close();
        } finally {
            status = Status.NO_CONNECTED;
        }
    }
}
