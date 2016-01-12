/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Amazon;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
//Definição de uma classe que representa uma máquina virtual da Amazon.
public class AmazonVirtualMachine {
    
    private String pricing;
    private String region;
    private int id;
    private String os;
    private String model;
    private double upfront;
    private String updated_at;
    private double term;
    private String created_at;
    private boolean latest;
    private double hourly;
    private boolean ebsoptimized;

    public AmazonVirtualMachine(String pricing, String region, int id, String os, String model, double upfront, String updated_at, double term, String created_at, boolean latest, double hourly, boolean ebsoptimized) {
        this.pricing = pricing;
        this.region = region;
        this.id = id;
        this.os = os;
        this.model = model;
        this.upfront = upfront;
        this.updated_at = updated_at;
        this.term = term;
        this.created_at = created_at;
        this.latest = latest;
        this.hourly = hourly;
        this.ebsoptimized = ebsoptimized;
    }

    public String getPricing() {
        return pricing;
    }

    public String getRegion() {
        return region;
    }

    public int getId() {
        return id;
    }

    public String getOs() {
        return os;
    }

    public String getModel() {
        return model;
    }

    public double getUpfront() {
        return upfront;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public double getTerm() {
        return term;
    }

    public String getCreated_at() {
        return created_at;
    }

    public boolean isLatest() {
        return latest;
    }

    public double getHourly() {
        return hourly;
    }

    public boolean isEbsoptimized() {
        return ebsoptimized;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.id;
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
        final AmazonVirtualMachine other = (AmazonVirtualMachine) obj;
        return this.id == other.id;
    }
}
