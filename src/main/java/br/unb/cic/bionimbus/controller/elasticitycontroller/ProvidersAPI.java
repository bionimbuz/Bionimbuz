package br.unb.cic.bionimbus.controller.elasticitycontroller;

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
