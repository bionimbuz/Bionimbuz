package br.unb.cic.bionimbus.services.tarification.Google;

import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.model.StorageInstance;
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

    private JSONObject computeEngine, storageEngine;
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
    private String allStorage[] = {"CP-COMPUTEENGINE-STORAGE-PD-CAPACITY",
        "CP-COMPUTEENGINE-STORAGE-PD-SSD",
        "CP-COMPUTEENGINE-PD-IO-REQUEST",
        "CP-COMPUTEENGINE-STORAGE-PD-SNAPSHOT"};

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
        JsonReader.saveJson(computerEngineString, defaultConfigPathname);
        this.computeEngine = getGcpInstance();
        this.storageEngine = getGcpStorage();
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
                new GoogleCloud(server, index);
            } catch (IOException | JSONException ex) {
                Logger.getLogger(GoogleCloud.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.computeEngine = getGcpInstance();
        this.storageEngine = getGcpStorage();
    }

    /**
     * Get gcp_price_list and Computeengine-vimage
     *
     * @return
     */
    private JSONObject getGcpInstance() {
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
        return this.computeEngine;
    }

    private JSONObject getGcpStorage() {
        JSONObject jsonList = JsonReader.readJson(defaultConfigPathname);
        JSONObject priceList = jsonList.getJSONObject("gcp_price_list");
        JSONObject result = new JSONObject();
        result.put("CP-COMPUTEENGINE-STORAGE-PD-CAPACITY", priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-CAPACITY"));
//        System.out.println(priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-CAPACITY"));
        result.put("CP-COMPUTEENGINE-PD-IO-REQUEST", priceList.getJSONObject("CP-COMPUTEENGINE-PD-IO-REQUEST"));
//        System.out.println(priceList.getJSONObject("CP-COMPUTEENGINE-PD-IO-REQUEST"));
        result.put("CP-COMPUTEENGINE-STORAGE-PD-SNAPSHOT", priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-SNAPSHOT"));
//        System.out.println(priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-SNAPSHOT"));
        result.put("CP-COMPUTEENGINE-STORAGE-PD-SSD", priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-SSD"));
//        System.out.println(priceList.getJSONObject("CP-COMPUTEENGINE-STORAGE-PD-SSD"));

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
    public JSONObject googleComputeEngineInstances(String instanceType) {
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
        JSONObject aux;
        for (String instanceTypeName : allInstanceTypeName) {
            aux = googleComputeEngineInstances(instanceTypeName);
            if (aux.keys().hasNext()) {
                result.add(aux);
//                System.out.println(aux);
            }
        }
        return result;
    }

    public ArrayList<Instance> getListInstanceGCE() {
        ArrayList<Instance> listInstancesGCE = new ArrayList();

        Instance instanceAux;
        String id, type, cpuType, locality, cpuArch, provider;
        Double costPerHour = 0D, memoryTotal = 0D, cpuHtz = 0D, hd = 0D, priceHd = 0D, hdType = 0D, numCores = 0D, gceu = 0D;
        int cores;

        ArrayList<JSONObject> listJsonObject = getListJsonObjectInstances();
//        System.out.println("Interno jsonlist: " + listJsonObject.size());
        for (JSONObject jsonObjectInstance : listJsonObject) {
            JSONObject i = jsonObjectInstance.getJSONObject(jsonObjectInstance.keys().next());
            try {
//                System.out.print("Gceu: " + i.getDouble("gceu") + " ,");
                cpuHtz = i.getDouble("gceu");
            } catch (JSONException ex) {
//                System.out.print("Não tem Gceu ,");
                cpuHtz = 0.0D;
            }
//            System.out.print("memory: " + i.getDouble("memory") + " ,");
            memoryTotal = i.getDouble("memory");
            try {
//                System.out.print("cores: " + i.getInt("cores") + " ,");
                cores = i.getInt("cores");
            } catch (JSONException ex) {
//                System.out.print("coresS: " + i.getString("cores") + " ,");
                cores = 0;
            }
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
            if (cores != 0 && cpuHtz != 0) {
                cpuHtz = cpuHtz / cores;
//                System.out.print("CPUHTz: " + cpuHtz + " ,");
            }
            //Need to Improve
            StorageInstance storageI = new StorageInstance(1D, 0.0008, "Bucket", "us", "Google");
//            System.out.print("Price us: " + i.getDouble("us") + " ,");
//            System.out.print("Price europe: " + i.getDouble("europe") + " ,");
//            System.out.print("Price asia: " + i.getDouble("asia") + " ,");
//            System.out.print("Price asia-east: " + i.getDouble("asia-east") + " ,");
//            System.out.println("Price asia-northeast: " + i.getDouble("asia-northeast") + " ,");
//            String id, String type, Double costPerHour, int quantity,
//            String locality, Double memoryTotal, Double cpuHtz, String cpuType,
//            StorageInstance storage, Integer numCores,
//            String cpuArch, String provider
            costPerHour = i.getDouble("us");
            Instance instanceUS = new Instance(jsonObjectInstance.keys().next(), jsonObjectInstance.keys().next(), costPerHour, 0, "us", memoryTotal, cpuHtz, "default", storageI, cores, "default", "Google Compute Engine");
            listInstancesGCE.add(instanceUS);
            costPerHour = i.getDouble("europe");
            Instance instanceEURO = new Instance(jsonObjectInstance.keys().next(), jsonObjectInstance.keys().next(), costPerHour, 0, "europe", memoryTotal, cpuHtz, "default", storageI, cores, "default", "Google Compute Engine");
            listInstancesGCE.add(instanceEURO);
            costPerHour = i.getDouble("asia");
            Instance instanceASIA = new Instance(jsonObjectInstance.keys().next(), jsonObjectInstance.keys().next(), costPerHour, 0, "asia", memoryTotal, cpuHtz, "default", storageI, cores, "default", "Google Compute Engine");
            listInstancesGCE.add(instanceASIA);
            costPerHour = i.getDouble("asia-east");
            Instance instanceASIAE = new Instance(jsonObjectInstance.keys().next(), jsonObjectInstance.keys().next(), costPerHour, 0, "asia-east", memoryTotal, cpuHtz, "default", storageI, cores, "default", "Google Compute Engine");
            listInstancesGCE.add(instanceASIAE);
            costPerHour = i.getDouble("asia-northeast");
            Instance instanceASIAN = new Instance(jsonObjectInstance.keys().next(), jsonObjectInstance.keys().next(), costPerHour, 0, "asia-northeast", memoryTotal, cpuHtz, "default", storageI, cores, "default", "Google Compute Engine");
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

    public JSONObject getComputeEngine() {
        return computeEngine;
    }

    public void setComputeEngine(JSONObject computeEngine) {
        this.computeEngine = computeEngine;
    }

    public JSONObject getStorageEngine() {
        return storageEngine;
    }

    public void setStorageEngine(JSONObject storageEngine) {
        this.storageEngine = storageEngine;
    }
}
