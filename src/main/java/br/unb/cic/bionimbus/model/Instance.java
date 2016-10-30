/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.model;

/**
 * Class that defines the Instance
 *
 * @author brenokx
 */
public class Instance {

    private String id;
    private String type;
    private Double costPerHour;
    private int quantity;
    private String locality;
    private Double memoryTotal;
    private Double cpuHtz;
    private String cpuType;
    private StorageInstance storage;
    private Integer numCores;
    private String description;
    private String cpuArch;
    private String provider;

    /**
     * Constructor
     */
    public Instance() {
//        this.id = "vazia";
//        this.type = "vazia";
//        this.costPerHour = 0.0D;
//        this.quantity = 0;
//        this.locality = "vazia";
//        this.memoryTotal = "vazia";
//        this.cpuHtz = "vazia";
//        this.cpuType = "vazia";
//        this.numCores=0;
//        this.hd = "vazia";
//        this.hdType = "vazia";
//        this.cpuArch= "vazia";
//        this.provider = "vazia";
//        setDescription();
    }

    /**
     *
     * @param id
     * @param type
     * @param costPerHour
     * @param quantity
     * @param locality
     * @param memoryTotal
     * @param cpuHtz
     * @param cpuType
     * @param storage
     * @param numCores
     * @param cpuArch
     * @param provider
     */
    public Instance(String id, String type, Double costPerHour, int quantity,
            String locality, Double memoryTotal, Double cpuHtz, String cpuType,
            StorageInstance storage, Integer numCores,
            String cpuArch, String provider) {
        this.id = id;
        this.type = type;
        this.costPerHour = costPerHour;
        this.quantity = quantity;
        this.locality = locality;
        this.memoryTotal = memoryTotal;
        this.cpuHtz = cpuHtz;
        this.cpuType = cpuType;
        this.storage = storage;
        this.numCores = numCores;
        this.cpuArch = cpuArch;
        this.provider = provider;
        setDescription();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getValueHour() {
        return getCostPerHour();
    }

    public void setValueHour(Double costPerHour) {
        this.setCostPerHour(costPerHour);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public Double getMemory() {
        return getMemoryTotal();
    }

    public void setMemory(Double memoryTotal) {
        this.setMemoryTotal(memoryTotal);
    }

    public Double getCpuHtz() {
        return cpuHtz;
    }

    public void setCpuHtz(Double cpuHtz) {
        this.cpuHtz = cpuHtz;
    }

    public String getCpuType() {
        return cpuType;
    }

    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }

    @Override
    public String toString() {

        return "Type: " + this.getType() + ", "
                + "CPU: " + this.getNumCores() + " " + this.getCpuHtz() + " Ghz, "
                + "CPUType: " + this.getCpuType() + ", "
                + "Ram:" + this.getMemoryTotal() + " GB, "
                + "Custo por hora : $" + this.getCostPerHour() + ", "
                + "Quantidade: " + this.getQuantity() + ", "
                + "Localidade: " + this.getLocality() + ", "
                + "Provider: " + this.getProvider() + ", "
                + "Id: " + this.getId();

    }

    public String getDescription() {
        return description;
    }

    public void setDescription() {
        this.setDescription(this.toString());
    }

    public int getQuantityCPU() {
        return getNumCores();
    }

    public void setQuantityCPU(int numCores) {
        this.setNumCores((Integer) numCores);
    }

    public String getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCpuArch() {
        return cpuArch;
    }

    public void setCpuArch(String cpuArch) {
        this.cpuArch = cpuArch;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(Double costPerHour) {
        this.costPerHour = costPerHour;
    }

    public Double getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(Double memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public StorageInstance getStorage() {
        return storage;
    }

    public void setStorage(StorageInstance storage) {
        this.storage = storage;
    }

    public Integer getNumCores() {
        return numCores;
    }

    public void setNumCores(Integer numCores) {
        this.numCores = numCores;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
