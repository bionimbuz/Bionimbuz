/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Amazon;

import br.unb.cic.bionimbus.services.tarifation.JsonReader;
import com.amazonaws.util.json.JSONException;
import java.io.IOException;
import org.json.JSONObject;

/**
 *
 * @author fritz
 */
public class Index {

    private String AmazonS3;
    private String AmazonGlacier;
    private String AmazonSES;
    private String AmazonRDS;
    private String AmazonSimpleDB;
    private String AmazonDynamoDB;
    private String AmazonEC2;
    private String AmazonRoute53;
    private String AmazonRedshift;
    private String AmazonElastiCache;
    private String AmazonCloudFront;
    private String awskms;
    private String AmazonVPC;
    
    public Index(String server,String index) throws JSONException, IOException {
        JSONObject amazonServicesURLs = JsonReader.readJsonFromUrl("https://"+"pricing.us-east-1.amazonaws.com"+"/offers/v1.0/aws/index.json");
        amazonServicesURLs = amazonServicesURLs.getJSONObject("offers");
        this.AmazonS3 ="https://" + server + amazonServicesURLs.getJSONObject("AmazonS3").getString("currentVersionUrl");
        this.AmazonGlacier ="https://" + server + amazonServicesURLs.getJSONObject("AmazonGlacier").getString("currentVersionUrl");
        this.AmazonSES ="https://" + server + amazonServicesURLs.getJSONObject("AmazonSES").getString("currentVersionUrl");
        this.AmazonRDS ="https://" + server + amazonServicesURLs.getJSONObject("AmazonRDS").getString("currentVersionUrl");
        this.AmazonSimpleDB ="https://" + server + amazonServicesURLs.getJSONObject("AmazonSimpleDB").getString("currentVersionUrl");
        this.AmazonDynamoDB ="https://" + server + amazonServicesURLs.getJSONObject("AmazonDynamoDB").getString("currentVersionUrl");
        this.AmazonEC2 ="https://" + server + amazonServicesURLs.getJSONObject("AmazonEC2").getString("currentVersionUrl");
        this.AmazonRoute53 ="https://" + server + amazonServicesURLs.getJSONObject("AmazonRoute53").getString("currentVersionUrl");
        this.AmazonEC2 ="https://" + server + amazonServicesURLs.getJSONObject("AmazonEC2").getString("currentVersionUrl");
        this.AmazonRoute53 ="https://" + server + amazonServicesURLs.getJSONObject("AmazonRoute53").getString("currentVersionUrl");
        this.AmazonRedshift ="https://" + server + amazonServicesURLs.getJSONObject("AmazonRedshift").getString("currentVersionUrl");
        this.AmazonElastiCache ="https://" + server + amazonServicesURLs.getJSONObject("AmazonElastiCache").getString("currentVersionUrl");
        this.AmazonCloudFront ="https://" + server + amazonServicesURLs.getJSONObject("AmazonCloudFront").getString("currentVersionUrl");
        this.awskms ="https://" + server + amazonServicesURLs.getJSONObject("awskms").getString("currentVersionUrl");
        this.AmazonVPC ="https://" + server + amazonServicesURLs.getJSONObject("AmazonVPC").getString("currentVersionUrl");      
    }
}
