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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author fritz
 */
public class Index {

    private JSONObject AmazonS3;
    private JSONObject AmazonGlacier;
    private JSONObject AmazonSES;
    private JSONObject AmazonRDS;
    private JSONObject AmazonSimpleDB;
    private JSONObject AmazonDynamoDB;
    private JSONObject AmazonEC2;
    private JSONObject AmazonRoute53;
    private JSONObject AmazonRedshift;
    private JSONObject AmazonElastiCache;
    private JSONObject AmazonCloudFront;
    private JSONObject awskms;
    private JSONObject AmazonVPC;
    
    public Index(String server,String index) throws JSONException, IOException {
        JSONObject amazonServicesURLs = JsonReader.readJsonFromUrl("https://"+"pricing.us-east-1.amazonaws.com"+"/offers/v1.0/aws/index.json");
        this.saveJson(amazonServicesURLs.toString(4),"index.json");
        amazonServicesURLs = amazonServicesURLs.getJSONObject("offers");
        String AmazonS3 ="https://" + server + amazonServicesURLs.getJSONObject("AmazonS3").getString("currentVersionUrl");
        this.AmazonS3 = JsonReader.readJsonFromUrl(AmazonS3);
        this.saveJson(this.AmazonS3.toString(4),"AmazonS3.json");
        String AmazonGlacier ="https://" + server + amazonServicesURLs.getJSONObject("AmazonGlacier").getString("currentVersionUrl");
        this.AmazonGlacier = JsonReader.readJsonFromUrl(AmazonGlacier);
        this.saveJson(this.AmazonGlacier.toString(4),"AmazonGlacier.json");
        String AmazonSES ="https://" + server + amazonServicesURLs.getJSONObject("AmazonSES").getString("currentVersionUrl");
        this.AmazonSES = JsonReader.readJsonFromUrl(AmazonSES);
        this.saveJson(this.AmazonSES.toString(4),"AmazonSES.json");
        String AmazonRDS ="https://" + server + amazonServicesURLs.getJSONObject("AmazonRDS").getString("currentVersionUrl");
        this.AmazonRDS = JsonReader.readJsonFromUrl(AmazonRDS);
        this.saveJson(this.AmazonRDS.toString(4),"AmazonRDS.json");
        String AmazonSimpleDB ="https://" + server + amazonServicesURLs.getJSONObject("AmazonSimpleDB").getString("currentVersionUrl");
        this.AmazonSimpleDB = JsonReader.readJsonFromUrl(AmazonSimpleDB);
        this.saveJson(this.AmazonSimpleDB.toString(4),"AmazonSimpleDB.json");
        String AmazonDynamoDB ="https://" + server + amazonServicesURLs.getJSONObject("AmazonDynamoDB").getString("currentVersionUrl");
        this.AmazonDynamoDB = JsonReader.readJsonFromUrl(AmazonDynamoDB);
        this.saveJson(this.AmazonDynamoDB.toString(4),"AmazonDynamoDB.json");
        String AmazonEC2 ="https://" + server + amazonServicesURLs.getJSONObject("AmazonEC2").getString("currentVersionUrl");
        this.AmazonEC2 = JsonReader.readJsonFromUrl(AmazonEC2);
        this.saveJson(this.AmazonEC2.toString(4),"AmazonEC2.json");
        String AmazonRoute53 ="https://" + server + amazonServicesURLs.getJSONObject("AmazonRoute53").getString("currentVersionUrl");
        this.AmazonRoute53 = JsonReader.readJsonFromUrl(AmazonRoute53);
        this.saveJson(this.AmazonRoute53.toString(4),"AmazonRoute53.json");
        String AmazonRedshift ="https://" + server + amazonServicesURLs.getJSONObject("AmazonRedshift").getString("currentVersionUrl");
        this.AmazonRedshift = JsonReader.readJsonFromUrl(AmazonRedshift);
        this.saveJson(this.AmazonRedshift.toString(4),"AmazonRedshift.json");
        String AmazonElastiCache ="https://" + server + amazonServicesURLs.getJSONObject("AmazonElastiCache").getString("currentVersionUrl");
        this.AmazonElastiCache = JsonReader.readJsonFromUrl(AmazonElastiCache);
        this.saveJson(this.AmazonElastiCache.toString(4),"AmazonElastiCache.json");
        String AmazonCloudFront ="https://" + server + amazonServicesURLs.getJSONObject("AmazonCloudFront").getString("currentVersionUrl");
        this.AmazonCloudFront = JsonReader.readJsonFromUrl(AmazonCloudFront);
        this.saveJson(this.AmazonCloudFront.toString(4),"AmazonCloudFront.json");
        String awskms ="https://" + server + amazonServicesURLs.getJSONObject("awskms").getString("currentVersionUrl");
        this.awskms = JsonReader.readJsonFromUrl(awskms);
        this.saveJson(this.awskms.toString(4),"awskms.json");
        String AmazonVPC ="https://" + server + amazonServicesURLs.getJSONObject("AmazonVPC").getString("currentVersionUrl");
        this.AmazonVPC = JsonReader.readJsonFromUrl(AmazonVPC);
        this.saveJson(this.AmazonVPC.toString(4),"AmazonVPC.json");
        System.out.println(AmazonEC2);
    }

    public Index() {
        String AmazonS3 ="AmazonS3.json";
        this.AmazonS3 = this.readJson(AmazonS3);
        String AmazonGlacier ="AmazonGlacier.json";
        this.AmazonGlacier = this.readJson(AmazonS3);
        String AmazonSES ="AmazonSES.json";
        this.AmazonSES = this.readJson(AmazonSES);
        String AmazonRDS ="AmazonRDS.json";
        this.AmazonRDS = this.readJson(AmazonRDS);
        String AmazonSimpleDB ="AmazonSimpleDB.json";
        this.AmazonSimpleDB = this.readJson(AmazonSimpleDB);
        String AmazonDynamoDB ="AmazonDynamoDB.json";
        this.AmazonDynamoDB = this.readJson(AmazonDynamoDB);
        String AmazonEC2 ="AmazonEC2.json";
        this.AmazonEC2 = this.readJson(AmazonEC2);
        String AmazonRoute53 ="AmazonRoute53.json";
        this.AmazonRoute53 = this.readJson(AmazonRoute53);
        String AmazonRedshift ="AmazonRedshift.json";
        this.AmazonRedshift = this.readJson(AmazonRedshift);
        String AmazonElastiCache ="AmazonElastiCache.json";
        this.AmazonElastiCache = this.readJson(AmazonElastiCache);
        String AmazonCloudFront ="AmazonCloudFront.json";
        this.AmazonCloudFront = this.readJson(AmazonCloudFront);
        String awskms ="awskms.json";
        this.awskms = this.readJson(awskms);
        String AmazonVPC ="AmazonVPC.json";
        this.AmazonVPC = this.readJson(AmazonVPC);
    }
    
    private void saveJson(String array, String filename) {

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
    
    private JSONObject readJson(String filename) {

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
    
    public JSONObject EC2Instances(String instanceType, String location, String os){
        JSONObject products = this.AmazonEC2.getJSONObject("products");
        JSONObject prices = this.AmazonEC2.getJSONObject("terms").getJSONObject("OnDemand");
        Iterator<String> it = products.keys();
        String auxString = "{";
        boolean first = true;
        while(it.hasNext()){
            JSONObject aux = products.getJSONObject(it.next());
            if(aux.getString("productFamily").equals("Compute Instance")){
                if((aux.getJSONObject("attributes").getString("instanceType").equals(instanceType))&&(aux.getJSONObject("attributes").getString("location").equals(location))&&(aux.getJSONObject("attributes").getString("operatingSystem").equals(os))){
                    if(first){
                        auxString = auxString +"\"" + aux.getString("sku")+"\":" + aux.toString(4); 
                        first = false;
                    }
                    else {
                        auxString = auxString +","+"\"" + aux.getString("sku")+"\":" + aux.toString(4); 
                    }
                }
            }
        }
        auxString = auxString +"}";
        return(new JSONObject(auxString));
    }
    
    public JSONObject EC2Instances(String instanceType, String location){
        JSONObject products = this.AmazonEC2.getJSONObject("products");
        JSONObject prices = this.AmazonEC2.getJSONObject("terms").getJSONObject("OnDemand");
        Iterator<String> it = products.keys();
        String auxString = "{";
        boolean first = true;
        while(it.hasNext()){
            JSONObject aux = products.getJSONObject(it.next());
            if(aux.getString("productFamily").equals("Compute Instance")){
                if((aux.getJSONObject("attributes").getString("instanceType").equals(instanceType))&&(aux.getJSONObject("attributes").getString("location").equals(location))){
                    if(first){
                        auxString = auxString +"\"" + aux.getString("sku")+"\":" + aux.toString(4); 
                        first = false;
                    }
                    else {
                        auxString = auxString +","+"\"" + aux.getString("sku")+"\":" + aux.toString(4); 
                    }
                }
            }
        }
        auxString = auxString +"}";
        return(new JSONObject(auxString));
    }
    
    public JSONObject EC2Instances(String instanceType){
        JSONObject products = this.AmazonEC2.getJSONObject("products");
        JSONObject prices = this.AmazonEC2.getJSONObject("terms").getJSONObject("OnDemand");
        Iterator<String> it = products.keys();
        String auxString = "{";
        boolean first = true;
        while(it.hasNext()){
            JSONObject aux = products.getJSONObject(it.next());
            if(aux.getString("productFamily").equals("Compute Instance")){
                if((aux.getJSONObject("attributes").getString("instanceType").equals(instanceType))){
                    if(first){
                        auxString = auxString +"\"" + aux.getString("sku")+"\":" + aux.toString(4); 
                        first = false;
                    }
                    else {
                        auxString = auxString +","+"\"" + aux.getString("sku")+"\":" + aux.toString(4); 
                    }
                }
            }
        }
        auxString = auxString +"}";
        return(new JSONObject(auxString));
    }
}
