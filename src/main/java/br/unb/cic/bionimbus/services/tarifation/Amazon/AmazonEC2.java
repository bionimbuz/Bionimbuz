/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation.Amazon;

import br.unb.cic.bionimbus.services.tarifation.JsonReader;
import java.io.IOException;
import org.json.JSONObject;

/**
 *
 * @author fritz
 */
public class AmazonEC2 {
    String location;
    String instanceType;
    String vcpu;
    String processor;
    String clockSpeed;
    String memory;
    String networkPerformance;
    String processorArchitecture;
    String operatingSystem;
    
    public AmazonEC2(String url, String instanceType) throws IOException {
        JSONObject amazonEC2Data = JsonReader.readJsonFromUrl(url);
    }
 
}
