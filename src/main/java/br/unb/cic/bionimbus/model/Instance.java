/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.model;

/**
 * Class that defines the Instance
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
    private Double hd;
    private String hdType;
    private Integer numCores;
    private String description;
    private String cpuArch;
    private String provider;
    
    /**
     * Constructor
     */
    public Instance(){
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
     * Constructor
     * @param id
     * @param type
     * @param costPerHour
     * @param quantity
     * @param locality 
     * @param memoryTotal 
     * @param cpuHtz 
     * @param cpuType 
     * @param numCores 
     * @param hd 
     * @param hdType 
     * @param cpuArch 
     * @param provider 
     */
    public Instance(String id, String type, Double costPerHour, int quantity, 
            String locality, Double memoryTotal, Double cpuHtz, String cpuType, 
            int numCores, Double hd, String hdType,String cpuArch,
            String provider){
        this.id =id;
        this.type = type;
        this.costPerHour = costPerHour;
        this.quantity = quantity;
        this.locality = locality;
        this.memoryTotal = memoryTotal;
        this.cpuHtz = cpuHtz;
        this.cpuType = cpuType;
        this.numCores=numCores;
        this.hd = hd;
        this.hdType = hdType;
        this.cpuArch= cpuArch;
        this.provider=provider;
        setDescription();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getValueHour() {
        return costPerHour;
    }

    public void setValueHour(Double costPerHour) {
        this.costPerHour = costPerHour;
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
        return memoryTotal;
    }


    public void setMemory(Double memoryTotal) {
        this.memoryTotal = memoryTotal;
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

    public Double getHd() {
        return hd;
    }

    public void setHd(Double hd) {
        this.hd = hd;
    }

    public String getHdType() {
        return hdType;
    }

    public void setHdType(String hdType) {
        this.hdType = hdType;
    }
    
     @Override
    public String toString() {
        
        return "Type: "+ this.type +", "
                + "CPU: "+ this.numCores+" "+ this.cpuHtz+" Ghz, "
                + "CPUType: " + this.cpuType+", "
                + "Ram:" + this.memoryTotal + " GB, " 
                + "HD: " + this.hd + " GB "+ this.hdType+", " 
                + "Custo por hora : $" + this.costPerHour+", "
                + "Quantidade: "+ this.quantity+", "
                + "Localidade: "+this.locality+", "
                + "Provider: "+this.provider+", "
                + "Id: "+this.id;
        
    }

    public String getDescription() {
        return description;
    }

    public void setDescription() {
        this.description = this.toString();
    }

    public int getQuantityCPU() {
        return numCores;
    }

    public void setQuantityCPU(int numCores) {
        this.numCores = numCores;
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
}
