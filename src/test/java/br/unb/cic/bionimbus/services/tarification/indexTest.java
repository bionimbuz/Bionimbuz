package br.unb.cic.bionimbus.services.tarification;

import br.unb.cic.bionimbus.services.tarification.Amazon.AmazonIndex;
import java.util.ArrayList;
import br.unb.cic.bionimbus.model.Instance;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
/**
 * Notes: To check if this test is OK or no, check the Output of the method!
 */
public class indexTest {

//    @Test
//    public void indexTest() throws JSONException, IOException {
////        AmazonIndex idx = new AmazonIndex("pricing.us-east-1.amazonaws.com", "/offers/v1.0/aws/index.json");
//        AmazonIndex idx = new AmazonIndex(); 
//        System.out.println(idx.EC2Instances("r3.xlarge","US East (N. Virginia)").toString(4));
//    }
   
    public static void main(String[] args) {
//         AmazonIndex idx = new AmazonIndex(); 
////      
//         ArrayList<Instance> result=idx.getListInstanceEc2();
         JSONObject jo = new JSONObject();
         try {
            jo = (JSONObject) new JSONTokener(IOUtils.toString(new URL("https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/index.json").openStream‌​())).nextValue();
//         for(Instance inst : result){
//             System.out.println(inst.toString());
//         }
        } catch ( IOException ex) {
            Logger.getLogger(indexTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        JSONObject offersAmazonEc2 = new JSONObject();
        String offersUrlAmazonEc2 = "https://pricing.us-east-1.amazonaws.com"+ jo.getJSONObject("offers").getJSONObject("AmazonEC2").getString("currentVersionUrl");
        try {
            offersAmazonEc2 = (JSONObject) new JSONTokener(IOUtils.toString(new URL(offersUrlAmazonEc2).openStream‌​())).nextValue();
        } catch ( IOException ex) {
            Logger.getLogger(indexTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        AmazonIndex idx = new AmazonIndex(offersAmazonEc2);
        jo= idx.getJsonObjectInstances();
        System.out.println(jo.toString(4));
    }
}
