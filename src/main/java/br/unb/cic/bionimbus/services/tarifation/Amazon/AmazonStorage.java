/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Amazon;

/**
 *
 * @author gabriel
 */
public class AmazonStorage {
    private int id;
    private String region;
    private String kind;
    private double price;
    private String PriceUnit;
    private String createdAt;
    private String updatedAt;

    public AmazonStorage(int id, String region, String kind, double price, String PriceUnit, String createdAt, String updatedAt) {
        this.id = id;
        this.region = region;
        this.kind = kind;
        this.price = price;
        this.PriceUnit = PriceUnit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getKind() {
        return kind;
    }

    public double getPrice() {
        return price;
    }

    public String getPriceUnit() {
        return PriceUnit;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.id;
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
        final AmazonStorage other = (AmazonStorage) obj;
        return this.id == other.id;
    }
}
