package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.p2p.Host;
import java.util.List;

public class PluginInfo implements PluginOps {

    private String id;

    private Host host;

    private long uptime;

    private long latency;

    private long timestamp;

    private Integer numCores;

    private Integer numNodes;

    private Integer numOccupied;

    private Long ranking = new Long(0l);

    private Float fsSize;
    
    private long storagecost;

    private Float fsFreeSize;

    private Double MemoryTotal;

    private Double MemoryFree;

    private Double FrequencyCore;

    private List<PluginService> services;

    public String getId() {
            return id;
    }

    public void setId(String id) {
            this.id = id;
    }

    public Host getHost() {
            return host;
    }

    public void setHost(Host host) {
            this.host = host;
    }

    public long getUptime() {
            return uptime;
    }

    public void setUptime(long uptime) {
            this.uptime = uptime;
    }

    public float getLatency() {
            return latency;
    }

    public void setLatency(long latency) {
            this.latency = latency;
    }
    
    public long getStorageCost() {
            return storagecost;
    }

    public void setStorageCost(long storagecost) {
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


    public Long getRanking() {
        return ranking;
    }

    public void setRanking(Long ranking) {
        this.ranking = ranking;
    }

    public Double getFrequencyCore() {
        return FrequencyCore;
    }

    public void setFrequencyCore(Double FrequencyCore) {
        this.FrequencyCore = FrequencyCore;
    }

    public Double getMemoryFree() {
        return MemoryFree;
    }

    public void setMemoryFree(Double MemoryFree) {
        this.MemoryFree = MemoryFree;
    }

    public Double getMemoryTotal() {
        return MemoryTotal;
    }

    public void setMemoryTotal(Double MemoryTotal) {
        this.MemoryTotal = MemoryTotal;
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

    @Override
    public String toString() {
            return id.toString();
    }
}
