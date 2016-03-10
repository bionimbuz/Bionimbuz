/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.services.tarifation.Google.GoogleDataGet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class GoogleDataGetTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDataGetTest.class);
    public static void main(String[] args){
        BioNimbusConfig configuration = null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            configuration = mapper.readValue(new File(System.getProperty("config.file", "conf/node.yaml")), BioNimbusConfig.class);
        } catch (IOException ex) {
            LOGGER.info("[IOException] - " + ex.getMessage());
        }
        GoogleDataGet gdg = new GoogleDataGet(configuration);
    }
}
