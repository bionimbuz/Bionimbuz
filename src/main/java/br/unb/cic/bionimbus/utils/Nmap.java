/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author zoonimbus
 */
public class Nmap {

    /**
     * Método para realizar um ping no ip de destino para que seja calculada a
     * latência.
     *
     * @param host
     * @return - Latencia média entre quem enviou os pacotes e o destino
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public static double nmap(String host) throws IOException, InterruptedException {
        float sizerequest;
        float temporesp = 0;
        int times = 0;
        boolean found = false;
        String teste;
        Matcher matcher;
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(new String[]{"nmap", host});
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        Pattern pattern = Pattern.compile("(?<=\\().*.(?=s latency)");

        TimeUnit.MILLISECONDS.sleep(500);
        if (in.ready()) {
            while ((teste = in.readLine()) != null && times < 4) {
                if (times == 3) {
                    matcher = pattern.matcher(teste);
                    while (matcher.find()) {
                        sizerequest = Float.parseFloat(matcher.group());
                        found = true;
                        p.destroy();
                        return sizerequest;
                    }
                    if (!found) {
                        System.out.println("I didn't found the text");
                    }
                }
                times += 1;
            }
        }
        p.destroy();
        return Double.MAX_VALUE;
    }
}
