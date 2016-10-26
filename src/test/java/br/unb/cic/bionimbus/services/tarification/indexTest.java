package br.unb.cic.bionimbus.services.tarification;

import br.unb.cic.bionimbus.services.tarification.Amazon.AmazonIndex;
import com.amazonaws.util.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONObject;
import org.junit.Test;
import br.unb.cic.bionimbus.model.Instance;
import com.amazonaws.util.json.Jackson;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

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
         AmazonIndex idx = new AmazonIndex(); 
         
//         JSONObject instancej= idx.EC2Instances("t2.nano", "US West (Oregon)", "Linux");
//         System.out.println(instancej);
//         System.out.println("1"+instancej.keySet());
//         System.out.println("2"+instancej.keys().next());
//         
//         Instance config = Jackson.fromJsonString(instancej.toString(), Instance.class);
//         System.out.println(config.toString());
         ArrayList<Instance> result=idx.getListInstanceEc2();
         System.out.println("AAA");
         System.out.println(result.get(0).toString());
         System.out.println(result.size());
    }
}
