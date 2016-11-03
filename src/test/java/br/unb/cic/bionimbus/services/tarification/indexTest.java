package br.unb.cic.bionimbus.services.tarification;

import java.util.ArrayList;

import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.services.tarification.Amazon.AmazonIndex;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
/**
 * Notes: To check if this test is OK or no, check the Output of the method!
 */
public class indexTest {
    
    // @Test
    // public void indexTest() throws JSONException, IOException {
    //// AmazonIndex idx = new AmazonIndex("pricing.us-east-1.amazonaws.com", "/offers/v1.0/aws/index.json");
    // AmazonIndex idx = new AmazonIndex();
    // System.out.println(idx.EC2Instances("r3.xlarge","US East (N. Virginia)").toString(4));
    // }
    
//        long now = System.currentTimeMillis();
//        
//        JSONObject jo = new JSONObject();
//        String url = "https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/index.json";
//        try (
//             final InputStream openedStream = new URL(url).openStream();) {
//            
//            jo = (JSONObject) new JSONTokener(openedStream).nextValue();
//            // for(Instance inst : result){
//            // System.out.println(inst.toString());
//            // }
//            
//            System.out.println("time 1: " + (System.currentTimeMillis() - now));
//            now = System.currentTimeMillis();
//            
//            JSONObject offersAmazonEc2 = new JSONObject();
//            url = "https://pricing.us-east-1.amazonaws.com" + jo.getJSONObject("offers").getJSONObject("AmazonEC2").getString("currentVersionUrl");
//            
//            try (
//                 final InputStream offersOpenedStream = new URL(url).openStream()) {
//                
//                offersAmazonEc2 = (JSONObject) new JSONTokener(offersOpenedStream).nextValue();
//                
//                System.out.println("time 2: " + (System.currentTimeMillis() - now));
//                
//                now = System.currentTimeMillis();
//                
//                final AmazonIndex idx = new AmazonIndex(offersAmazonEc2);
//                jo = idx.getJsonObjectInstances();
//                
//                System.out.println("time 3: " + (System.currentTimeMillis() - now));
//                
//                now = System.currentTimeMillis();
//                
//                final ArrayList<Instance> result = idx.getListInstanceEc2();
//                
//                System.out.println("time 4: " + (System.currentTimeMillis() - now));
//                
//                // System.out.println(jo.toString(4)); // temp
//            }
//        } catch (final IOException ex) {
//            Logger.getLogger(indexTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
    public static void main(String[] args) {
         AmazonIndex idx = new AmazonIndex();
         ArrayList<Instance> result=idx.getListInstanceEc2();
         result.forEach((i) -> {
             System.out.println(i);
        });
        
    }
}
