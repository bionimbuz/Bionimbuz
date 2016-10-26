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
    private Double valueHour;
    private int quantity;
    private String locality;
    private String memory;
    private String cpuHtz;
    private String cpuType;
    private String hd;
    private String hdType;
    private int quantityCPU;
    private String description;
    private String cpuArch;
    private String provider;
    
    /**
     * Constructor
     */
    public Instance(){
//        this.id = "vazia";
//        this.type = "vazia";
//        this.valueHour = 0.0D;
//        this.quantity = 0;
//        this.locality = "vazia";
//        this.memory = "vazia";
//        this.cpuHtz = "vazia";
//        this.cpuType = "vazia";
//        this.quantityCPU=0;
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
     * @param valueHour
     * @param quantity
     * @param locality 
     * @param memory 
     * @param cpuHtz 
     * @param cpuType 
     * @param quantityCPU 
     * @param hd 
     * @param hdType 
     * @param cpuArch 
     * @param provider 
     */
    public Instance(String id, String type, Double valueHour, int quantity, 
            String locality, String memory, String cpuHtz, String cpuType, 
            int quantityCPU, String hd, String hdType,String cpuArch,
            String provider){
        this.id =id;
        this.type = type;
        this.valueHour = valueHour;
        this.quantity = quantity;
        this.locality = locality;
        this.memory = memory;
        this.cpuHtz = cpuHtz;
        this.cpuType = cpuType;
        this.quantityCPU=quantityCPU;
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
        return valueHour;
    }

    public void setValueHour(Double valueHour) {
        this.valueHour = valueHour;
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

    public String getMemory() {
        return memory;
    }


    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getCpuHtz() {
        return cpuHtz;
    }

    public void setCpuHtz(String cpuHtz) {
        this.cpuHtz = cpuHtz;
    }

    public String getCpuType() {
        return cpuType;
    }

    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }

    public String getHd() {
        return hd;
    }

    public void setHd(String hd) {
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
                + "CPU: "+ getQuantityCPU()+" "+ this.cpuHtz+", "
                + "CPUType: " + this.cpuType+", "
                + "Ram:" + this.memory + ", " 
                + "HD: " + this.hd + ", " 
                + "Custo por hora : " + this.valueHour+", "
                + "Quantidade: "+ this.quantity;
        
    }

    public String getDescription() {
        return description;
    }

    public void setDescription() {
        this.description = this.toString();
    }

    public int getQuantityCPU() {
        return quantityCPU;
    }

    public void setQuantityCPU(int quantityCPU) {
        this.quantityCPU = quantityCPU;
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
