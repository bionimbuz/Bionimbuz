package br.unb.cic.bionimbus.services.tarification;

import br.unb.cic.bionimbus.services.tarification.Amazon.AmazonIndex;
import java.util.ArrayList;
import br.unb.cic.bionimbus.model.Instance;

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
//      
         ArrayList<Instance> result=idx.getListInstanceEc2();
         
//         for(Instance inst : result){
//             System.out.println(inst.toString());
//         }

    }
}
