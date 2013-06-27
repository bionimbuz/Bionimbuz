/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.storage;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author deric
 */
public class Ping {

    public static double calculo(String ip) throws IOException {

        double avg = 0;
        
        float sizerequest = 0;
        float temporesp = 0;
        int times = 0;
        boolean found = false;
        String teste;
        Matcher matcher;
        Runtime r = Runtime.getRuntime();
        Process p = r.exec("ping " + ip);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        Pattern pattern = Pattern.compile("(?<=time=).*.(?= )");
        Pattern patternBand = Pattern.compile("(?<=\\) ).*.(?=\\()");

        while ((teste = in.readLine()) != null && times < 4) {
            if (times == 0) {
                matcher = patternBand.matcher(teste);
                while (matcher.find()) {
                    sizerequest = Float.parseFloat(matcher.group());
                    found = true;
                }
                if (!found) {
                    System.out.println("I didn't found the text");
                }
            } else {
                float taxadetransferencia = 0;
                matcher = pattern.matcher(teste);
                while (matcher.find()) {
                    temporesp = Float.parseFloat(matcher.group());
                    taxadetransferencia = sizerequest / ((temporesp / 1000));
                    avg += Float.parseFloat(matcher.group());
                    found = true;
                }
                if (!found) {
                    System.out.println("I didn't found the text");
                }
            }
            times += 1;
        }
        double avglatency = avg / (times - 1);

        return avglatency;
    }
}


