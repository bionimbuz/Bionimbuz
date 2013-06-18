package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.p2p.ChordRing;
import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.p2p.PeerNode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class PluginInfo implements PluginOps {

    private String id;
    
    private String path_zk;
    
    private Host host;

    private long uptime;

    private Double latency;

    private long timestamp;

    private Integer numCores;

    private Integer numNodes;

    private Integer numOccupied;
    
    private final PeerNode peerNode=null;

    private Double ranking = 0d;

    private Float fsSize;

    private Double storagecost;
    
    private long rank;

    private Float fsFreeSize;

    private Double memoryTotal;

    private Double memoryFree;

    private Double frequencyCore;

    private List<PluginService> services;
    
    private final ChordRing chord=null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Endereço do plugin(peer) no zookeeper.
     * @return o endereço do plugin(peer) no zk de acordo com seu id 
     */
    public String getPath_zk() {
        path_zk ="/peers/peer_"+id;
        return path_zk;
    }
    public void setPath_zk(String path_zk) {
        this.path_zk =path_zk;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public long getUptime() {
        return System.currentTimeMillis() - uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
            
    }
   
    public Double getLatency() {
        return latency;
    }

    public void setLatency(Double latency) {
        this.latency = latency;
            
    }

    public double getStorageCost() {
        return storagecost;
    }

    public void setStorageCost(Double storagecost) {
        this.storagecost = storagecost;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Float getFsFreeSize() {
        return fsFreeSize;
    }

    public void setFsFreeSize(Float fsFreeSize) {
        this.fsFreeSize = fsFreeSize;
    }

    public Integer getNumCores() {
        return numCores;
    }

    public void setNumCores(Integer numCores) {
        this.numCores = numCores;
    }

    public Integer getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(Integer numNodes) {
        this.numNodes = numNodes;
    }

    public Float getFsSize() {
        return fsSize;
    }

    public void setFsSize(Float fsSize) {
        this.fsSize = fsSize;
    }

    public Integer getNumOccupied() {
        return numOccupied;
    }

    public void setNumOccupied(Integer numOccupied) {
        this.numOccupied = numOccupied;
    }

    public List<PluginService> getServices() {
        return services;
    }

    public void setServices(List<PluginService> services) {
        this.services = services;
    }

    public PluginService getService(long serviceId) {
        for (PluginService service : getServices())
            if (service.getId() == serviceId)
                return service;
        return null;
    }


    public Double getRanking() {
        return ranking;
    }

    public void setRanking(Double ranking) {
        this.ranking = ranking;
    }

    public Double getFrequencyCore() {
        return frequencyCore;
    }

    public void setFrequencyCore(Double frequencyCore) {
        this.frequencyCore = frequencyCore;
    }

    public Double getMemoryFree() {
        return memoryFree;
    }

    public void setMemoryFree(Double memoryFree) {
        this.memoryFree = memoryFree;
    }

    public Double getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(Double memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (!(object instanceof PluginInfo)) {
            return false;
        }

        PluginInfo other = (PluginInfo) object;

        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    //Alterado para retornar os valores que serão gravados no znode peer old: id
    @Override
    public String toString() {  
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception ex) {
            Logger.getLogger(PluginInfo.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }

}
