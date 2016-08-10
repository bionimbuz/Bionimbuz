/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Google;

import br.unb.cic.bionimbus.services.tarifation.JsonReader;
import java.io.IOException;
import org.json.JSONObject;

/**
 *
 * @author fritz
 */
public class GoogleCloud {
    
    private JSONObject ComputerEngine;
    
    public GoogleCloud (String server, String index) throws IOException {
        String ComputerEngine = "https://cloudpricingcalculator.appspot.com/static/data/pricelist.json";
        this.ComputerEngine = JsonReader.readJsonFromUrl(ComputerEngine);
        JsonReader.saveJson(this.ComputerEngine.toString(), "GoogleCloud.json");
        System.out.println(this.ComputerEngine.toString(4));
    }
    
}
