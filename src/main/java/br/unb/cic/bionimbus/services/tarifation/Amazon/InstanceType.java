/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Amazon;

/**
 *
 * @author fritz
 */
public class InstanceType {
    
    private int id;
    private int core;
    private double ecu;
    private double ram;
    private String network;
    private String model;
    private String storageCount;
    private String storageSize;
    private String storageSSD;
    private Boolean windows;
    private String createdAt;
    private String updatedAt;
    private Boolean hvm;
    private String cpu;

    public InstanceType(int id, int core, double ecu, double ram, String network, String model, String storageCount, String storageSize, String storageSSD, Boolean windows, String createdAt, String updatedAt, Boolean hvm, String cpu) {
        this.id = id;
        this.core = core;
        this.ecu = ecu;
        this.ram = ram;
        this.network = network;
        this.model = model;
        this.storageCount = storageCount;
        this.storageSize = storageSize;
        this.storageSSD = storageSSD;
        this.windows = windows;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hvm = hvm;
        this.cpu = cpu;
    }

    public String getId() {
        return id +"";
    }

    public String getCore() {
        return core +"";
    }

    public String getEcu() {
        return ecu+"";
    }

    public String getRam() {
        return ram+"";
    }

    public String getNetwork() {
        return network;
    }

    public String getModel() {
        return model;
    }

    public String getStorageCount() {
        return storageCount;
    }

    public String getStorageSize() {
        return storageSize;
    }

    public String getStorageSSD() {
        return storageSSD;
    }

    public Boolean getWindows() {
        return windows;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Boolean getHvm() {
        return hvm;
    }

    public String getCpu() {
        return cpu;
    }
    
    
    
    
    
    
}
