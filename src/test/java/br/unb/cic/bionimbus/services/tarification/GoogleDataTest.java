/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarification;

import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.services.tarification.Amazon.AmazonIndex;
import java.io.IOException;

import br.unb.cic.bionimbuz.services.tarification.Google.GoogleCloud;
import br.unb.cic.bionimbuz.services.tarification.JsonReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.junit.Test;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class GoogleDataTest {

     
     @Test
     public void GoogleDataTest(){
    //// AmazonIndex idx = new AmazonIndex("pricing.us-east-1.amazonaws.com", "/offers/v1.0/aws/index.json");
    // AmazonIndex idx = new AmazonIndex();
    // System.out.println(idx.EC2Instances("r3.xlarge","US East (N. Virginia)").toString(4));
     }
    public static void main(String[] args){
        AmazonIndex idx = null;
        ArrayList<Instance> result=new ArrayList<>();
        String defaultConfigPathname =System.getProperty("user.home")  + "/Bionimbuz/conf/all.json";
        GoogleCloud gc=null;
         try {
             gc = new GoogleCloud("www.cloudpricingcalculator.appspot.com","/static/data/pricelist.json");
             idx = new AmazonIndex("pricing.us-east-1.amazonaws.com", "/offers/v1.0/aws/index.json");
         } catch (IOException | JSONException | com.amazonaws.util.json.JSONException ex) {
             Logger.getLogger(GoogleDataTest.class.getName()).log(Level.SEVERE, null, ex);
         }
//        GoogleCloud gc = new GoogleCloud();    
//        gc.getListJsonObjectInstances();
////        System.out.println(gc.GoogleComputeEngineInstances("N1.STANDARD-4.PREEMPTIBLE", "").toString(4));
////        ArrayList<Instance> instancesGoogle=gc.getListInstanceGCE();
//        
//        gc.getListJsonObjectInstances();
    result.addAll(idx.getListInstanceEc2());
    result.addAll(gc.getListInstanceGCE());
     JsonReader.saveJson(result.toString(), defaultConfigPathname);
    result.forEach((i) -> {
    System.out.println(i);
//            
        });
    
    }
}
