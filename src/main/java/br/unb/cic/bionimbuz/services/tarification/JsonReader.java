package br.unb.cic.bionimbuz.services.tarification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonReader {

//    public JsonReader(String json){
//        super(json);
//    }
//    
//    @Override
//    public JSONObject putOnce(String key, Object value) throws JSONException {
//            Object storedValue;
//            String keyaux;
//            if (key != null && value != null) {
//                storedValue=value;
//                if ((storedValue = this.opt(key)) != null ) {
//                    if(!storedValue.equals(value))                          //Only through Exception for different values with same key
//                        throw new JSONException("Duplicate key \"" + key + "\"");
//                    else
//                        return this;
//                }
//                this.put(key, value);
//            }
//            return this;
//    }
//    private static String readAll(Reader rd) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        int cp;
//        while ((cp = rd.read()) != -1) {
//            sb.append((char) cp);
//        }
//        return sb.toString();
//    }

    /**
     * Method that read jsonobject from url
     * @param urlString
     * @return
     * @throws IOException
     * @throws JSONException 
     */
//        URL url = new URL(urlString);
//        URLConnection uc;
//        uc = url.openConnection();
//        uc.addRequestProperty("User-Agent",
//                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
//        uc.connect();
//        try (InputStream is = uc.getInputStream()) {
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//            String jsonText = readAll(rd);
//            
//            JSONObject json = new JSONObject(jsonText);
//            return json;
//        }
    public static JSONObject readJsonFromUrl(String urlString) throws IOException, JSONException  {
        JSONObject jo = new JSONObject();
        
        try(final InputStream openedStream = new URL(urlString).openStream();) {
            jo = (JSONObject) new JSONTokener(openedStream).nextValue();
        } catch (JSONException | ClassCastException | MalformedURLException |UnknownHostException ex) {
            String ent = IOUtils.toString(new URL(urlString).openStream());
            ArrayList<String> j = new ArrayList(Arrays.asList(ent.split("},")));
            ArrayList<String> j2 = new ArrayList();
            Set<String> hs = new LinkedHashSet<>();
            int aux = 1;
            for (String j1 : j) {
                if (aux++ != j.size()) {
                    j1 = j1.concat("}");
                }
                j2.add(j1);
            }
            hs.addAll(j2);
            j2.clear();
            j.clear();
            String computeEngineAux = hs.toString();
            computeEngineAux = computeEngineAux.substring(1, computeEngineAux.length() - 1);
            computeEngineAux = computeEngineAux.replaceAll("}, ", "},");
            jo = (JSONObject) new JSONTokener(computeEngineAux).nextValue();
            
//            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jo;
    }

    /*
     This method is used to save jsons from services on files.
     */
    public static void saveJson(String array, String filename) {

        try {
            OutputStream os = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            try (BufferedWriter bw = new BufferedWriter(osw)) {
                bw.write(array);
            } catch (IOException ex) {
                Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     This method is used to get the json data from the files.
     */
//    public static JSONObject readJson(String filename) {
//        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
//            StringBuilder sb = new StringBuilder();
//            String line = br.readLine();
//
//            while (line != null) {
//                sb.append(line);
//                sb.append(System.lineSeparator());
//                line = br.readLine();
//            }
//
//            String everything = sb.toString();
//
//            if (everything != null) {
//
//                return (new JSONObject(everything));
//            } else {
//
//                return (null);
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return (null);
//    }
}
