/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation;

import br.unb.cic.bionimbus.services.tarifation.Amazon.AmazonEC2;
import br.unb.cic.bionimbus.services.tarifation.Amazon.AmazonIndex;
import com.amazonaws.util.json.JSONException;
import java.io.IOException;
import org.junit.Test;


public class AmazonEC2Test {
    
    @Test
    public void AmazonEC2Test() throws JSONException, IOException{
        String url = "https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/AmazonEC2/current/index.json";
        AmazonEC2 aec = new AmazonEC2(url, "lala");
    }
}
