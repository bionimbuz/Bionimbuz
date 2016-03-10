/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Amazon;

import br.unb.cic.bionimbus.services.tarifation.Instance;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class AmazonDataTransfer implements Instance{

    private boolean activationStatus;
    private int id;
    private String region;
    private String kind;
    private String tier;
    private double price;
    private String createdAt;
    private String updatedAt;

    /**
     *
     * @param id
     * @param region
     * @param kind
     * @param tier
     * @param price
     * @param createdAt
     * @param updatedAt
     */
    public AmazonDataTransfer(int id, String region, String kind, String tier, double price, String createdAt, String updatedAt) {
        this.id = id;
        this.region = region;
        this.kind = kind;
        this.tier = tier;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     *
     * @return
     */
    @Override
    public String status() {
        return activationStatus+"";
    }

    /**
     *
     * @return
     */
    @Override
    public String getId() {
        return id +"";
    }

    /**
     *
     * @return
     */
    public String getRegion() {
        return region;
    }

    /**
     *
     * @return
     */
    public String getKind() {
        return kind;
    }

    /**
     *
     * @return
     */
    public String getTier() {
        return tier;
    }

    /**
     *
     * @return
     */
    public String getPrice() {
        return price+"/OPERATION";
    }

    /**
     *
     * @return
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     *
     * @return
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.id;
        return hash;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AmazonDataTransfer other = (AmazonDataTransfer) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return this.getKind();
    }
}
