/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarification;

import java.io.IOException;

import br.unb.cic.bionimbus.services.tarification.Google.GoogleCloud;
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
        GoogleCloud gc=null;
         try {
             gc = new GoogleCloud("www.cloudpricingcalculator.appspot.com","/static/data/pricelist.json");
         } catch (IOException | JSONException ex) {
             Logger.getLogger(GoogleDataTest.class.getName()).log(Level.SEVERE, null, ex);
         }
//        GoogleCloud gc = new GoogleCloud();    
//        gc.getListJsonObjectInstances();
////        System.out.println(gc.GoogleComputeEngineInstances("N1.STANDARD-4.PREEMPTIBLE", "").toString(4));
////        ArrayList<Instance> instancesGoogle=gc.getListInstanceGCE();
//        
//        gc.getListJsonObjectInstances();
    gc.getListInstanceGCE().forEach((i) -> {
    System.out.println(i);
//            
        });
    
    }
}
