/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.storage;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;
import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.PeerNode;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
/**
 *
 * @author deric
 */
public class Ping {

   // private String pingCmd;
     // public static void main(String[] args) throws IOException{     
        
    public static float calculo (String pingCmd) throws IOException{
        
            //String pingCmd = "192.168.1.146";
            
            float avg=0;
            float taxadetransferencia=0;
            float sizerequest=0;
            float temporesp=0;
            int times=0; 
            boolean found = false;
            String teste;   
            Matcher matcher;
            Runtime r = Runtime.getRuntime();
            Process p = r.exec("ping " + pingCmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            Pattern pattern = Pattern.compile("(?<=time=).*.(?= )");
            Pattern patternBand = Pattern.compile("(?<=\\) ).*.(?=\\()");
           // Pattern patternBand = Pattern.compile("(?<= ).*.(?=\\()");

            while ((teste = in.readLine()) != null && times <4) {
                System.out.println("1: "+teste);
                
                if(times==0){
                    matcher= patternBand.matcher(teste);
                    while (matcher.find()) {
                        sizerequest=Float.parseFloat(matcher.group().toString());
                        System.out.println("tamanho da mensagem de requisição "+sizerequest);
                        found = true;
                    }
                    if (!found) {
                        System.out.println("I didn't found the text");
                    }
                }
                else{
                    matcher= pattern.matcher(teste);
                    while (matcher.find()) {
                        System.out.println("2: Tempo de resposta: " + matcher.group().toString());
                        temporesp=Float.parseFloat(matcher.group().toString());
                        taxadetransferencia=sizerequest/((temporesp/1000));
                        avg+=Float.parseFloat(matcher.group().toString());
                        System.out.println("Taxa de tranferência: "+taxadetransferencia+" Somatorio tempo de resposta: "+avg);
                        found = true;
                    }
                    if (!found) {
                        System.out.println("I didn't found the text");
                    }
                }
                times +=1;
        //        pingResult += teste;
            }
            float avglatency = avg/(times-1);
            System.out.println(" Latencia média : " +avglatency);
           
            return avglatency;
       /* }
        catch(IOException e){
            System.out.println(abc);
        }*/
    }
}


