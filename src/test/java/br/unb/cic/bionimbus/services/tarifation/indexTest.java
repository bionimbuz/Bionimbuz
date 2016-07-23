/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation;

import br.unb.cic.bionimbus.services.tarifation.Amazon.Index;
import com.amazonaws.util.json.JSONException;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author fritz
 */

/**
 *  Notes: To check if this test is OK or no, check the Output of the method! 
 */
public class indexTest {
    
    @Test
    public void indexTest() throws JSONException, IOException{
        //Index idx = new Index("pricing.us-east-1.amazonaws.com","/offers/v1.0/aws/index.json");
        Index idx = new Index(); 
        System.out.println(idx.EC2Instances("i2.4xlarge","Asia Pacific (Mumbai)","Windows").toString(4));
    }
}
