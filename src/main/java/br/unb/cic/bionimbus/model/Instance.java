/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.model;


import java.util.UUID;

/**
 * Class that defines the Instance
 * @author brenokx
 */
public class Instance {
    private String id = UUID.randomUUID().toString();
    private String type;
    private Double valueHour;
    private int quantity;
    private String locality;
    private Double memory;
    private Double cpuHtz;
    private String cpuType;
    private Double hd;
    private String hdType;
    private int quantityCPU;
    private String description;

    public Instance(){
        this.type = "vazia";
        this.valueHour = 0.0D;
        this.quantity = 0;
        this.locality = "vazia";
        this.memory = 0.0D;
        this.cpuHtz = 0.0D;
        this.cpuType = "vazia";
        this.quantityCPU=0;
        this.hd = 0.0D;
        this.hdType = "vazia";
        setDescription();
    }
    
    /**
     * Constructor
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
     */
    public Instance(String type, Double valueHour, int quantity, String locality, Double memory, Double cpuHtz, String cpuType, int quantityCPU, Double hd, String hdType){
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
        setDescription();
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the valueHour
     */
    public Double getValueHour() {
        return valueHour;
    }

    /**
     * @param valueHour the valueHour to set
     */
    public void setValueHour(Double valueHour) {
        this.valueHour = valueHour;
    }

    /**
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the locality
     */
    public String getLocality() {
        return locality;
    }

    /**
     * @param locality the locality to set
     */
    public void setLocality(String locality) {
        this.locality = locality;
    }

    /**
     * @return the memory
     */
    public Double getMemory() {
        return memory;
    }

    /**
     * @param memory the memory to set
     */
    public void setMemory(Double memory) {
        this.memory = memory;
    }

    /**
     * @return the cpuHtz
     */
    public Double getCpuHtz() {
        return cpuHtz;
    }

    /**
     * @param cpuHtz the cpuHtz to set
     */
    public void setCpuHtz(Double cpuHtz) {
        this.cpuHtz = cpuHtz;
    }

    /**
     * @return the cpuType
     */
    public String getCpuType() {
        return cpuType;
    }

    /**
     * @param cpuType the cpuType to set
     */
    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }

    /**
     * @return the hd
     */
    public Double getHd() {
        return hd;
    }

    /**
     * @param hd the hd to set
     */
    public void setHd(Double hd) {
        this.hd = hd;
    }

    /**
     * @return the hdType
     */
    public String getHdType() {
        return hdType;
    }

    /**
     * @param hdType the hdType to set
     */
    public void setHdType(String hdType) {
        this.hdType = hdType;
    }
    
     @Override
    public String toString() {
        
        return "Type: "+ this.type +", "
                + "CPU: "+ getQuantityCPU()+" "+ this.cpuHtz+"GHZ, "
                + "CPUType: " + this.cpuType+", "
                + "Ram:" + this.memory + "Gb, " 
                + "HD: " + this.hd + "Gb, " 
                + "Custo por hora : " + this.valueHour;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the Description with tostring of class
     */
    public void setDescription() {
        this.description = this.toString();
    }

    /**
     * @return the quantityCPU
     */
    public int getQuantityCPU() {
        return quantityCPU;
    }

    /**
     * @param quantityCPU the quantityCPU to set
     */
    public void setQuantityCPU(int quantityCPU) {
        this.quantityCPU = quantityCPU;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
}
