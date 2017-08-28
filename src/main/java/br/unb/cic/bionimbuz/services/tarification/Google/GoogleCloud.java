package br.unb.cic.bionimbuz.services.tarification.Google;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import br.unb.cic.bionimbuz.constants.SystemConstants;
import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.model.StorageInstance;
import br.unb.cic.bionimbuz.services.tarification.JsonReader;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class GoogleCloud {

    private static final String GOOGLE = "Google";
    private static final String DEFAULT = "default";
    private static final String ASIANORTHEAST = "asia-northeast";
    private static final String ASIAEAST = "asia-east";
    private static final String ASIA = "asia";
    private static final String EUROPE = "europe";
    private static final String US = "us";
    private static final String CPCOMPUTEENGINESTORAGEPDSSD = "CP-COMPUTEENGINE-STORAGE-PD-SSD";
    private static final String CPCOMPUTEENGINESTORAGEPDSNAPSHOT = "CP-COMPUTEENGINE-STORAGE-PD-SNAPSHOT";
    private static final String CPCOMPUTEENGINEPDIOREQUEST = "CP-COMPUTEENGINE-PD-IO-REQUEST";
    private static final String CPCOMPUTEENGINESTORAGEPDCAPACITY = "CP-COMPUTEENGINE-STORAGE-PD-CAPACITY";
    private static final String COMPUTEENGINEVMIMAGE = "COMPUTEENGINE-VMIMAGE";
    private static final String GCP_PRICE_LIST = "gcp_price_list";
    private JSONObject computeEngine, storageEngine;
    //TAKE CARE WITH HTTPS, on Google just http ...
    final String http = "http://";
    final String server = "www.cloudpricingcalculator.appspot.com";
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
//    private String allStorage[] = {CPCOMPUTEENGINESTORAGEPDCAPACITY, CPCOMPUTEENGINESTORAGEPDSSD, CPCOMPUTEENGINEPDIOREQUEST, CPCOMPUTEENGINESTORAGEPDSNAPSHOT};

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
//        PricingGet getter = new PricingGet();
//        String computerEngineString = getter.get(server, index);
    public GoogleCloud(String server, String index) throws IOException, JSONException {
        this.computeEngine = JsonReader.readJsonFromUrl(http + server + index);
        this.storageEngine = getGcpStorage();
        this.computeEngine = getGcpInstance();
        JsonReader.saveJson(this.computeEngine.toString(), SystemConstants.FILE_INSTANCES_GOOGLE);
        JsonReader.saveJson(this.storageEngine.toString(), SystemConstants.FILE_STORAGES_GOOGLE);

    }

    /**
     * Constructor responsible for obtaining dara about Google Cloud Compute
     * Engine's VMs from local file.<br>
     */
    public GoogleCloud() {
//        String computeEngine = "GoogleCloud.json";
        JSONObject aux = null;
        try (InputStream is = new FileInputStream(SystemConstants.FILE_INSTANCES_GOOGLE);) {
            aux = (JSONObject) new JSONTokener(is).nextValue();
        } catch (IOException ex) {
            try {
                GoogleCloud googleCloud = new GoogleCloud(server, index);
                aux = googleCloud.computeEngine;
            } catch (IOException | JSONException ex1) {
                Logger.getLogger(GoogleCloud.class.getName()).log(Level.SEVERE, null, ex);
            }
//            Logger.getLogger(GoogleCloud.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.computeEngine = aux;
        try (InputStream is = new FileInputStream(SystemConstants.FILE_STORAGES_GOOGLE);) {
            aux = (JSONObject) new JSONTokener(is).nextValue();
        } catch (IOException ex) {
            Logger.getLogger(GoogleCloud.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.storageEngine = aux;
    }

    /**
     * Get gcp_price_list and Computeengine-vimage
     *
     * @return JSONObject
     */
    private JSONObject getGcpInstance() {
//        this.computeEngine = JsonReader.readJson(defaultConfigPathnameInstance);
        JSONObject aux = this.computeEngine.getJSONObject(GCP_PRICE_LIST);
        Iterator<String> it = aux.keys();
        ArrayList<String> invalidKeys = new ArrayList<>();
        while (it.hasNext()) {
            String key = it.next();
            if (!key.contains(COMPUTEENGINEVMIMAGE)) {
                invalidKeys.add(key);
            }
        }
        it = invalidKeys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            aux.remove(key);
        }
        return aux;
    }
//        JSONObject priceList = aux.getJSONObject("gcp_price_list");
//        System.out.println(priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-CAPACITY"));
//        System.out.println(priceList.getJSONObject("CP-COMPUTEENGINE-PD-IO-REQUEST"));
//        System.out.println(priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-SNAPSHOT"));
//        System.out.println(priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-SSD"));

    /**
     * Method get all the storage price for compute engine and returns a
     * objectjson with they
     *
     * @return JSONObject
     */
    private JSONObject getGcpStorage() {
        JSONObject result = new JSONObject();
        try {
            result.put(CPCOMPUTEENGINESTORAGEPDCAPACITY, this.storageEngine.getJSONObject(CPCOMPUTEENGINESTORAGEPDCAPACITY));
            result.put(CPCOMPUTEENGINEPDIOREQUEST, this.storageEngine.getJSONObject(CPCOMPUTEENGINEPDIOREQUEST));
            result.put(CPCOMPUTEENGINESTORAGEPDSNAPSHOT, this.storageEngine.getJSONObject(CPCOMPUTEENGINESTORAGEPDSNAPSHOT));
            result.put(CPCOMPUTEENGINESTORAGEPDSSD, this.storageEngine.getJSONObject(CPCOMPUTEENGINESTORAGEPDSSD));
        } catch (NullPointerException | JSONException ex) {
            result.put(CPCOMPUTEENGINESTORAGEPDCAPACITY, this.computeEngine.getJSONObject(GCP_PRICE_LIST).getJSONObject(CPCOMPUTEENGINESTORAGEPDCAPACITY));
            result.put(CPCOMPUTEENGINEPDIOREQUEST, this.computeEngine.getJSONObject(GCP_PRICE_LIST).getJSONObject(CPCOMPUTEENGINEPDIOREQUEST));
            result.put(CPCOMPUTEENGINESTORAGEPDSNAPSHOT, this.computeEngine.getJSONObject(GCP_PRICE_LIST).getJSONObject(CPCOMPUTEENGINESTORAGEPDSNAPSHOT));
            result.put(CPCOMPUTEENGINESTORAGEPDSSD, this.computeEngine.getJSONObject(GCP_PRICE_LIST).getJSONObject(CPCOMPUTEENGINESTORAGEPDSSD));
        }
        return (result);
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
     * @return JSONObject containing all VM's data that matches with input.
     */
    public JSONObject gCEngineInstances(String instanceType) {
        String typeParse[] = instanceType.split("\\.");
        Iterator<String> it = this.computeEngine.keys();
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
                result.put(key, this.computeEngine.getJSONObject(key));
            }
        }
        return (result);
    }

    /**
     * Method get the computengine with just the instances and return a list
     * with Jsonobjects
     *
     * @return ArrayList<JSONObject>
     */
//    public ArrayList<JSONObject> getListJsonObjectInstances() {
//        ArrayList<JSONObject> result = new ArrayList<>();
//
//        final Iterator<String> it = this.computeEngine.keys();
//        while (it.hasNext()) {
//            JSONObject aux = new JSONObject();
//            aux.put(it.next(), this.computeEngine.getJSONObject(it.next()));
//            result.add(aux);
//        }
//        return result;
//    }

//        System.out.println("Interno jsonlist: " + listJsonObject.size());
//                System.out.print("Gceu: " + i.getDouble("gceu") + " ,");
//                System.out.print("Não tem Gceu ,");
//            System.out.print("memory: " + i.getDouble("memory") + " ,");
//                System.out.print("cores: " + i.getInt("cores") + " ,");
//                System.out.print("coresS: " + i.getString("cores") + " ,");
//            try {
//                System.out.print("maxNumberOfPd: " + i.getInt("maxNumberOfPd") + "maxPdSize: " + i.getInt("maxPdSize") + ", ");
//            } catch (JSONException ex) {
//                System.out.print("Não tem MaxNumberOfPd nem Maxpdsize, ");
//            }
//            try {
//                System.out.print("SSD: " + i.get("ssd").toString() + " ,");
//            } catch (JSONException ex) {
//                System.out.print("Não tem SSD");
//            }
//                System.out.print("CPUHTz: " + cpuHtz + " ,");
//            System.out.print("Price us: " + i.getDouble("us") + " ,");
//            System.out.print("Price europe: " + i.getDouble("europe") + " ,");
//            System.out.print("Price asia: " + i.getDouble("asia") + " ,");
//            System.out.print("Price asia-east: " + i.getDouble("asia-east") + " ,");
//            System.out.println("Price asia-northeast: " + i.getDouble("asia-northeast") + " ,");
//            String id, String type, Double costPerHour, int quantity,
//            String locality, Double memoryTotal, Double cpuHtz, String cpuType,
//            StorageInstance storage, Integer numCores,
//            String cpuArch, String provider
//            System.out.print("TYPE: "+key);
//            System.out.print("");
//            System.out.print("gceu/cores: "+ gceu+"/");
//            System.out.print(cores + " =");
//            System.out.println("CPUHTz: " + cpuHtz );
    /**
     * Method that get googleengine with all instance object json and return
     *
     * @return ArrayList<Instance>
     */
    public ArrayList<Instance> getListInstanceGCE() {
        ArrayList<Instance> listInstancesGCE = new ArrayList<>();
        
        
        Double costPerHour = 0D, memoryTotal = 0D, cpuHtz = 0D, gceu = 0D;
        int cores=0;
        String key;
        final Iterator<String> it = this.computeEngine.keys();
        while (it.hasNext()) {
            key=it.next();
            JSONObject aux = this.computeEngine.getJSONObject(key);
            try {
                gceu = aux.getDouble("gceu");
            } catch (JSONException ex) {
                gceu = 0.0D;
            }
            memoryTotal = aux.getDouble("memory");
            try {
                cores = aux.getInt("cores");
            } catch (JSONException ex) {
                cores = 0;
            }
            if (cores != 0 && gceu != 0.0D) 
                cpuHtz = gceu / cores;
            else
                cpuHtz=0.0D;
            String type ="";
            try{
                type = key.substring(25).toLowerCase();
            }catch(Exception e){
                type = key;
            }
            //Need to Improve 0.026 is the price offer from bucket for mounth, 
            //divide by 30 days and after for 24 hour(0,026÷30)/24=0,000036111
            StorageInstance storageI = new StorageInstance(1D, 0.000036111, "Bucket", US, GOOGLE);
            costPerHour = aux.getDouble(US);
            Instance instanceUS = new Instance(key, type, costPerHour, US, memoryTotal, cpuHtz, DEFAULT, storageI, cores, DEFAULT, GOOGLE);
            listInstancesGCE.add(instanceUS);
            costPerHour = aux.getDouble(EUROPE);
            Instance instanceEURO = new Instance(key, type, costPerHour, EUROPE, memoryTotal, cpuHtz, DEFAULT, storageI, cores, DEFAULT, GOOGLE);
            listInstancesGCE.add(instanceEURO);
            costPerHour = aux.getDouble(ASIA);
            Instance instanceASIA = new Instance(key, type, costPerHour, ASIA, memoryTotal, cpuHtz, DEFAULT, storageI, cores, DEFAULT, GOOGLE);
            listInstancesGCE.add(instanceASIA);
            costPerHour = aux.getDouble(ASIAEAST);
            Instance instanceASIAE = new Instance(key,type, costPerHour, ASIAEAST, memoryTotal, cpuHtz, DEFAULT, storageI, cores, DEFAULT, GOOGLE);
            listInstancesGCE.add(instanceASIAE);
            costPerHour = aux.getDouble(ASIANORTHEAST);
            Instance instanceASIAN = new Instance(key, type, costPerHour, ASIANORTHEAST, memoryTotal, cpuHtz, DEFAULT, storageI, cores, DEFAULT, GOOGLE);
            listInstancesGCE.add(instanceASIAN);
        }
        return listInstancesGCE;
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


}
