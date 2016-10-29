package br.unb.cic.bionimbus.services.tarification.Google;

import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.services.tarification.JsonReader;
import br.unb.cic.bionimbus.services.tarification.Utils.RestfulGetterBehaviors.PricingGet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class GoogleCloud {

    private JSONObject computeEngine;
    final String defaultConfigPathname = System.getProperty("user.home") + "/Bionimbuz/conf/GoogleCloud.json";
    final String http = "https://";
    final String server = "cloudpricingcalculator.appspot.com";
    final String index = "/static/data/pricelist.json";
    private String allInstanceTypeName[] = {"F1.MICRO", "G1.SMALL", "N1.STANDARD-1",
        "N1.STANDARD-2", "N1.STANDARD-4", "N1.STANDARD-8", "N1.STANDARD-16",
        "N1.STANDARD-32", "N1.HIGHMEM-2", "N1.HIGHMEM-4", "N1.HIGHMEM-8",
        "N1.HIGHMEM-16", "N1.HIGHMEM-32", "N1.HIGHCPU-2", "N1.HIGHCPU-4",
        "N1.HIGHCPU-8", "N1.HIGHCPU-16", "N1.HIGHCPU-32",
        "F1.MICRO.PREEMPTIBLE", "G1.SMALL.PREEMPTIBLE",
        "N1.STANDARD-1.PREEMPTIBLE", "N1.STANDARD-2.PREEMPTIBLE",
        "N1.STANDARD-4.PREEMPTIBLE", "N1.STANDARD-8.PREEMPTIBLE",
        "N1.STANDARD-16.PREEMPTIBLE", "N1.STANDARD-32.PREEMPTIBLE",
        "N1.HIGHMEM-2.PREEMPTIBLE", "N1.HIGHMEM-4.PREEMPTIBLE",
        "N1.HIGHMEM-8.PREEMPTIBLE", "N1.HIGHMEM-16.PREEMPTIBLE",
        "N1.HIGHMEM-32.PREEMPTIBLE", "N1.HIGHCPU-2.PREEMPTIBLE",
        "N1.HIGHCPU-4.PREEMPTIBLE", "N1.HIGHCPU-8.PREEMPTIBLE",
        "N1.HIGHCPU-16.PREEMPTIBLE", "N1.HIGHCPU-32.PREEMPTIBLE"};
    private String allLocation[] = {"asia", "europe", "us"};

    /**
     * Constructor responsible for obtaining data about Google Cloud Compute
     * Engine's VMs from Web.<br>
     * Recommended Server: "cloudpricingcalculator.appspot.com"<br>
     * Recommended Index: "/static/data/pricelist.json"<br>
     *
     * @param server Server where the JSON file is located.
     * @param index Remaining URL info.
     * @throws IOException
     */
    public GoogleCloud(String server, String index) throws IOException, JSONException {
        PricingGet getter = new PricingGet();
        String computerEngineString = getter.get(server, index);
//        this.computeEngine= JsonReader.readJsonFromUrl(http+server+index);
        
//        System.out.println(computeEngineAux);
//        System.out.println("\n\n");        
        this.computeEngine= new JSONObject(computerEngineString);
//        this.computeEngine = new JSONObject(computerEngineString);
        JsonReader.saveJson(computerEngineString, defaultConfigPathname);
//       try( PrintWriter out = new PrintWriter( System.getProperty("user.home") + "/Bionimbuz/hsS.json") ){
//            out.println( computeEngineAux );
//        }
//        
////        this.computeEngine = new JSONObject(computeEngineAux);
//        try( PrintWriter out = new PrintWriter( System.getProperty("user.home") + "/Bionimbuz/jsS.json") ){
//            out.println( this.computeEngine);
//        }
//        System.out.println(this.computeEngine.toString(4));
//        this.computeEngine = new JSONObject(computerEngineString);
//        System.out.println(this.computeEngine.toString(4));
    }

    /**
     * Constructor responsible for obtaining dara about Google Cloud Compute
     * Engine's VMs from local file.<br>
     */
    public GoogleCloud() {
//        String computeEngine = "GoogleCloud.json";

        File f = new File(defaultConfigPathname);
        if (!(f.exists() && !f.isDirectory())) {
            try {
                new GoogleCloud(server,index );
            } catch (IOException | JSONException ex) {
                Logger.getLogger(GoogleCloud.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.computeEngine = JsonReader.readJson(defaultConfigPathname);
        this.computeEngine = this.computeEngine.getJSONObject("gcp_price_list");
        Iterator<String> it = this.computeEngine.keys();
        ArrayList<String> invalidKeys = new ArrayList<>();
        while (it.hasNext()) {
            String key = it.next();
            if (!key.contains("COMPUTEENGINE-VMIMAGE")) {
                invalidKeys.add(key);
            }
        }
        it = invalidKeys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            this.computeEngine.remove(key);
        }
    }

    /**
     * Method responsible for Google Cloud's Compute Engine instance data
     * obtaining.
     *
     * @param instanceType Possible Values:<br><br>
     *
     * - "F1.MICRO"<br>
     * - "G1.SMALL"<br>
     * - "N1.STANDARD-1"<br>
     * - "N1.STANDARD-2"<br>
     * - "N1.STANDARD-4"<br>
     * - "N1.STANDARD-8"<br>
     * - "N1.STANDARD-16"<br>
     * - "N1.STANDARD-32"<br>
     * - "N1.HIGHMEM-2"<br>
     * - "N1.HIGHMEM-4"<br>
     * - "N1.HIGHMEM-8"<br>
     * - "N1.HIGHMEM-16"<br>
     * - "N1.HIGHMEM-32"<br>
     * - "N1.HIGHCPU-2"<br>
     * - "N1.HIGHCPU-4"<br>
     * - "N1.HIGHCPU-8"<br>
     * - "N1.HIGHCPU-16"<br>
     * - "N1.HIGHCPU-32"<br>
     * - "F1.MICRO.PREEMPTIBLE"<br>
     * - "G1.SMALL.PREEMPTIBLE"<br>
     * - "N1.STANDARD-1.PREEMPTIBLE"<br>
     * - "N1.STANDARD-2.PREEMPTIBLE"<br>
     * - "N1.STANDARD-4.PREEMPTIBLE"<br>
     * - "N1.STANDARD-8.PREEMPTIBLE"<br>
     * - "N1.STANDARD-16.PREEMPTIBLE"<br>
     * - "N1.STANDARD-32.PREEMPTIBLE"<br>
     * - "N1.HIGHMEM-2.PREEMPTIBLE"<br>
     * - "N1.HIGHMEM-4.PREEMPTIBLE"<br>
     * - "N1.HIGHMEM-8.PREEMPTIBLE"<br>
     * - "N1.HIGHMEM-16.PREEMPTIBLE"<br>
     * - "N1.HIGHMEM-32.PREEMPTIBLE"<br>
     * - "N1.HIGHCPU-2.PREEMPTIBLE"<br>
     * - "N1.HIGHCPU-4.PREEMPTIBLE"<br>
     * - "N1.HIGHCPU-8.PREEMPTIBLE"<br>
     * - "N1.HIGHCPU-16.PREEMPTIBLE"<br>
     * - "N1.HIGHCPU-32.PREEMPTIBLE"<br>
     *
     * @param location Possible Values: <br><br>
     *
     * - "asia" <br>
     * - "europe"<br>
     * - "us"<br>
     *
     * @return JSONObject containing all VM's data that matches with input.
     */
    public JSONObject GoogleComputeEngineInstances(String instanceType, String location) {
        String typeParse[] = instanceType.split("\\.");
        Iterator<String> it = this.getComputeEngine().keys();
        JSONObject result = new JSONObject();
        while (it.hasNext()) {
            String key = it.next();
            boolean vm = true;
            for (String typeParse1 : typeParse) {
                if (!key.contains(typeParse1)) {
                    vm = false;
                }
            }
            if (vm) {
                result.put(key, this.getComputeEngine().getJSONObject(key));
            }
        }
        return (result);
    }

    public ArrayList<JSONObject> getListJsonObjectInstances() {
        ArrayList<JSONObject> result = new ArrayList<>();
        JSONObject aux = new JSONObject();
        for (String instanceTypeName : allInstanceTypeName) {
            for (String location : allLocation) {
                //allSO is the base of so, but we need just linux, so Linux so
                aux = GoogleComputeEngineInstances(instanceTypeName, location);
                System.out.print(aux);
                System.out.println(" " + aux.keys().next());
                if (aux.keys().hasNext()) {
                    result.add(aux);
                }
            }
        }
        return result;
    }

    public ArrayList<Instance> getListInstanceGCE() {
        ArrayList<Instance> listInstancesEc2 = new ArrayList();
//        Instance instanceAux;
//        Double memory,hd, cpuhtz,qtd;
//        String aux= "",hdType="";      
//        ArrayList<JSONObject> listJsonObject =getListJsonObjectInstances();
//        System.out.println("Interno jsonlist: "+listJsonObject.size());
//        for(JSONObject jsonObjectInstance : listJsonObject){
//            JSONObject i = jsonObjectInstance.getJSONObject(jsonObjectInstance.keys().next()).getJSONObject("attributes");
//                    
//            System.out.println("instanceType: "+i.getString("instanceType"));
//            System.out.println("Price: "+instance.getJSONObject(instance.keys().next()).getDouble("price"));
//            System.out.println("location: "+i.getString("location"));
//            System.out.print("memory: "+i.getString("memory"));
//            System.out.print("cpuHtz: "+i.getString("clockSpeed"));
//            System.out.println("cpuType: "+i.getString("physicalProcessor"));
//            aux=i.getString("memory");
//            String part[]=aux.split("(?= )");
//            if(part[0].contains(",")){
//                part[0]=part[0].replace(",", ".");
//            }
//            memory=Double.parseDouble(part[0]);
////            System.out.print("memory: D: "+memory+" S: "+i.getString("memory")+" ");
//            
//            aux=i.getString("clockSpeed");
//            part=aux.split("(?= )");
//
//            if(part.length>3)
//                cpuhtz = Double.parseDouble(part[2]);
//            else
//                cpuhtz = Double.parseDouble(part[0]);
////            System.out.print("cpuHtz: D: "+cpuhtz+" S: " +i.getString("clockSpeed")+" ");
//            
//            aux=i.getString("storage");
//            part=aux.split("(?= )");
//            switch (part.length) {
//                case 2:
//                    hd = 80D;
//                    hdType=part[0];
//                    break;
//                case 3:
//                    qtd = Double.parseDouble(part[0]);
//                    part[2]=part[2].replace(",", ".");
//                    hd = qtd * Double.parseDouble(part[2]);
//                    hdType="HDD";
//                    break;
//                default:
//                    qtd = Double.parseDouble(part[0]);
//                    hd = qtd * Double.parseDouble(part[2]);
//                    hdType=part[3];
//                    break;
//            }
////            System.out.println("storage: D: "+hd+" S: " +i.getString("storage"));
//            
//            
////            System.out.println("processorArchitecture: "+i.getString("processorArchitecture"));
//
//            //String id, String type, Double valueHour, int quantity, String locality, String memory, String cpuHtz, String cpuType, int quantityCPU, String hd, String hdType,String cpuArch, String provider
//            instanceAux =new Instance(jsonObjectInstance.keys().next(), i.getString("instanceType"),jsonObjectInstance.getJSONObject(jsonObjectInstance.keys().next()).getDouble("price"), 0, i.getString("location"), memory,cpuhtz, i.getString("physicalProcessor"),i.getInt("vcpu"), hd, hdType, i.getString("processorArchitecture"),"Amazon EC2");
//            listInstancesEc2.add(instanceAux);
////            }
//        }
//        
        return listInstancesEc2;
    }

    public String[] getAllInstanceType() {
        return allInstanceTypeName;
    }

    public void setAllInstanceType(String[] allInstanceTypeName) {
        this.allInstanceTypeName = allInstanceTypeName;
    }

    public String[] getAllLocation() {
        return allLocation;
    }

    public void setAllLocation(String[] allLocation) {
        this.allLocation = allLocation;
    }

    /**
     * @return the computeEngine
     */
    public JSONObject getComputeEngine() {
        return computeEngine;
    }

    /**
     * @param computeEngine the computeEngine to set
     */
    public void setComputeEngine(JSONObject computeEngine) {
        this.computeEngine = computeEngine;
    }
}
