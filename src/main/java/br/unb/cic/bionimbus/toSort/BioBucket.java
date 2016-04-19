/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.toSort.CloudStorageMethods.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Lucas
 */
public class BioBucket {
    
    private StorageProvider provider;
    private String name;
    private String mountPoint;
    private String endPoint;
    private boolean mounted;
    private boolean inUse;
    
    private float bandwith; // em B/s
    private float latency;

    public BioBucket (StorageProvider provider, String name, String mountPoint) {
        this.provider = provider;
        this.name = name;
        this.mountPoint = mountPoint;
        mounted = false;
    }
    
    public StorageProvider getProvider() {
        return provider;
    }

    public void setProvider(StorageProvider provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public boolean isMounted() {
        return mounted;
    }

    public void setMounted(boolean mounted) {
        this.mounted = mounted;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }
    
    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endpoint) {
        this.endPoint = endpoint;
    }

    public float getBandwith() {
        return bandwith;
    }

    public void setBandwith(float bandwith) {
        this.bandwith = bandwith;
    }
    
    public float getLatency() {
        return latency;
    }

    public void setLatency(float latency) {
        this.latency = latency;
    }
    
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception ex) {
            Logger.getLogger(BioBucket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
