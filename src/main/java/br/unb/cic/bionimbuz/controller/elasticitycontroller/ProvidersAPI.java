package br.unb.cic.bionimbuz.controller.elasticitycontroller;

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
    public void createinstance(String type, String nameinstance) throws IOException;
    public void terminate(String instanceid) ;
   
   
  
}
