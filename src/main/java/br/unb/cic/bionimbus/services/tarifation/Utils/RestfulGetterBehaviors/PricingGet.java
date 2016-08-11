package br.unb.cic.bionimbus.services.tarifation.Utils.RestfulGetterBehaviors;

import br.unb.cic.bionimbus.services.tarifation.Utils.RestfulGetter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class PricingGet implements RestfulGetter {

    public PricingGet() {
    }

    @Override
    public String get(String server, String address) {

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
                return EntityUtils.toString(entity);
            } else {
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(PricingGet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                httpClient.close();
            } catch (Exception ex) {
                Logger.getLogger(PricingGet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /**
     *
     * @param array
     * @param filename
     */
    @Override
    public void saveGet(String array, String filename) {

        try {
            OutputStream os = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            try (BufferedWriter bw = new BufferedWriter(osw)) {
                bw.write(array);
            } catch (IOException ex) {
                Logger.getLogger(PricingGet.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PricingGet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
