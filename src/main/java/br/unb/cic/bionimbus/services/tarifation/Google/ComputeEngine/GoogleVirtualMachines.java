/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Google.ComputeEngine;

import java.util.Objects;

/**
 *
 * @author gabriel
 */
public class GoogleVirtualMachines {
    private String model;
    private String region;
    private double price;
    private double memory;
    private double gceu;
    private int MaxNumberOfPd;
    private int maxPdSize;
    private int[] ssd;

    public GoogleVirtualMachines(String model, String region, double price, double memory, double gceu, int MaxNumberOfPd, int maxPdSize, int[] ssd) {
        this.model=model;
        this.region = region;
        this.price = price;
        this.memory = memory;
        this.gceu = gceu;
        this.MaxNumberOfPd = MaxNumberOfPd;
        this.maxPdSize = maxPdSize;
        this.ssd = ssd;
    }

    public String getModel() {
        return model;
    }

    public String getRegion() {
        return region;
    }

    public double getPrice() {
        return price;
    }

    public double getMemory() {
        return memory;
    }

    public double getGceu() {
        return gceu;
    }

    public int getMaxNumberOfPd() {
        return MaxNumberOfPd;
    }

    public int getMaxPdSize() {
        return maxPdSize;
    }

    public int[] getSsd() {
        return ssd;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.model);
        hash = 29 * hash + Objects.hashCode(this.region);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GoogleVirtualMachines other = (GoogleVirtualMachines) obj;
        if (!Objects.equals(this.model, other.model)) {
            return false;
        }
        return Objects.equals(this.region, other.region);
    }
    
}
