package br.unb.cic.bionimbus.toSort;

/**
 *
 * @author gabriel
 */


/*Bibliotecas relacionadas às Exceptions e à manipulação de arquivos*/
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*Bibliotecas relacionadas ao GET (Request dos preços)*/
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/*Bibliotecas relacionadas à manipulação de JSON's*/
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*Classe relacionada à atualização e obtenção de preços*/
public class AmazonPricingGet {
    
    /*pricing Array: ou apresenta a lista de preços, ou é null*/
    private JSONArray pricingArray=null;
   
    //Server: "info.awsstream.com"
    //Address: "/instances.json?"
    /*Construtor utilizado no caso de atualização: Nessa situação, um GET é realizado para obtenção dos preços da AWS*/
    public AmazonPricingGet (String Server, String address, String filename){
        
        try{
            /*Obtenção dos preços usando pricingGET*/
            System.out.println("Getting prices...");
            String arrayString = this.pricingGET(Server, address);
            System.out.println("Completed.");
            
            /*Caso não haja problemas na comunicação, arrayString é diferente de NULL*/
            if(arrayString!=null){
                
                /*Realização da persistência dos preços, em TXT (por enquanto)*/
                System.out.println("Saving prices..");
                this.saveJsonArray(arrayString,filename);
                /*atribui a lista de preços à pricingArray*/
                this.pricingArray = new JSONArray(arrayString);
                System.out.println("Saved.");
            }
            else{
                this.pricingArray = readJsonArray(filename);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        finally{
            System.out.println("GET process completed.");
        }
    }
    
/*  
    //Nesse Construtor, não é realizada a atualização dos preços, mas, sim, lê de um arquivo, já gerado anteriormente, a lista de preços
    public AmazonPricingGet (String filename){
        //atribui a lista de preços a pricingArray
        this.pricingArray= this.readJsonArray(filename);
    }
    
*/
    
    /*GET: pricingArray*/
    public JSONArray pricingArrayGET(){
        return(this.pricingArray);
    }
    
    public void atualizePrices(String server, String address) {
        
        try{
            /*Obtenção dos preços usando pricingGET*/
            System.out.println("Getting prices...");
            String arrayString = this.pricingGET(server, address);
            System.out.println("Completed.");
            
            /*Caso não haja problemas na comunicação, arrayString é diferente de NULL*/
            if(arrayString!=null){
                
                /*Realização da persistência dos preços, em TXT (por enquanto)*/
                System.out.println("Saving prices..");
                this.saveJsonArray(arrayString,"instancesOD1.txt");
                /*atribui a lista de preços à pricingArray*/
                this.pricingArray = new JSONArray(arrayString);
                System.out.println("Saved.");
            }
        } catch (JSONException ex) {
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        finally{
            System.out.println("GET process completed.");
        }
    }
    
    public double getPrice(int id){
        return(getPriceObj(searchJsonObject(id)));
    }
    
    public double getPriceObj(JSONObject machine){
        
        return(machine.getDouble("hourly"));
    }
    
    /*O objetivo desse método é obter informações de uma máquiva específica, a partir de seu ID*/
    private JSONObject searchJsonObject(int id) {
        
        int i=0;
        JSONObject JSONObj;
        JSONObj = null;
        for (i = 0; i < this.pricingArray.length(); i++) {
            int ID=0;
            try {
                JSONObj = this.pricingArray.getJSONObject(i);
                ID = JSONObj.getInt("id");
            } catch (JSONException ex) {
                Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (ID == id) {
                break;
            }
        }
        if(i==this.pricingArray.length()){
            /*Caso ID não seja encontrado, retorna-se null*/
            return(null);
        }
        else{
            return(JSONObj);
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
            }
            else{
                return(null);
            }
        }
        /*Catch exception, if problems occur with the request*/
        catch (IOException ex) {
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (JSONException ex) {
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        finally {

            try {
                httpClient.close();//Close the connection
            } 
            catch (IOException ex) {
                Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return (null);
    }
    
    private void saveJsonArray(String array, String filename){
        
        try {
            OutputStream os = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            
            bw.write(array);
            bw.close();
            
        }
        
        catch (FileNotFoundException ex) {
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (IOException ex) {
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private JSONArray readJsonArray(String filename){
        
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
            
            if(everything!=null){
                
                return(new JSONArray(everything));
            }
            else{
                
                return(null);
            }
        }
        
        catch (FileNotFoundException ex) {
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (JSONException ex) {
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException ex){
            Logger.getLogger(AmazonPricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return(null);
    }
}
