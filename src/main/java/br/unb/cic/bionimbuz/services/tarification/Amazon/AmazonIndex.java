package br.unb.cic.bionimbuz.services.tarification.Amazon;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.amazonaws.util.json.JSONException;

import br.unb.cic.bionimbuz.constants.SystemConstants;
import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.model.StorageInstance;
import br.unb.cic.bionimbuz.services.tarification.JsonReader;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class AmazonIndex {
    
    private static final String ON_DEMAND = "OnDemand";
    private static final String TERMS = "terms";
    private static final String PRODUCTS2 = "products";
    private static final String PRODUCT_FAMILY = "productFamily";
    private static final String COMPUTE_INSTANCE = "Compute Instance";
    private static final String OPERATING_SYSTEM = "operatingSystem";
    private static final String LOCATION2 = "location";
    private static final String INSTANCE_TYPE = "instanceType";
    private static final String NA = "NA";
    private static final String PRE_INSTALLED_SW = "preInstalledSw";
    private static final String LICENSE_MODEL = "licenseModel";
    private static final String SHARED = "Shared";
    private static final String TENANCY = "tenancy";
    private static final String ATTRIBUTES = "attributes";
    private static final String NO_LICENSE_REQUIRED = "No License required";
    private static final String PRICE_DIMENSIONS = "priceDimensions";
    private static final String SKU = "sku";
    private static final String PRICE_PER_UNIT = "pricePerUnit";
    private static final String USD = "USD";
    private static final String END_RANGE = "endRange";
    private static final String BEGIN_RANGE = "beginRange";
    private static final String DESCRIPTION = "description";
    private static final String RATE_CODE = "rateCode";
    private static final String PRICE = "price";
    /**
     * Te following JSONObjects are relative to each of the Amazon Services.
     * They keep the JSON's from AWS with the Service Object's informations.
     *
   
    private JSONObject amazonS3;
    private JSONObject amazonGlacier;
    private JSONObject amazonSES;
    private JSONObject amazonRDS;
    private JSONObject amazonSimpleDB;
    private JSONObject amazonDynamoDB;
    private JSONObject amazonRoute53;
    private JSONObject amazonRedshift;
    private JSONObject amazonElastiCache;
    private JSONObject amazonCloudFront;
    private JSONObject awskms;
    private JSONObject amazonVPC; */
    private JSONObject amazonEC2; 
    //TAKE CARE WITH HTTPS, here work, but on Google just http ...
    final String http = "https://";
    final String server = "pricing.us-east-1.amazonaws.com";
    final String index = "/offers/v1.0/aws/index.json";
    private String allInstanceTypeName[] = { 
        "t2.nano","t2.micro","t2.small","t2medium","t2.large",
        "m4.large","m4.xlarge","m4.2xlarge","m4.4xlarge","m4.4xlarge","m4.10xlarge",
        "m3.medium","m3.large","m3.xlarge","m3.2xlarge",
        "c4.large","c4.xlarge","c4.2xlarge","c4.4xlarge","c4.8xlarge",
        "c3.large","c3.xlarge","c3.2xlarge","c3.4xlarge","c3.8xlarge",
        "g2.2xlarge","g2.8xlarge",
        "x1.32xlarge",
        "r3.large","r3.xlarge","r3.2xlarge","r3.4xlarge","r3.8xlarge",
        "i2.xlarge","i2.2xlarge","i2.4xlarge","i2.8xlarge",
        "d2.xlarge","d2.2xlarge","d2.4xlarge","d2.8xlarge" };
    private String allLocation[] = {
            "Asia Pacific (Mumbai)",
            "Asia Pacific (Seoul)",
            "Asia Pacific (Tokyo)",
            "Asia Pacific (Singapore)",
            "Asia Pacific (Sydney)",
            "South America (Sao Paulo)",
            "AWS GovCloud (US)",
            "US West (Oregon)",
            "US West (N. California)",
            "US East (N. Virginia)",
            "EU (Frankfurt)",
            "EU (Ireland)" };
    private String allSO[] = {
            "Windows",
            "SUSE",
            "Linux",
            "RHEL" };
    
    /**
     * pricing.us-east-1.amazonaws.com/offers/v1.0/aws/AmazonEC2/current/index.json
     * The index constructor is responsible to read the jsons of the services
     * from the URL and create the json objects relatives to the attributes of
     * the class.
     *
     * Note: For BioNimbuz users, this constructor must be used only by the
     * PricingGetterService. If you want to use the info, use the constructor
     * without arguments.
     *
     * @param server
     *            The Server from where you get the jsons.
     * @param index
     *            The index's url.
     * @throws JSONException
     * @throws IOException
     *
     * String AmazonS3 = "https://" + server + amazonServicesURLs.getJSONObject("AmazonS3").getString("currentVersionUrl");
     * this.AmazonS3 = JsonReader.readJsonFromUrl(AmazonS3);
     * JsonReader.saveJson(this.AmazonS3.toString(4), "AmazonS3.json");
     * String AmazonGlacier = "https://" + server + amazonServicesURLs.getJSONObject("AmazonGlacier").getString("currentVersionUrl");
     * this.AmazonGlacier = JsonReader.readJsonFromUrl(AmazonGlacier);
     * JsonReader.saveJson(this.AmazonGlacier.toString(4), "AmazonGlacier.json");
     * String AmazonSES = "https://" + server + amazonServicesURLs.getJSONObject("AmazonSES").getString("currentVersionUrl");
     * this.AmazonSES = JsonReader.readJsonFromUrl(AmazonSES);
     * JsonReader.saveJson(this.AmazonSES.toString(4), "AmazonSES.json");
     * String AmazonRDS = "https://" + server + amazonServicesURLs.getJSONObject("AmazonRDS").getString("currentVersionUrl");
     * this.AmazonRDS = JsonReader.readJsonFromUrl(AmazonRDS);
     * JsonReader.saveJson(this.AmazonRDS.toString(4), "AmazonRDS.json");
     * String AmazonSimpleDB = "https://" + server + amazonServicesURLs.getJSONObject("AmazonSimpleDB").getString("currentVersionUrl");
     * this.AmazonSimpleDB = JsonReader.readJsonFromUrl(AmazonSimpleDB);
     * JsonReader.saveJson(this.AmazonSimpleDB.toString(4), "AmazonSimpleDB.json");
     * String AmazonDynamoDB = "https://" + server + amazonServicesURLs.getJSONObject("AmazonDynamoDB").getString("currentVersionUrl");
     * this.AmazonDynamoDB = JsonReader.readJsonFromUrl(AmazonDynamoDB);
     * JsonReader.saveJson(this.AmazonDynamoDB.toString(4), "AmazonDynamoDB.json");
     * String AmazonRoute53 = "https://" + server + amazonServicesURLs.getJSONObject("AmazonRoute53").getString("currentVersionUrl");
     * this.AmazonRoute53 = JsonReader.readJsonFromUrl(AmazonRoute53);
     * JsonReader.saveJson(this.AmazonRoute53.toString(4), "AmazonRoute53.json");
     * String AmazonRedshift = "https://" + server + amazonServicesURLs.getJSONObject("AmazonRedshift").getString("currentVersionUrl");
     * this.AmazonRedshift = JsonReader.readJsonFromUrl(AmazonRedshift);
     * JsonReader.saveJson(this.AmazonRedshift.toString(4), "AmazonRedshift.json");
     * String AmazonElastiCache = "https://" + server + amazonServicesURLs.getJSONObject("AmazonElastiCache").getString("currentVersionUrl");
     * this.AmazonElastiCache = JsonReader.readJsonFromUrl(AmazonElastiCache);
     * JsonReader.saveJson(this.AmazonElastiCache.toString(4), "AmazonElastiCache.json");
     * String AmazonCloudFront = "https://" + server + amazonServicesURLs.getJSONObject("AmazonCloudFront").getString("currentVersionUrl");
     * this.AmazonCloudFront = JsonReader.readJsonFromUrl(AmazonCloudFront);
     * JsonReader.saveJson(this.AmazonCloudFront.toString(4), "AmazonCloudFront.json");
     * String awskms = "https://" + server + amazonServicesURLs.getJSONObject("awskms").getString("currentVersionUrl");
     * this.awskms = JsonReader.readJsonFromUrl(awskms);
     * JsonReader.saveJson(this.awskms.toString(4), "awskms.json");
     * String AmazonVPC = "https://" + server + amazonServicesURLs.getJSONObject("AmazonVPC").getString("currentVersionUrl");
     * this.AmazonVPC = JsonReader.readJsonFromUrl(AmazonVPC);
     * JsonReader.saveJson(this.AmazonVPC.toString(4), "AmazonVPC.json");
     */
    public AmazonIndex(String server, String index) throws JSONException, IOException {
        final JSONObject amazonServicesURLs = JsonReader.readJsonFromUrl(this.http + server + index);
        // JsonReader.saveJson(amazonServicesURLs.toString(4), defaultConfigPathname);
        final String amazonEC2Instances = this.http + server + amazonServicesURLs.getJSONObject("offers").getJSONObject("AmazonEC2").getString("currentVersionUrl");
        this.amazonEC2 = JsonReader.readJsonFromUrl(amazonEC2Instances);
        this.amazonEC2 =getJsonObjectInstances();
        JsonReader.saveJson(this.amazonEC2.toString(), SystemConstants.FILE_INSTANCES_AMAZON);
//        System.out.println(amazonEC2Instances);
    }
    
    /**
     * This constructor reads the jsons of the services from the archive
     * generated by the constructor above. This constructor allow faster
     * initialization to consult the data about services.
     *
     * For BioNimbuZ users: This should be used to consult data about services.
     * Have fun xD
     * String AmazonS3 = "AmazonS3.json";
     * this.AmazonS3 = JsonReader.readJson(AmazonS3);
     * String AmazonGlacier = "AmazonGlacier.json";
     * this.AmazonGlacier = JsonReader.readJson(AmazonS3);
     * String AmazonSES = "AmazonSES.json";
     * this.AmazonSES = JsonReader.readJson(AmazonSES);
     * String AmazonRDS = "AmazonRDS.json";
     * this.AmazonRDS = JsonReader.readJson(AmazonRDS);
     * String AmazonSimpleDB = "AmazonSimpleDB.json";
     * this.AmazonSimpleDB = JsonReader.readJson(AmazonSimpleDB);
     * String AmazonDynamoDB = "AmazonDynamoDB.json";
     * this.AmazonDynamoDB = JsonReader.readJson(AmazonDynamoDB);
     * String AmazonRoute53 = "AmazonRoute53.json";
     * this.AmazonRoute53 = JsonReader.readJson(AmazonRoute53);
     * String AmazonRedshift = "AmazonRedshift.json";
     * this.AmazonRedshift = JsonReader.readJson(AmazonRedshift);
     * String AmazonElastiCache = "AmazonElastiCache.json";
     * this.AmazonElastiCache = JsonReader.readJson(AmazonElastiCache);
     * String AmazonCloudFront = "AmazonCloudFront.json";
     * this.AmazonCloudFront = JsonReader.readJson(AmazonCloudFront);
     * String awskms = "awskms.json";
     * this.awskms = JsonReader.readJson(awskms);
     * String AmazonVPC = "AmazonVPC.json";
     * this.AmazonVPC = JsonReader.readJson(AmazonVPC);
     */
    public AmazonIndex() {
        JSONObject aux = new JSONObject();
//             final InputStream openedStream = new URL(url).openStream();) {
//            
//            jo = (JSONObject) new JSONTokener(openedStream).nextValue();
        try (final InputStream is = new FileInputStream(SystemConstants.FILE_INSTANCES_AMAZON);) {
            aux = (JSONObject) new JSONTokener(is).nextValue();
        } catch (IOException ex) {
            try {
                AmazonIndex amazonIndex = new AmazonIndex(this.server, this.index);
                aux=amazonIndex.amazonEC2;
            } catch (JSONException | IOException ex1) {
                Logger.getLogger(AmazonIndex.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        
        this.amazonEC2 = aux;
    }
    public AmazonIndex(JSONObject allOffers) {
        this.amazonEC2 = allOffers;
        
    }
    /**
     * This method is used to get Instance Data from EC2 based on the instance
     * type, location and Operational System.
     *
     * @param instanceType
     *            <br>
     *            Possible values:<br>
     *            <br>
     *            - "t2.nano"<br>
     *            - "t2.micro"<br>
     *            - "t2.small"<br>
     *            - "t2medium"<br>
     *            - "t2.large"<br>
     *            - "m4.large"<br>
     *            - "m4.xlarge"<br>
     *            - "m4.2xlarge"<br>
     *            - "m4.4xlarge"<br>
     *            - "m4.10xlarge"<br>
     *            - "m3.medium"<br>
     *            - "m3.large"<br>
     *            - "m3.xlarge"<br>
     *            - "m3.2xlarge"<br>
     *            - "c4.large"<br>
     *            - "c4.xlarge"<br>
     *            - "c4.2xlarge"<br>
     *            - "c4.4xlarge"<br>
     *            - "c4.8xlarge"<br>
     *            - "c3.large"<br>
     *            - "c3.xlarge"<br>
     *            - "c3.2xlarge"<br>
     *            - "c3.4xlarge"<br>
     *            - "c3.8xlarge"<br>
     *            - "g2.2xlarge"<br>
     *            - "g2.8xlarge"<br>
     *            - "x1.32xlarge"<br>
     *            - "r3.large"<br>
     *            - "r3.xlarge"<br>
     *            - "r3.2xlarge"<br>
     *            - "r3.4xlarge"<br>
     *            - "r3.8xlarge"<br>
     *            - "i2.xlarge"<br>
     *            - "i2.2xlarge"<br>
     *            - "i2.4xlarge"<br>
     *            - "i2.8xlarge"<br>
     *            - "d2.xlarge"<br>
     *            - "d2.2xlarge"<br>
     *            - "d2.4xlarge"<br>
     *            - "d2.8xlarge"<br>
     * @param location
     *            Possible values:<br>
     *            <br>
     *            - "Asia Pacific (Mumbai)" <br>
     *            - "Asia Pacific (Seoul)" <br>
     *            - "Asia Pacific (Tokyo)" <br>
     *            - "Asia Pacific (Singapore)" <br>
     *            - "Asia Pacific (Sydney)" <br>
     *            - "South America (Sao Paulo)" <br>
     *            - "AWS GovCloud (US)" <br>
     *            - "US West (Oregon)" <br>
     *            - "US West (N. California)" <br>
     *            - "US East (N. Virginia)" <br>
     *            - "EU (Frankfurt)" <br>
     *            - "EU (Ireland)" <br>
     *
     * @param os
     *            Possible values:<br>
     *            <br>
     *            - "Windows"<br>
     *            - "SUSE"<br>
     *            - "Linux"<br>
     *            - "RHEL"<br>
     *
     * @return A json Object with all the json objects that matches with the
     *         specified input.
     */
    public JSONObject eC2Instances(String instanceType, String location, String os) {
        final JSONObject products = this.amazonEC2.getJSONObject(PRODUCTS2);
        final JSONObject prices = this.amazonEC2.getJSONObject(TERMS).getJSONObject(ON_DEMAND);
        final Iterator<String> it = products.keys();
        JSONObject result = new JSONObject();
        while (it.hasNext()) {
            JSONObject aux = products.getJSONObject(it.next());
            if (aux.has(PRODUCT_FAMILY)) {
                if (aux.getString(PRODUCT_FAMILY).equals(COMPUTE_INSTANCE)) {
                    // aux.getJSONObject("attributes").getString("licenseModel").equals("No License required")
                    // aux.getJSONObject("attributes").getString("licenseModel").equals("License Included")
                    final JSONObject auxAttribs = aux.getJSONObject(ATTRIBUTES);
                    if (auxAttribs.getString(LICENSE_MODEL).equals(NO_LICENSE_REQUIRED) && auxAttribs.getString(TENANCY).equals(SHARED) && auxAttribs.getString(PRE_INSTALLED_SW).equals(NA)
                            && auxAttribs.getString(INSTANCE_TYPE).equals(instanceType) && auxAttribs.getString(LOCATION2).equals(location) && auxAttribs.getString(OPERATING_SYSTEM).equals(os)) {
                        final JSONObject price = prices.getJSONObject(aux.getString(SKU));
                        final Iterator<String> itPricesAux = price.keys();
                        while (itPricesAux.hasNext()) {
                            JSONObject auxPricesObj = price.getJSONObject(itPricesAux.next());
                            auxPricesObj = auxPricesObj.getJSONObject(PRICE_DIMENSIONS);
                            final Iterator<String> itPricesAux1 = auxPricesObj.keys();
                            JSONObject auxPricesObj1 = null;
                            while (itPricesAux1.hasNext()) {
                                auxPricesObj1 = auxPricesObj.getJSONObject(itPricesAux1.next());
                                aux = aux.put(PRICE, auxPricesObj1.getJSONObject(PRICE_PER_UNIT).getDouble(USD));
                                aux = aux.put(RATE_CODE, auxPricesObj1.getString(RATE_CODE));
                                aux = aux.put(DESCRIPTION, auxPricesObj1.getString(DESCRIPTION));
                                aux = aux.put(BEGIN_RANGE, auxPricesObj1.getString(BEGIN_RANGE));
                                aux = aux.put(END_RANGE, auxPricesObj1.getString(END_RANGE));
                                result = result.put(auxPricesObj1.getString(RATE_CODE), aux);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * This method is used to get Instance Data from EC2 based on the instance
     * type and location.
     *
     * @param instanceType
     *            <br>
     *            Possible values:<br>
     *            <br>
     *            - "t2.nano"<br>
     *            - "t2.micro"<br>
     *            - "t2.small"<br>
     *            - "t2medium"<br>
     *            - "t2.large"<br>
     *            - "m4.large"<br>
     *            - "m4.xlarge"<br>
     *            - "m4.2xlarge"<br>
     *            - "m4.4xlarge"<br>
     *            - "m4.10xlarge"<br>
     *            - "m3.medium"<br>
     *            - "m3.large"<br>
     *            - "m3.xlarge"<br>
     *            - "m3.2xlarge"<br>
     *            - "c4.large"<br>
     *            - "c4.xlarge"<br>
     *            - "c4.2xlarge"<br>
     *            - "c4.4xlarge"<br>
     *            - "c4.8xlarge"<br>
     *            - "c3.large"<br>
     *            - "c3.xlarge"<br>
     *            - "c3.2xlarge"<br>
     *            - "c3.4xlarge"<br>
     *            - "c3.8xlarge"<br>
     *            - "g2.2xlarge"<br>
     *            - "g2.8xlarge"<br>
     *            - "x1.32xlarge"<br>
     *            - "r3.large"<br>
     *            - "r3.xlarge"<br>
     *            - "r3.2xlarge"<br>
     *            - "r3.4xlarge"<br>
     *            - "r3.8xlarge"<br>
     *            - "i2.xlarge"<br>
     *            - "i2.2xlarge"<br>
     *            - "i2.4xlarge"<br>
     *            - "i2.8xlarge"<br>
     *            - "d2.xlarge"<br>
     *            - "d2.2xlarge"<br>
     *            - "d2.4xlarge"<br>
     *            - "d2.8xlarge"<br>
     * @param location
     *            Possible values:<br>
     *            <br>
     *            - "Asia Pacific (Mumbai)" <br>
     *            - "Asia Pacific (Seoul)" <br>
     *            - "Asia Pacific (Tokyo)" <br>
     *            - "Asia Pacific (Singapore)" <br>
     *            - "Asia Pacific (Sydney)" <br>
     *            - "South America (Sao Paulo)" <br>
     *            - "AWS GovCloud (US)" <br>
     *            - "US West (Oregon)" <br>
     *            - "US West (N. California)" <br>
     *            - "US East (N. Virginia)" <br>
     *            - "EU (Frankfurt)" <br>
     *            - "EU (Ireland)" <br>
     *
     * @return A json Object with all the json objects that matches with the
     *         specified input.
     */
    public JSONObject eC2Instances(String instanceType, String location) {
        final JSONObject products = this.amazonEC2.getJSONObject(PRODUCTS2);
        final JSONObject prices = this.amazonEC2.getJSONObject(TERMS).getJSONObject(ON_DEMAND);
        final Iterator<String> it = products.keys();
        JSONObject result = new JSONObject();
        while (it.hasNext()) {
            JSONObject aux = products.getJSONObject(it.next());
            if (aux.has(PRODUCT_FAMILY)) {
                if (aux.getString(PRODUCT_FAMILY).equals(COMPUTE_INSTANCE)) {
                    if (aux.getJSONObject(ATTRIBUTES).getString(LICENSE_MODEL).equals(NO_LICENSE_REQUIRED) && aux.getJSONObject(ATTRIBUTES).getString(TENANCY).equals(SHARED)
                            && aux.getJSONObject(ATTRIBUTES).getString(PRE_INSTALLED_SW).equals(NA) && aux.getJSONObject(ATTRIBUTES).getString(INSTANCE_TYPE).equals(instanceType)
                            && aux.getJSONObject(ATTRIBUTES).getString(LOCATION2).equals(location)) {
                        final Iterator<String> itPricesAux = prices.getJSONObject(aux.getString(SKU)).keys();
                        while (itPricesAux.hasNext()) {
                            JSONObject auxPricesObj = prices.getJSONObject(aux.getString(SKU)).getJSONObject(itPricesAux.next());
                            auxPricesObj = auxPricesObj.getJSONObject(PRICE_DIMENSIONS);
                            final Iterator<String> itPricesAux1 = auxPricesObj.keys();
                            while (itPricesAux1.hasNext()) {
                                final JSONObject auxPricesObj1 = auxPricesObj.getJSONObject(itPricesAux1.next());
                                aux = aux.put(PRICE, auxPricesObj1.getJSONObject(PRICE_PER_UNIT).getDouble(USD));
                                aux = aux.put(RATE_CODE, auxPricesObj1.getString(RATE_CODE));
                                result = result.put(auxPricesObj1.getString(RATE_CODE), aux);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * This method is used to get Instance Data from EC2 based on the instance
     * type.
     *
     * @param instanceType
     *            <br>
     *            Possible values:<br>
     *            <br>
     *            - "t2.nano"<br>
     *            - "t2.micro"<br>
     *            - "t2.small"<br>
     *            - "t2medium"<br>
     *            - "t2.large"<br>
     *            - "m4.large"<br>
     *            - "m4.xlarge"<br>
     *            - "m4.2xlarge"<br>
     *            - "m4.4xlarge"<br>
     *            - "m4.10xlarge"<br>
     *            - "m3.medium"<br>
     *            - "m3.large"<br>
     *            - "m3.xlarge"<br>
     *            - "m3.2xlarge"<br>
     *            - "c4.large"<br>
     *            - "c4.xlarge"<br>
     *            - "c4.2xlarge"<br>
     *            - "c4.4xlarge"<br>
     *            - "c4.8xlarge"<br>
     *            - "c3.large"<br>
     *            - "c3.xlarge"<br>
     *            - "c3.2xlarge"<br>
     *            - "c3.4xlarge"<br>
     *            - "c3.8xlarge"<br>
     *            - "g2.2xlarge"<br>
     *            - "g2.8xlarge"<br>
     *            - "x1.32xlarge"<br>
     *            - "r3.large"<br>
     *            - "r3.xlarge"<br>
     *            - "r3.2xlarge"<br>
     *            - "r3.4xlarge"<br>
     *            - "r3.8xlarge"<br>
     *            - "i2.xlarge"<br>
     *            - "i2.2xlarge"<br>
     *            - "i2.4xlarge"<br>
     *            - "i2.8xlarge"<br>
     *            - "d2.xlarge"<br>
     *            - "d2.2xlarge"<br>
     *            - "d2.4xlarge"<br>
     *            - "d2.8xlarge"<br>
     *
     * @return A json Object with all the json objects that matches with the
     *         specified input.
     */
    public JSONObject eC2Instances(String instanceType) {
        final JSONObject products = this.amazonEC2.getJSONObject(PRODUCTS2);
        final JSONObject prices = this.amazonEC2.getJSONObject(TERMS).getJSONObject(ON_DEMAND);
        final Iterator<String> it = products.keys();
        JSONObject result = new JSONObject();
        while (it.hasNext()) {
            JSONObject aux = products.getJSONObject(it.next());
            if (aux.has(PRODUCT_FAMILY)) {
                if (aux.getString(PRODUCT_FAMILY).equals(COMPUTE_INSTANCE)) {
                    if (aux.getJSONObject(ATTRIBUTES).getString(LICENSE_MODEL).equals(NO_LICENSE_REQUIRED) && aux.getJSONObject(ATTRIBUTES).getString(TENANCY).equals(SHARED)
                            && aux.getJSONObject(ATTRIBUTES).getString(PRE_INSTALLED_SW).equals(NA) && aux.getJSONObject(ATTRIBUTES).getString(INSTANCE_TYPE).equals(instanceType)) {
                        final Iterator<String> itPricesAux = prices.getJSONObject(aux.getString(SKU)).keys();
                        while (itPricesAux.hasNext()) {
                            JSONObject auxPricesObj = prices.getJSONObject(aux.getString(SKU)).getJSONObject(itPricesAux.next());
                            auxPricesObj = auxPricesObj.getJSONObject(PRICE_DIMENSIONS);
                            final Iterator<String> itPricesAux1 = auxPricesObj.keys();
                            while (itPricesAux1.hasNext()) {
                                final JSONObject auxPricesObj1 = auxPricesObj.getJSONObject(itPricesAux1.next());
                                aux = aux.put(PRICE, auxPricesObj1.getJSONObject(PRICE_PER_UNIT).getDouble(USD));
                                aux = aux.put(RATE_CODE, auxPricesObj1.getString(RATE_CODE));
                                result = result.put(auxPricesObj1.getString(RATE_CODE), aux);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    /**
     * Method that returns all instances from big json url, all the json offer need to be on 
     * @return 
     */
    public JSONObject getJsonObjectInstances() {
        final JSONObject result = new JSONObject();
        JSONObject aux = new JSONObject();
        for (final String instanceTypeName : this.allInstanceTypeName) {
            for (final String location : this.allLocation) {
                // allSO is the base of so, but we need just linux, so Linux so
                aux = this.eC2Instances(instanceTypeName, location, "Linux");
                // System.out.println(aux);
                if (aux.keys().hasNext()) {
                    result.put(aux.keys().next(), aux);
                }
            }
        }
        return result;
    }

    public ArrayList<JSONObject> getListJsonObjectInstances() {
        final ArrayList<JSONObject> result = new ArrayList<>();
        
        final Iterator<String> it = this.amazonEC2.keys();
        try{
            JSONObject aux = new JSONObject();
            while (it.hasNext()) {
                aux=this.amazonEC2.getJSONObject(it.next());
                result.add(aux);
            }
        }catch(NoSuchElementException ex){
            System.out.println(ex);
        }
        return result;
    }
    
//        System.out.println("Interno jsonlist: " + listJsonObject.size());
        // System.out.println("instanceType: "+i.getString("instanceType"));
        // System.out.println("Price: "+instance.getJSONObject(instance.keys().next()).getDouble("price"));
        // System.out.println("location: "+i.getString("location"));
        // System.out.print("memory: "+i.getString("memory"));
        // System.out.print("cpuHtz: "+i.getString("clockSpeed"));
        // System.out.println("cpuType: "+i.getString("physicalProcessor"));
        // System.out.print("memory: D: "+memory+" S: "+i.getString("memory")+" ");
        // System.out.print("cpuHtz: D: "+cpuhtz+" S: " +i.getString("clockSpeed")+" ");
        // System.out.println("storage: D: "+hd+" S: " +i.getString("storage"));
        // System.out.println("processorArchitecture: "+i.getString("processorArchitecture"));
    public ArrayList<Instance> getListInstanceEc2() {
        final ArrayList<Instance> listInstancesEc2 = new ArrayList();
        Instance instanceAux;
        Double memory, hd, cpuhtz, qtd;
        String aux = "", hdType = "";
        StorageInstance storage = new StorageInstance();
        final ArrayList<JSONObject> listJsonObject = this.getListJsonObjectInstances();
        for (final JSONObject jsonObjectInstance : listJsonObject) {
            final JSONObject i = jsonObjectInstance.getJSONObject(jsonObjectInstance.keys().next()).getJSONObject(ATTRIBUTES);
            aux = i.getString("memory");
            String part[] = aux.split("(?= )");
            if (part[0].contains(",")) {
                part[0] = part[0].replace(",", ".");
            }
            memory = Double.parseDouble(part[0]);
            aux = i.getString("clockSpeed");
            part = aux.split("(?= )");
            if (part.length > 3) {
                cpuhtz = Double.parseDouble(part[2]);
            } else {
                cpuhtz = Double.parseDouble(part[0]);
            }
            aux = i.getString("storage");
            part = aux.split("(?= )");
            switch (part.length) {
                case 2:
                    hd = 80D;
                    hdType = part[0];
                    storage = new StorageInstance(hd, 0.0D, hdType, hdType, "Amazon");
                    break;
                case 3:
                    qtd = Double.parseDouble(part[0]);
                    part[2] = part[2].replace(",", "");
                    hd = qtd * Double.parseDouble(part[2]);
                    hdType = "HDD";
                    storage = new StorageInstance(hd, 0.0D, hdType, hdType, "Amazon");
                    break;
                default:
                    qtd = Double.parseDouble(part[0]);
                    hd = qtd * Double.parseDouble(part[2]);
                    hdType = part[3];
                    storage = new StorageInstance(hd, 0.0D, hdType, hdType, "Amazon");
                    break;
            }
            // String id, String type, Double costPerHour, int quantity,
            // String locality, Double memoryTotal, Double cpuHtz, String cpuType,
            // StorageInstance storage, Integer numCores,
            // String cpuArch, String provider
            instanceAux = new Instance(jsonObjectInstance.keys().next(), i.getString(INSTANCE_TYPE), jsonObjectInstance.getJSONObject(jsonObjectInstance.keys().next()).getDouble(PRICE),
                    i.getString(LOCATION2), memory, cpuhtz, i.getString("physicalProcessor"), storage, i.getInt("vcpu"), i.getString("processorArchitecture"), "Amazon");
            listInstancesEc2.add(instanceAux);
        }
        
        return listInstancesEc2;
    }
    
    public String[] getAllInstanceTypeName() {
        return this.allInstanceTypeName;
    }
    
    public void setAllInstanceTypeName(String[] allInstanceTypeName) {
        this.allInstanceTypeName = allInstanceTypeName;
    }
    
    public String[] getAllLocation() {
        return this.allLocation;
    }
    
    public void setAllLocation(String[] allLocation) {
        this.allLocation = allLocation;
    }
    
    public String[] getAllSO() {
        return this.allSO;
    }
    
    public void setAllSO(String[] allSO) {
        this.allSO = allSO;
    }
}
