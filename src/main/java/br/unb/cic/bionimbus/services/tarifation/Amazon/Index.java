/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Amazon;

import br.unb.cic.bionimbus.services.tarifation.JsonReader;
import com.amazonaws.util.json.JSONException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
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
        this.saveIndex(amazonServicesURLs.toString(4),"index.json");
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
        System.out.println(AmazonEC2);
    }

    public Index(String server) {
        
        JSONObject amazonServicesURLs = readIndex("index.json");
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
        System.out.println(AmazonEC2);
    }
    
    private void saveIndex(String array, String filename) {

        try {
            OutputStream os = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            try (BufferedWriter bw = new BufferedWriter(osw)) {
                bw.write(array);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AmazonDataGet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AmazonDataGet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private JSONObject readIndex(String filename) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            String everything = sb.toString();

            if (everything != null) {

                return (new JSONObject(everything));
            } else {

                return (null);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AmazonData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AmazonData.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (null);
    }
}
