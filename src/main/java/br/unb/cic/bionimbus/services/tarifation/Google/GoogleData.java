/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Google;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author gabriel
 */
public class GoogleData {

    public JSONObject GoogleJsonService;
//    private final Map<String, String> config;
    private Map<String, String> GoogleService;
    
    public GoogleData(BioNimbusConfig config) {
//        this.config = new HashMap<>();
//        this.config.put("Filename", "GooglePrices.txt");
        GoogleJsonService = readJSONObject(config.getRootFolder()+"/conf/GooglePrices.txt");
        //this.GoogleJsonService = GoogleJsonService;
        this.createInfo(GoogleJsonService);
    }

    private JSONObject readJSONObject(String filename) {

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

                return (new JSONObject(everything));
            } else {

                return (null);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GoogleData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException | IOException ex) {
            Logger.getLogger(GoogleData.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (null);
    }

    private void createInfo(JSONObject GoogleJsonService) {
        JSONObject Data = GoogleJsonService.getJSONObject("gcp_price_list");
        Iterator<String> it = Data.keys();
        while (it.hasNext()) {
            Object obj = Data.get(it.next());
            String objClass = obj.getClass().toString();
            System.out.println(objClass);
        }
    }

    public JSONObject getGoogleJsonService() {
        return GoogleJsonService;
    }

    public void setGoogleService(Map<String, String> GoogleService) {
        this.GoogleService = GoogleService;
    }

}
