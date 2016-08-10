/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation;

import br.unb.cic.bionimbus.services.tarifation.Google.GoogleCloud;
import br.unb.cic.bionimbus.services.tarifation.Google.GoogleData;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author gabriel
 */
public class GoogleDataTest {
    
    public static void main(String[] args){
        try {
            GoogleCloud gc = new GoogleCloud("https://cloudpricingcalculator.appspot.com","/static/data/pricelist.json");
        } catch (IOException ex) {
            Logger.getLogger(GoogleDataTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
