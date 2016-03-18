/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Google;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import static br.unb.cic.bionimbus.config.BioNimbusConfigLoader.loadHostConfig;
import br.unb.cic.bionimbus.services.tarifation.Utils.RestfulGetter;
import br.unb.cic.bionimbus.services.tarifation.Utils.RestfulGetterBehaviors.PricingGet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class GoogleDataGet {

    private final RestfulGetter getter = new PricingGet();
    private final Map<String, String> config;

    /**
     * Contrutor da classe
     *
     * Server: "cloudpricingcalculator.appspot.com" Address:
     * "/static/data/pricelist.json"
     * @param config
     */
    public GoogleDataGet(BioNimbusConfig config) {
        this.config = new HashMap<>();
        this.config.put("Server", "cloudpricingcalculator.appspot.com");
        this.config.put("Address", "/static/data/pricelist.json");
        this.config.put("Filename", config.getRootFolder()+"/conf/GooglePrices.txt");

        System.out.println("Getting Google prices...");
        String arrayString = this.getter.get(this.config.get("Server"), this.config.get("Address"));
        System.out.println("Completed.");

        if (arrayString != null) {
            System.out.println("Saving Google prices...");
            getter.saveGet(arrayString, this.config.get("Filename"));
            System.out.println("Saved.");
        }

    }
//     public static void main(String[] args) throws Exception {
//        
//        final String configFile = System.getProperty("config.file", "conf/node.yaml");
//        BioNimbusConfig config = loadHostConfig(configFile);
//
//        new GoogleDataGet(config);
//    } 
}
