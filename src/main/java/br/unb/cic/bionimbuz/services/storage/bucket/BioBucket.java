/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.storage.bucket;

import br.unb.cic.bionimbuz.services.storage.bucket.CloudStorageMethods.*;
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
    
    private float upBandwith; // em B/s
    private float dlBandwith; // em B/s
    private float latency;

    /**
     * Constructor for bucket, name of bucket(Intervalo) createad on amazon s3 or google storage
     * @param provider
     * @param name 
     * @param mountPoint 
     */
    public BioBucket(StorageProvider provider, String name, String mountPoint) {
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

    public float getUpBandwith() {
        return upBandwith;
    }

    public void setUpBandwith(float upBandwith) {
        this.upBandwith = upBandwith;
    }

    public float getDlBandwith() {
        return dlBandwith;
    }

    public void setDlBandwith(float dlBandwith) {
        this.dlBandwith = dlBandwith;
    }
    
    public float getAvgBandwith() {
        float avg = (upBandwith + dlBandwith)/2;
        
        return avg;
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
