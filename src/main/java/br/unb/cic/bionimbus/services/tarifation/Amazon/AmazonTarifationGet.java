package br.unb.cic.bionimbus.services.tarifation.Amazon;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*Classe relacionada à atualização e obtenção de preços*/
public class AmazonTarifationGet {

    private Map<String, AmazonVirtualMachine> AmazonMachines;
    private Map<String, AmazonStorage> AmazonStorageService;
    private Map<String, AmazonDataTransfer> AmazonDataTransferServices;
    private Map<String, String> config;

    //Server: "info.awsstream.com"
    //AddressOD: "/instances.json?"
    //AddressStorage: "/storage.json?"
    //AddressDataTransfer: "/transfer.json?"
    /*Construtor utilizado no caso de atualização: Nessa situação, um GET é realizado para obtenção dos preços da AWS*/
    public AmazonTarifationGet() {

        try {
            JSONArray pricingODArray;
            JSONArray pricingStorageArray;
            JSONArray pricingDataTransferArray;
            this.AmazonMachines = new HashMap<>();
            this.AmazonStorageService = new HashMap<>();
            this.AmazonDataTransferServices = new HashMap<>();
            this.config = new HashMap<>();
            this.config.put("Server", "info.awsstream.com");
            this.config.put("AddressOD", "/instances.json?");
            this.config.put("FilenameOD", "AmazonInstancesOD.txt");
            this.config.put("AddressStorage", "/storage.json?");
            this.config.put("FilenameStorage", "AmazonStorage.txt");
            this.config.put("AddressDataTransfer", "/transfer.json?");
            this.config.put("FilenameDataTransfer", "AmazonDataTransfer.txt");
            System.out.println("Getting prices On Demand...");
            String arrayODString = this.pricingGET(this.config.get("Server"), this.config.get("AddressOD"));
            System.out.println("Getting Storage prices...");
            String arrayStorageString = this.pricingGET(this.config.get("Server"), this.config.get("AddressStorage"));
            System.out.println("Getting Data Transfer prices...");
            String arrayDataTransferString = this.pricingGET(this.config.get("Server"), this.config.get("AddressDataTransfer"));
            System.out.println("Completed.");
            if (arrayODString != null) {
                System.out.println("Saving prices On Demand...");
                this.saveJsonArray(arrayODString, this.config.get("FilenameOD"));
                pricingODArray = new JSONArray(arrayODString);
                System.out.println("Saved.");
            } else {
                pricingODArray = readJsonArray(this.config.get("FilenameOD"));
            }

            this.createVirtualMachines(pricingODArray);

            if (arrayStorageString != null) {
                System.out.println("Saving Storage prices...");
                this.saveJsonArray(arrayStorageString, this.config.get("FilenameStorage"));
                pricingStorageArray = new JSONArray(arrayStorageString);
                System.out.println("Saved.");
            } else {
                pricingStorageArray = readJsonArray(this.config.get("FilenameStorage"));
            }

            this.createStorageInfo(pricingStorageArray);

            if (arrayDataTransferString != null) {
                System.out.println("Saving Data Transfer prices...");
                this.saveJsonArray(arrayStorageString, this.config.get("FilenameDataTransfer"));
                pricingDataTransferArray = new JSONArray(arrayDataTransferString);
                System.out.println("Saved.");
            } else {
                pricingDataTransferArray = readJsonArray(this.config.get("FilenameDataTransfer"));
            }

            this.createDataTransferInfo(pricingDataTransferArray);

        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("GET process completed.");
        }
    }

    private void createDataTransferInfo(JSONArray AmazonDataTransferArray) throws JSONException {
        for (int i = 0; i < AmazonDataTransferArray.length(); i++) {
            JSONObject obj = AmazonDataTransferArray.getJSONObject(i);
            AmazonDataTransfer adt = new AmazonDataTransfer(obj.getInt("id"), obj.getString("region"), obj.getString("kind"), obj.getString("tier"), obj.getDouble("price"), obj.getString("created_at"), obj.getString("updated_at"));
            this.AmazonDataTransferServices.put("" + obj.getInt("id"), adt);
        }
    }

    private void createVirtualMachines(JSONArray AmazonMachinesArray) throws JSONException {
        for (int i = 0; i < AmazonMachinesArray.length(); i++) {
            JSONObject obj = AmazonMachinesArray.getJSONObject(i);
            AmazonVirtualMachine avm = new AmazonVirtualMachine(obj.getString("pricing"), obj.getString("region"), obj.getInt("id"), obj.getString("os"), obj.getString("model"), obj.getDouble("upfront"), obj.getString("updated_at"), obj.getDouble("term"), obj.getString("created_at"), obj.getBoolean("latest"), obj.getDouble("hourly"), obj.getBoolean("ebsoptimized"));
            this.AmazonMachines.put("" + obj.getInt("id"), avm);
        }
    }

    private void createStorageInfo(JSONArray AmazonStorageArray) throws JSONException {
        for (int i = 0; i < AmazonStorageArray.length(); i++) {
            JSONObject obj = AmazonStorageArray.getJSONObject(i);
            AmazonStorage as = new AmazonStorage(obj.getInt("id"), obj.getString("region"), obj.getString("kind"), obj.getDouble("price"), obj.getString("unit"), obj.getString("created_at"), obj.getString("updated_at"));
            this.AmazonStorageService.put("" + obj.getInt("id"), as);
        }
    }

    private String pricingGET(String server, String address) {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            // Especifica o host, a porta e o protocolo
            HttpHost target = new HttpHost(server, 80, "http");
            // Especifica o get request
            HttpGet getRequest = new HttpGet(address);

            /*Important stuff...*/
            System.out.println("executing request to " + target);

            HttpResponse httpResponse = httpClient.execute(target, getRequest);
            HttpEntity entity = httpResponse.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(httpResponse.getStatusLine());
            Header[] headers = httpResponse.getAllHeaders();

            for (Header header : headers) {
                System.out.println(header);
            }

            System.out.println("----------------------------------------");
            /*End of important stuff*/

            /*Caso o resultado do request não seja nulo, ele é tratado*/
            if (entity != null) {
                JSONArray result = new JSONArray(EntityUtils.toString(entity));
                return (result.toString(4));
            } else {
                return (null);
            }
        } /*Catch exception, if problems occur with the request*/ catch (IOException | JSONException ex) {
            Logger.getLogger(AmazonTarifationGet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            try {
                httpClient.close();//Close the connection
            } catch (IOException ex) {
                Logger.getLogger(AmazonTarifationGet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return (null);
    }

    private void saveJsonArray(String array, String filename) {

        try {
            OutputStream os = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            try (BufferedWriter bw = new BufferedWriter(osw)) {
                bw.write(array);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AmazonTarifationGet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AmazonTarifationGet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JSONArray readJsonArray(String filename) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            String everything = sb.toString();

            if (everything != null) {

                return (new JSONArray(everything));
            } else {

                return (null);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AmazonTarifationGet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException | IOException ex) {
            Logger.getLogger(AmazonTarifationGet.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (null);
    }

    public double getInstancePrice(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getHourly();
    }

    public String getInstanceRegion(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getRegion();
    }

    public String getInstancePricingType(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getPricing();
    }

    public String getInstanceOs(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getOs();
    }

    public String getInstanceModel(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getModel();
    }

    public double getInstanceUpfront(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getUpfront();
    }

    public String getInstanceUpdatedAt(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getUpdated_at();
    }

    public double getInstanceTerm(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getTerm();
    }

    public String getInstanceCreatedAt(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).getCreated_at();
    }

    public boolean isInstanceLatest(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).isLatest();
    }

    public boolean isInstanceEbsoptimized(int id) {
        String Id = "" + id;
        return this.AmazonMachines.get(Id).isEbsoptimized();
    }

    public String getStorageRegion(int id) {
        String Id = "" + id;
        return this.AmazonStorageService.get(Id).getRegion();
    }

    public String getStorageCreatedAt(int id) {
        String Id = "" + id;
        return this.AmazonStorageService.get(Id).getCreatedAt();
    }

    public String getStorageKind(int id) {
        String Id = "" + id;
        return this.AmazonStorageService.get(Id).getKind();
    }

    public String getStoragePriceUnit(int id) {
        String Id = "" + id;
        return this.AmazonStorageService.get(Id).getPriceUnit();
    }

    public String getStorageUpdatedAt(int id) {
        String Id = "" + id;
        return this.AmazonStorageService.get(Id).getUpdatedAt();
    }

    public double getStoragePrice(int id) {
        String Id = "" + id;
        return this.AmazonStorageService.get(Id).getPrice();
    }

    public String getDataTransferRegion(int id) {
        String Id = "" + id;
        return this.AmazonDataTransferServices.get(Id).getRegion();
    }

    public String getDataTransferTier(int id) {
        String Id = "" + id;
        return this.AmazonDataTransferServices.get(Id).getTier();
    }

    public String getDataTransferUpdateAt(int id) {
        String Id = "" + id;
        return this.AmazonDataTransferServices.get(Id).getUpdatedAt();
    }

    public String getDataTransferCreatedAt(int id) {
        String Id = "" + id;
        return this.AmazonDataTransferServices.get(Id).getCreatedAt();
    }

    public String getDataTransferKind(int id) {
        String Id = "" + id;
        return this.AmazonDataTransferServices.get(Id).getKind();
    }

    public double getDataTransferPrice(int id) {
        String Id = "" + id;
        return this.AmazonDataTransferServices.get(Id).getPrice();
    }
}