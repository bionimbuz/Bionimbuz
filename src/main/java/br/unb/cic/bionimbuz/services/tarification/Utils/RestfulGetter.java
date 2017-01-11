/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.tarification.Utils;

/**
 *
 * @author gabriel
 */
public interface RestfulGetter {

    /**
     *
     * @param server - Server where the GET operation will be realized.
     * @param address - The complement address where the operation will be
     * realized.
     * @return - the result entity of the GET operation, in String format.
     */
    public String get(String server, String address);
    
    public void saveGet(String array, String filename);
}
