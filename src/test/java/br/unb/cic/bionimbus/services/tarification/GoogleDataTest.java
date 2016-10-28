/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarification;

import java.io.IOException;

import br.unb.cic.bionimbus.services.tarification.Google.GoogleCloud;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class GoogleDataTest {

    public static void main(String[] args) throws IOException {
        GoogleCloud gc = new GoogleCloud("cloudpricingcalculator.appspot.com","/static/data/pricelist.json");
//        gc.getListJsonObjectInstances();
//        System.out.println(gc.GoogleComputeEngineInstances("N1.STANDARD-4.PREEMPTIBLE", "").toString(4));
    }
}
