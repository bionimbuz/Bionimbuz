/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * @param ip - Ip onde será enviado os pacotes para o calculo necessario
     * @return - Latencia média entre quem enviou os pacotes e o destino
     * @throws IOException
     */
    public static double nmap(String host) throws IOException {
        float sizerequest = 0;
        float temporesp = 0;
        int times = 0;
        boolean found = false;
        String teste;
        Matcher matcher;
        Runtime r = Runtime.getRuntime();
        Process p = r.exec("nmap " + host);
        BufferedReader in = new BufferedReader(new  InputStreamReader(p.getInputStream()));
        Pattern pattern = Pattern.compile("(?<=\\().*.(?=s latency)");
        if(in.ready()){
            while ((teste = in.readLine()) != null && times < 4) {
                if (times == 3) {
                    matcher = pattern.matcher(teste);
                    while (matcher.find()) {
                        System.out.println(""+matcher.group());
                        sizerequest = Float.parseFloat(matcher.group());
                        found = true;
                        p.destroy();
                        return sizerequest;
                    } 
                    if (!found) {
                        System.out.println("I didn't found the text");
                    }
                }
                times+=1;
            }
        }
        p.destroy();
        return Double.MAX_VALUE;
    }
//    public static void main(String[] args) {
//        double teste=0d;
//        int i =0;
//        try {
//            while(i<3){
//                teste+=Nmap.nmap("www.google.com");
//                i++;
//            }
//            System.out.println("I"+i);
//            System.out.println("Média:"+teste/i);
//        } catch (IOException ex) {
//            Logger.getLogger(Nmap.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
