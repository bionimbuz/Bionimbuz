package br.unb.cic.bionimbus.services.tarifation.Amazon;

import br.unb.cic.bionimbus.services.tarifation.JsonReader;
import com.amazonaws.util.json.JSONException;
import java.io.IOException;
import java.util.Iterator;
import org.json.JSONObject;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class AmazonIndex {

    /**
     * Te following JSONObjects are relative to each of the Amazon Services.
     * They keep the JSON's from AWS with the Service Object's informations.
     *
     */
    private JSONObject AmazonS3;
    private JSONObject AmazonGlacier;
    private JSONObject AmazonSES;
    private JSONObject AmazonRDS;
    private JSONObject AmazonSimpleDB;
    private JSONObject AmazonDynamoDB;
    private JSONObject AmazonEC2;
    private JSONObject AmazonRoute53;
    private JSONObject AmazonRedshift;
    private JSONObject AmazonElastiCache;
    private JSONObject AmazonCloudFront;
    private JSONObject awskms;
    private JSONObject AmazonVPC;

    /**
     * The index constructor is responsible to read the jsons of the services
     * from the URL and create the json objects relatives to the attributes of
     * the class.
     *
     * Note: For BioNimbuz users, this constructor must be used only by the
     * PricingGetterService. If you want to use the info, use the constructor
     * without arguments.
     *
     * @param server The Server from where you get the jsons.
     * @param index The index's url.
     * @throws JSONException
     * @throws IOException
     */
    public AmazonIndex(String server, String index) throws JSONException, IOException {
        JSONObject amazonServicesURLs = JsonReader.readJsonFromUrl("https://" + "pricing.us-east-1.amazonaws.com" + "/offers/v1.0/aws/index.json");
        JsonReader.saveJson(amazonServicesURLs.toString(4), "index.json");
        amazonServicesURLs = amazonServicesURLs.getJSONObject("offers");
        /*
        String AmazonS3 = "https://" + server + amazonServicesURLs.getJSONObject("AmazonS3").getString("currentVersionUrl");
        this.AmazonS3 = JsonReader.readJsonFromUrl(AmazonS3);
        JsonReader.saveJson(this.AmazonS3.toString(4), "AmazonS3.json");
        String AmazonGlacier = "https://" + server + amazonServicesURLs.getJSONObject("AmazonGlacier").getString("currentVersionUrl");
        this.AmazonGlacier = JsonReader.readJsonFromUrl(AmazonGlacier);
        JsonReader.saveJson(this.AmazonGlacier.toString(4), "AmazonGlacier.json");
        String AmazonSES = "https://" + server + amazonServicesURLs.getJSONObject("AmazonSES").getString("currentVersionUrl");
        this.AmazonSES = JsonReader.readJsonFromUrl(AmazonSES);
        JsonReader.saveJson(this.AmazonSES.toString(4), "AmazonSES.json");
        String AmazonRDS = "https://" + server + amazonServicesURLs.getJSONObject("AmazonRDS").getString("currentVersionUrl");
        this.AmazonRDS = JsonReader.readJsonFromUrl(AmazonRDS);
        JsonReader.saveJson(this.AmazonRDS.toString(4), "AmazonRDS.json");
        String AmazonSimpleDB = "https://" + server + amazonServicesURLs.getJSONObject("AmazonSimpleDB").getString("currentVersionUrl");
        this.AmazonSimpleDB = JsonReader.readJsonFromUrl(AmazonSimpleDB);
        JsonReader.saveJson(this.AmazonSimpleDB.toString(4), "AmazonSimpleDB.json");
        String AmazonDynamoDB = "https://" + server + amazonServicesURLs.getJSONObject("AmazonDynamoDB").getString("currentVersionUrl");
        this.AmazonDynamoDB = JsonReader.readJsonFromUrl(AmazonDynamoDB);
        JsonReader.saveJson(this.AmazonDynamoDB.toString(4), "AmazonDynamoDB.json");
        String AmazonRoute53 = "https://" + server + amazonServicesURLs.getJSONObject("AmazonRoute53").getString("currentVersionUrl");
        this.AmazonRoute53 = JsonReader.readJsonFromUrl(AmazonRoute53);
        JsonReader.saveJson(this.AmazonRoute53.toString(4), "AmazonRoute53.json");
        String AmazonRedshift = "https://" + server + amazonServicesURLs.getJSONObject("AmazonRedshift").getString("currentVersionUrl");
        this.AmazonRedshift = JsonReader.readJsonFromUrl(AmazonRedshift);
        JsonReader.saveJson(this.AmazonRedshift.toString(4), "AmazonRedshift.json");
        String AmazonElastiCache = "https://" + server + amazonServicesURLs.getJSONObject("AmazonElastiCache").getString("currentVersionUrl");
        this.AmazonElastiCache = JsonReader.readJsonFromUrl(AmazonElastiCache);
        JsonReader.saveJson(this.AmazonElastiCache.toString(4), "AmazonElastiCache.json");
        String AmazonCloudFront = "https://" + server + amazonServicesURLs.getJSONObject("AmazonCloudFront").getString("currentVersionUrl");
        this.AmazonCloudFront = JsonReader.readJsonFromUrl(AmazonCloudFront);
        JsonReader.saveJson(this.AmazonCloudFront.toString(4), "AmazonCloudFront.json");
        String awskms = "https://" + server + amazonServicesURLs.getJSONObject("awskms").getString("currentVersionUrl");
        this.awskms = JsonReader.readJsonFromUrl(awskms);
        JsonReader.saveJson(this.awskms.toString(4), "awskms.json");
        String AmazonVPC = "https://" + server + amazonServicesURLs.getJSONObject("AmazonVPC").getString("currentVersionUrl");
        this.AmazonVPC = JsonReader.readJsonFromUrl(AmazonVPC);
        JsonReader.saveJson(this.AmazonVPC.toString(4), "AmazonVPC.json");
         */
        String AmazonEC2 = "https://" + server + amazonServicesURLs.getJSONObject("AmazonEC2").getString("currentVersionUrl");
        this.AmazonEC2 = JsonReader.readJsonFromUrl(AmazonEC2);
        JsonReader.saveJson(this.AmazonEC2.toString(4), "AmazonEC2.json");
        System.out.println(AmazonEC2);
    }

    /**
     * This constructor reads the jsons of the services from the archive
     * generated by the constructor above. This constructor allow faster
     * initialization to consult the data about services.
     *
     * For BioNimbuZ users: This should be used to consult data about services.
     * Have fun xD
     */
    public AmazonIndex() {
        /*
        String AmazonS3 = "AmazonS3.json";
        this.AmazonS3 = JsonReader.readJson(AmazonS3);
        String AmazonGlacier = "AmazonGlacier.json";
        this.AmazonGlacier = JsonReader.readJson(AmazonS3);
        String AmazonSES = "AmazonSES.json";
        this.AmazonSES = JsonReader.readJson(AmazonSES);
        String AmazonRDS = "AmazonRDS.json";
        this.AmazonRDS = JsonReader.readJson(AmazonRDS);
        String AmazonSimpleDB = "AmazonSimpleDB.json";
        this.AmazonSimpleDB = JsonReader.readJson(AmazonSimpleDB);
        String AmazonDynamoDB = "AmazonDynamoDB.json";
        this.AmazonDynamoDB = JsonReader.readJson(AmazonDynamoDB);
        String AmazonRoute53 = "AmazonRoute53.json";
        this.AmazonRoute53 = JsonReader.readJson(AmazonRoute53);
        String AmazonRedshift = "AmazonRedshift.json";
        this.AmazonRedshift = JsonReader.readJson(AmazonRedshift);
        String AmazonElastiCache = "AmazonElastiCache.json";
        this.AmazonElastiCache = JsonReader.readJson(AmazonElastiCache);
        String AmazonCloudFront = "AmazonCloudFront.json";
        this.AmazonCloudFront = JsonReader.readJson(AmazonCloudFront);
        String awskms = "awskms.json";
        this.awskms = JsonReader.readJson(awskms);
        String AmazonVPC = "AmazonVPC.json";
        this.AmazonVPC = JsonReader.readJson(AmazonVPC);
         */
        String AmazonEC2 = "AmazonEC2.json";
        this.AmazonEC2 = JsonReader.readJson(AmazonEC2);
    }

    /**
     * This method is used to get Instance Data from EC2 based on the instance
     * type, location and Operational System.
     *
     * @param instanceType Specifies the instance type (as in the json).
     * @param location Specifies the location (as in the json).
     * @param os Specifies the OS (as in the json).
     * @return A json Object with all the json objects that matches with the
     * specified input.
     */
    public JSONObject EC2Instances(String instanceType, String location, String os) {
        JSONObject products = this.AmazonEC2.getJSONObject("products");
        JSONObject prices = this.AmazonEC2.getJSONObject("terms").getJSONObject("OnDemand");
        Iterator<String> it = products.keys();
        JSONObject result = new JSONObject();
        while (it.hasNext()) {
            JSONObject aux = products.getJSONObject(it.next());
            if (aux.has("productFamily")) {
                if (aux.getString("productFamily").equals("Compute Instance")) {
                    if ((aux.getJSONObject("attributes").getString("licenseModel").equals("License Included")) && (aux.getJSONObject("attributes").getString("tenancy").equals("Shared")) && (aux.getJSONObject("attributes").getString("preInstalledSw").equals("NA")) && (aux.getJSONObject("attributes").getString("instanceType").equals(instanceType)) && (aux.getJSONObject("attributes").getString("location").equals(location)) && (aux.getJSONObject("attributes").getString("operatingSystem").equals(os))) {
                        Iterator<String> itPricesAux = prices.getJSONObject(aux.getString("sku")).keys();
                        while (itPricesAux.hasNext()) {
                            JSONObject auxPricesObj = prices.getJSONObject(aux.getString("sku")).getJSONObject(itPricesAux.next());
                            auxPricesObj = auxPricesObj.getJSONObject("priceDimensions");
                            Iterator<String> itPricesAux1 = auxPricesObj.keys();
                            while (itPricesAux1.hasNext()) {
                                JSONObject auxPricesObj1 = auxPricesObj.getJSONObject(itPricesAux1.next());
                                aux = aux.put("price", auxPricesObj1.getJSONObject("pricePerUnit").getDouble("USD"));
                                aux = aux.put("rateCode", auxPricesObj1.getString("rateCode"));
                                aux = aux.put("description", auxPricesObj1.getString("description"));
                                aux = aux.put("beginRange", auxPricesObj1.getString("beginRange"));
                                aux = aux.put("endRange", auxPricesObj1.getString("endRange"));
                                result = result.put(auxPricesObj1.getString("rateCode"), aux);
                            }
                        }
                    }
                }
            }
        }
        return (result);
    }

    /**
     * This method is used to get Instance Data from EC2 based on the instance
     * type and location.
     *
     * @param instanceType Specifies the instance type (as in the json).
     * @param location Specifies the location (as in the json).
     * @return A json Object with all the json objects that matches with the
     * specified input.
     */
    public JSONObject EC2Instances(String instanceType, String location) {
        JSONObject products = this.AmazonEC2.getJSONObject("products");
        JSONObject prices = this.AmazonEC2.getJSONObject("terms").getJSONObject("OnDemand");
        Iterator<String> it = products.keys();
        JSONObject result = new JSONObject();
        while (it.hasNext()) {
            JSONObject aux = products.getJSONObject(it.next());
            if (aux.has("productFamily")) {
                if (aux.getString("productFamily").equals("Compute Instance")) {
                    if ((aux.getJSONObject("attributes").getString("licenseModel").equals("License Included")) && (aux.getJSONObject("attributes").getString("tenancy").equals("Shared")) && (aux.getJSONObject("attributes").getString("preInstalledSw").equals("NA")) && (aux.getJSONObject("attributes").getString("instanceType").equals(instanceType)) && (aux.getJSONObject("attributes").getString("location").equals(location))) {
                        Iterator<String> itPricesAux = prices.getJSONObject(aux.getString("sku")).keys();
                        while (itPricesAux.hasNext()) {
                            JSONObject auxPricesObj = prices.getJSONObject(aux.getString("sku")).getJSONObject(itPricesAux.next());
                            auxPricesObj = auxPricesObj.getJSONObject("priceDimensions");
                            Iterator<String> itPricesAux1 = auxPricesObj.keys();
                            while (itPricesAux1.hasNext()) {
                                JSONObject auxPricesObj1 = auxPricesObj.getJSONObject(itPricesAux1.next());
                                aux = aux.put("price", auxPricesObj1.getJSONObject("pricePerUnit").getDouble("USD"));
                                aux = aux.put("rateCode", auxPricesObj1.getString("rateCode"));
                                result = result.put(auxPricesObj1.getString("rateCode"), aux);
                            }
                        }
                    }
                }
            }
        }
        return (result);
    }

    /**
     * This method is used to get Instance Data from EC2 based on the instance
     * type.
     *
     * @param instanceType Specifies the instance type (as in the json).
     * @return A json Object with all the json objects that matches with the
     * specified input.
     */
    public JSONObject EC2Instances(String instanceType) {
        JSONObject products = this.AmazonEC2.getJSONObject("products");
        JSONObject prices = this.AmazonEC2.getJSONObject("terms").getJSONObject("OnDemand");
        Iterator<String> it = products.keys();
        JSONObject result = new JSONObject();
        while (it.hasNext()) {
            JSONObject aux = products.getJSONObject(it.next());
            if (aux.has("productFamily")) {
                if (aux.getString("productFamily").equals("Compute Instance")) {
                    if ((aux.getJSONObject("attributes").getString("licenseModel").equals("License Included")) && (aux.getJSONObject("attributes").getString("tenancy").equals("Shared")) && (aux.getJSONObject("attributes").getString("preInstalledSw").equals("NA")) && (aux.getJSONObject("attributes").getString("instanceType").equals(instanceType))) {
                        Iterator<String> itPricesAux = prices.getJSONObject(aux.getString("sku")).keys();
                        while (itPricesAux.hasNext()) {
                            JSONObject auxPricesObj = prices.getJSONObject(aux.getString("sku")).getJSONObject(itPricesAux.next());
                            auxPricesObj = auxPricesObj.getJSONObject("priceDimensions");
                            Iterator<String> itPricesAux1 = auxPricesObj.keys();
                            while (itPricesAux1.hasNext()) {
                                JSONObject auxPricesObj1 = auxPricesObj.getJSONObject(itPricesAux1.next());
                                aux = aux.put("price", auxPricesObj1.getJSONObject("pricePerUnit").getDouble("USD"));
                                aux = aux.put("rateCode", auxPricesObj1.getString("rateCode"));
                                result = result.put(auxPricesObj1.getString("rateCode"), aux);
                            }
                        }
                    }
                }
            }
        }
        return (result);
    }
}
