/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Amazon;

import br.unb.cic.bionimbus.services.tarifation.JsonReader;
import br.unb.cic.bionimbus.services.tarifation.Utils.RestfulGetter;
import br.unb.cic.bionimbus.services.tarifation.Utils.RestfulGetterBehaviors.PricingGet;
import com.amazonaws.util.json.JSONException;
import java.io.IOException;
import org.json.JSONObject;

/**
 *
 * @author fritz
 */
public class Index {

    private String amazonS3;
    private String amazonGlacier;
    private String amazonSES;
    private String amazonRDS;
    private String amazonSimpleDB;
    private String amazinDynamoDB;
    private String amazonEC2;
    private String amazonRoute53;
    private String amazonRedshift;
    private String amazonElastiCache;
    private String amazonCloudFront;
    private String awskms;
    private String amazonVPC;
    
    public Index(String server,String index) throws JSONException, IOException {
        JSONObject amazonServicesURLs = JsonReader.readJsonFromUrl("https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/index.json");
        amazonServicesURLs = amazonServicesURLs.getJSONObject("offers");
        System.out.println(amazonServicesURLs.toString(4));
        
    }
}
