/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Utils;

import br.unb.cic.bionimbus.services.tarifation.Amazon.AmazonTarifationGet;
import java.io.IOException;
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

/**
 *
 * @author Gabriel Fritz Sluzala
 */

/*
    Implementação incompleta
*/
public class PricingGet {
    
    public static String pricingGet(String server, String address) {

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
    
}
