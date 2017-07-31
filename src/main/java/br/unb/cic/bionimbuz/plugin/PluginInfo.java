/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.plugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbuz.p2p.Host;

public class PluginInfo implements PluginOps {

    private String id;
    private String InstanceName;
    private int privateCloud;
    private Host host;
    private long uptime;
    private Double latency = 0d;
    private double costpergiga;
    private long timestamp;
    private Integer numCores;
    private Integer numNodes;
    private Integer numOccupied;
    private Double ranking = 0d;
    private Float fsSize;
    private double storagecost;
    private Float fsFreeSize;
    private Double memoryTotal;
    private Double memoryFree;
    private Double currentFrequencyCore;
    // frequency in Hz
    private Double factoryFrequencyCore;
    private List<PluginService> services;
    private double costPerHour = Double.MAX_VALUE;
    private Double bandwidth;
    private String ip;
    private String provider;

    public PluginInfo() {
    }

    public PluginInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstanceName() {
        return InstanceName;
    }

    public void setInstanceName(String instanceName) {
        this.InstanceName = instanceName;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public double getCostPerGiga() {
        return costpergiga;
    }

    public void setCostPerGiga(double costpergiga) {
        this.costpergiga = costpergiga;
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

    public void setStorageCost(double storagecost) {
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

    public PluginService getService(String serviceId) {
        for (PluginService service : getServices()) {
            if (service.getId().equals(serviceId)) {
                return service;
            }
        }
        return null;
    }

    public Double getRanking() {
        return ranking;
    }

    public void setRanking(Double ranking) {
        this.ranking = ranking;
    }

    public Double getCurrentFrequencyCore() {
        return currentFrequencyCore;
    }

    public void setCurrentFrequencyCore(Double frequencyCore) {
        this.currentFrequencyCore = frequencyCore;
    }

    public Double getFactoryFrequencyCore() {
        return factoryFrequencyCore;
    }

    public void setFactoryFrequencyCore(Double frequencyCore) {
        this.factoryFrequencyCore = frequencyCore;
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
        if (this == object) {
            return true;
        }

        if (!(object instanceof PluginInfo)) {
            return false;
        }

        PluginInfo other = (PluginInfo) object;

        return this.id.equals(other.id);
    }

    public int getPrivateCloud() {
        return privateCloud;
    }

    public void setPrivateCloud(int privateCloud) {
        this.privateCloud = privateCloud;
    }

    public void setCostPerHour(double costPerHour) {
        this.costPerHour = costPerHour;
    }

    public double getCostPerHour() {
        return costPerHour;
    }

    public Double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Double bandwidth) {
        this.bandwidth = bandwidth;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
