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

	public double gpuMemoryTotal,
			gpuMemoryMaxFrequency,
			gpuMemoryBus,
			gpuMemoryBandwith,
			gpuFloatingPointPerf;
	public double gpuMaxFrequency;


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

    public String Serialize(){
	    String ret= "PLUGIN_INFO";
	    ret+= '\n';
	    
	    ret+= "id=";
	    ret+= id;
	    ret+= '\n';
	    
	    ret+= "instanceName=";
	    ret+= getInstanceName();
	    ret+= '\n';
	    
	    ret+= "privateCloud=";
	    ret+= String.valueOf(privateCloud);
	    ret+= '\n';
	    
	    ret+= "host=";
	    ret+= host.getAddress();
	    ret+= ':';
	    ret += String.valueOf(host.getPort());
	    ret+= '\n';
	    
	    ret+= "upTime=";
	    ret+= String.valueOf(getUptime());
	    ret+= '\n';
	    
	    ret+= "latency=";
	    ret+= String.valueOf(latency);
	    ret+= '\n';
	    
	    ret+= "costPerGiga=";
	    ret+= String.valueOf(costpergiga);
	    ret+= '\n';
	    
	    ret+= "timestamp=";
	    ret+= String.valueOf(timestamp);
	    ret+= '\n';
	    
	    ret+= "numCores=";
	    ret+= String.valueOf(numCores);
	    ret+= '\n';
	    
	    ret+= "numNodes=";
	    ret+= String.valueOf(numNodes);
	    ret+= '\n';
	    
	    ret+= "numOccupied=";
	    ret+= String.valueOf(numOccupied);
	    ret+= '\n';
	    
	    ret+= "ranking=";
	    ret+= String.valueOf(ranking);
	    ret+= '\n';
	    
	    ret+= "fsSize=";
	    ret+= String.valueOf(fsSize);
	    ret+= '\n';
	    
	    ret+= "memoryTotal=";
	    ret+= String.valueOf(memoryTotal);
	    ret+= '\n';
	    
	    ret+= "memoryFree=";
	    ret+= String.valueOf(memoryFree);
	    ret+= '\n';
	    
	    ret+= "currentFrequencyCore=";
	    ret+= String.valueOf(currentFrequencyCore);
	    ret+= '\n';
	    
	    ret+= "costPerHour=";
	    ret+= String.valueOf(costPerHour);
	    ret+= '\n';
	    
	    ret+= "bandwith=";
	    ret+= String.valueOf(getBandwidth());
	    ret+= '\n';
	    
	    ret+= "ip=";
	    ret+= ip;
	    ret+= '\n';
	    
	    ret+= "provider=";//obs:: n pode ter \n no provider
	    ret+= provider;
	    ret+= '\n';
	    
	    //adições para suporte de GPU
	    ret+= "gpuMemoryTotal=";
	    ret+= String.valueOf(gpuMemoryTotal);
	    ret+= '\n';
	    
	    ret+= "gpuMemoryMaxFrequency=";
	    ret+= String.valueOf(gpuMemoryMaxFrequency);
	    ret+= '\n';
	    
	    ret+= "gpuMemoryBus=";
	    ret+= String.valueOf(gpuMemoryBus);
	    ret+= '\n';
	    
	    ret+= "gpuMemoryBandwith=";
	    ret+= String.valueOf(gpuMemoryBandwith);
	    ret+= '\n';
	    
	    ret+= "gpuFloatingPointPerf=";
	    ret+= String.valueOf(gpuFloatingPointPerf);
	    ret+= '\n';
	    
	    ret+= "gpuMaxFrequency=";
	    ret+= String.valueOf(gpuMaxFrequency);
	    ret+= '\n';
	    
	    return ret;
    }

}
