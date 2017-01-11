package br.unb.cic.bionimbuz.controller.elasticitycontroller;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;

/**
 *
 * @author guilherme
 */
public interface ProvidersAPI {
    
    /**
     *
     * @param type
     * @throws IOException
     */
    
    public void setup();
    public void createinstance(String type) throws IOException;
   
   
  
}
