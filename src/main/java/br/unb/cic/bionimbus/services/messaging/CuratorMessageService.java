/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.unb.cic.bionimbus.services.messaging;

import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.LoggerFactory;

/**
 * Manages the ZooKeeper connection, ZNodes, reconnection... Uses Curator Framework
 * @author willian
 */
@Singleton
public class CuratorMessageService implements CloudMessageService {
    
    CuratorFramework client;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CuratorMessageService.class);
    private volatile Path path = CuratorMessageService.Path.ROOT;
    
    public CuratorMessageService() {
        LOGGER.info("Criando Curator service...");  
    }
    
    @Override
    public synchronized void connect(String connectionString) {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        
        client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        client.start();
    }
    
    @Override
    public Path getPath() {
        return path;
    }
    
    /**
     * Internal Enum that handles Paths for BioNimbuZ proccessing ZNodes
     */
    public enum Path {
        
        COST_PREFIX("/cost_"),
        COUNT("/count"),
        END("/end"),
        FILES("/files"),
        HISTORY("/history"),
        LATENCY("/latency"),
        MODES("/modes"),
        PEERS("/peers"), 
        PENDING_SAVE("/pending_save"),
        PIPELINES("/pipelines"),
        PIPELINE_FLAG("/flag"),
        PREFIX_FILE("/file_"),
        PREFIX_PEER("/peer_"), 
        PREFIX_PENDING_FILE("/pending_file_"),
        PREFIX_PIPELINE("/pipeline_"),
        PREFIX_SERVICE("/service_"),
        PREFIX_TASK("/task_"),
        PRESET("/PRESET"),
        ROOT("/"), 
        SCHED("/sched"),
        SEPARATOR("/"),
        SERVICES("/services"),
        SIZE_JOBS("/size_jobs"),
        START("/start"),
        STATUS("/STATUS"),
        STATUSWAITING("/STATUSWAITING"),
        STEP("/step"),
        TASKS("/tasks"), 
        UNDERSCORE("_");
        
        private final String value;
        
        private Path(String value) {
            this.value = value;
        }
        
        /**
         * Return the full path of a ZNode
         * @param args Input String arguments. For the most cases there will be
         * only one id input. For cases ROOT, PENDING_SAVE, PEERS, PIPELINES 
         * and SERVICES there will be no input. For the PREFIX_TASK case, the 
         * first argument must be the plugin id and the second, the task id. For
         * the PREFIX_FILE case the first argument is the plugin id and the 
         * second, the file id.
         * @return 
         */
        public String getFullPath(String ... args) {
            switch (this) {
                case ROOT: return "" + this;
                case PENDING_SAVE: return "" +PENDING_SAVE;
                case PREFIX_PENDING_FILE: return ""+PENDING_SAVE+PREFIX_PENDING_FILE+args[0];
                case PEERS:  return "" + PEERS;
                case PREFIX_PEER: return ""+PEERS+PREFIX_PEER+args[0];
                case STATUS: return ""+PEERS+PREFIX_PEER+args[0]+STATUS;
                case STATUSWAITING: return ""+PEERS+PREFIX_PEER+args[0]+STATUSWAITING;
                case SCHED: return ""+PEERS+PREFIX_PEER+args[0]+SCHED;
                case SIZE_JOBS: return ""+PEERS+PREFIX_PEER+args[0]+SCHED+SIZE_JOBS;
                case TASKS: return ""+PEERS+PREFIX_PEER+args[0]+SCHED+TASKS;
                case PREFIX_TASK: return ""+PEERS+PREFIX_PEER+args[0]+SCHED+TASKS+PREFIX_TASK+args[1];
                case FILES: return ""+PEERS+PREFIX_PEER+args[0]+FILES;
                case PREFIX_FILE: return ""+PEERS+PREFIX_PEER+args[0]+FILES+PREFIX_FILE+args[1];
                case PREFIX_PIPELINE: return "" + PIPELINES + PREFIX_PIPELINE + args[0];
                case PIPELINE_FLAG: return "" + PIPELINES + PREFIX_PIPELINE + args[0] + PIPELINE_FLAG;
                case PIPELINES: return "" + PIPELINES;
                case SERVICES: return "" + SERVICES;
                case PREFIX_SERVICE: return "" + SERVICES + PREFIX_SERVICE + args[0];
                case HISTORY: return "" + SERVICES + PREFIX_SERVICE + args[0] + HISTORY;
                case STEP: return "" + SERVICES + PREFIX_SERVICE + args[0] + STEP;
                case MODES: return "" + SERVICES + PREFIX_SERVICE + args[0] + MODES;
                case PRESET: return "" + SERVICES + PREFIX_SERVICE + args[0] + MODES + PRESET;
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
    
    /**
     * Creates a new ZNode
     * @param cm
     * @param node
     * @param desc 
     */
    @Override
    public void createZNode(CreateMode cm, String node, String desc) {
        try {
            if (!getZNodeExist(node, null)) {
                client.create().withMode(cm).
                                withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).
                                forPath(node, (desc == null) ? new byte[0] : desc.getBytes());
            } else {
                Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, "Existent node {0}" + Arrays.toString(Thread.currentThread().getStackTrace()), node);
            }
        } catch (Exception ex) {
            Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Check if a ZNode exists
     * @param path
     * @param watcher
     * @return 
     */
    @Override
    public Boolean getZNodeExist(String path, Watcher watcher) {
        // Need to know how to use watchers in this method (Zookeeper Watcher or Curator Watcher?)
        Stat s = null;
        try {
            s = client.checkExists().usingWatcher(watcher).forPath(path);
        } catch (Exception ex) {
            Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }    
        return s != null; 
        
    }
    
    /**
     * Get a list of children's paths of a ZNode
     * @param path
     * @param watcher
     * @return 
     */
    @Override
    public List<String> getChildren(String path, Watcher watcher) {        
        try {
            return client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception ex) {
            Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    /**
     * Retrieves the data in a ZNode
     * @param path
     * @param watcher
     * @return 
     */
    @Override
    public String getData(String path, Watcher watcher){
        byte[] data;
        String ret = null;
        
        try {
            data = client.getData().usingWatcher(watcher).forPath(path);
            ret = new String(data);
        } catch (Exception ex) {
            Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    /**
     * Set the data in a determined ZNode
     * @param path
     * @param data 
     */
    @Override
    public void setData(String path, String data) {
        try {
            client.setData().forPath(path, data.getBytes());
        } catch (Exception ex) {
            Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Deletes a ZNode
     * @param path 
     */
    @Override
    public void delete(String path) {
        try {            
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception ex) {
            Logger.getLogger(CuratorMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Close ZooKeeper connection
     */
    @Override
    public void close(){
        client.close();
    }
    
}
