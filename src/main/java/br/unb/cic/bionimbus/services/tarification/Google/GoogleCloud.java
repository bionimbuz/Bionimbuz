package br.unb.cic.bionimbus.services.tarification.Google;

import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.services.tarification.JsonReader;
import br.unb.cic.bionimbus.services.tarification.Utils.RestfulGetterBehaviors.PricingGet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONObject;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class GoogleCloud {

    private JSONObject ComputeEngine;
    final String defaultConfigPathname = System.getProperty("user.home") + "/Bionimbuz/conf/GoogleCloud.json";
    final String server= "cloudpricingcalculator.appspot.com";
    final String index= "/static/data/pricelist.json";
    private String allInstanceType[] ={"F1.MICRO", "G1.SMALL", "N1.STANDARD-1", 
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
    private String allLocation []= {"asia", "europe", "us"};
    
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
    public GoogleCloud(String server, String index) throws IOException {
        PricingGet getter = new PricingGet();
        String ComputerEngineString = getter.get(server, index);
        JsonReader.saveJson(ComputerEngineString, defaultConfigPathname);
        this.ComputeEngine = new JSONObject(ComputerEngineString);
        System.out.println(this.ComputeEngine.toString(4));
    }

    /**
     * Constructor responsible for obtaining dara about Google Cloud Compute
     * Engine's VMs from local file.<br>
     */
    public GoogleCloud() {
//        String ComputeEngine = "GoogleCloud.json";

        File f = new File(defaultConfigPathname);
        if(f.exists() && !f.isDirectory()) { 
            // do something
            this.ComputeEngine = JsonReader.readJson(defaultConfigPathname);
        }else
            GoogleComputeEngineInstances(index, server);
 
        this.ComputeEngine = this.ComputeEngine.getJSONObject("gcp_price_list");
        Iterator<String> it = this.ComputeEngine.keys();
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
            this.ComputeEngine.remove(key);
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
        Iterator<String> it = this.ComputeEngine.keys();
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
                result.put(key, this.ComputeEngine.getJSONObject(key));
            }
        }
        return (result);
    }
    public ArrayList<Instance> getInstancesAmazon(){
        
        return null;
    }

    /**
     * @return the allInstanceType
     */
    public String[] getAllInstanceType() {
        return allInstanceType;
    }

    /**
     * @param allInstanceType the allInstanceType to set
     */
    public void setAllInstanceType(String[] allInstanceType) {
        this.allInstanceType = allInstanceType;
    }

    /**
     * @return the allLocation
     */
    public String[] getAllLocation() {
        return allLocation;
    }

    /**
     * @param allLocation the allLocation to set
     */
    public void setAllLocation(String[] allLocation) {
        this.allLocation = allLocation;
    }
}
