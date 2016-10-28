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
//         String aux="", part[]={""},hdType="";
//         Double memory,hd, cpuhtz=0.0D,qtd;
         
//         JSONObject instancej= idx.EC2Instances("t2.nano", "US West (Oregon)", "Linux");
//         System.out.println(instancej);
//         System.out.print("memory: "+instancej.getJSONObject(instancej.keys().next()).getJSONObject("attributes").getString("memory"));
//         aux=instancej.getJSONObject(instancej.keys().next()).getJSONObject("attributes").getString("memory");
//         part=aux.split("(?= )");
//         memory=Double.parseDouble(part[0]);
//         System.out.println(memory);
//         System.out.print("cpuHtz: "+instancej.getJSONObject(instancej.keys().next()).getJSONObject("attributes").getString("clockSpeed"));
//         aux=instancej.getJSONObject(instancej.keys().next()).getJSONObject("attributes").getString("clockSpeed");
//         part=aux.split("(?= )");
//         if(part.length>2){
//             cpuhtz = Double.parseDouble(part[2]);
//         }
//         else
//             cpuhtz = Double.parseDouble(part[0]);
//         System.out.println(cpuhtz);
//         System.out.println("storage: "+instancej.getJSONObject(instancej.keys().next()).getJSONObject("attributes").getString("storage"));
//         aux=instancej.getJSONObject(instancej.keys().next()).getJSONObject("attributes").getString("storage");
//         part=aux.split("(?= )");
//        switch (part.length) {
//            case 2:
//                hd = 80D;
//                hdType=part[0];
//                break;
//            case 3:
//                qtd = Double.parseDouble(part[0]);
//                hd = qtd * Double.parseDouble(part[2]);
//                hdType="HDD";
//                break;
//            default:
//                qtd = Double.parseDouble(part[0]);
//                hd = qtd * Double.parseDouble(part[2]);
//                hdType=part[3];
//                break;
//        }
//         System.out.println(hd);
             
             
//         System.out.println("1"+instancej.keySet());
//         System.out.println("2"+instancej.keys().next());
//         
//         Instance config = Jackson.fromJsonString(instancej.toString(), Instance.class);
//         System.out.println(config.toString());
         ArrayList<Instance> result=idx.getListInstanceEc2();
         
         for(Instance inst : result){
             System.out.println(inst.toString());
         }

    }
}
