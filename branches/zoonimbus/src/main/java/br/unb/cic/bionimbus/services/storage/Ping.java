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

    // private String pingCmd;
    // public static void main(String[] args) throws IOException{

    public static long calculo(String pingCmd) throws IOException {

        //String pingCmd = "192.168.1.146";

        long avg = 0;
        float taxadetransferencia = 0;
        float sizerequest = 0;
        float temporesp = 0;
        int times = 0;
        boolean found = false;
        String teste;
        Matcher matcher;
        Runtime r = Runtime.getRuntime();
        Process p = r.exec("ping " + pingCmd);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        Pattern pattern = Pattern.compile("(?<=time=).*.(?= )");
        Pattern patternBand = Pattern.compile("(?<=\\) ).*.(?=\\()");

        while ((teste = in.readLine()) != null && times < 4) {
            System.out.println("1: " + teste);

            if (times == 0) {
                matcher = patternBand.matcher(teste);
                while (matcher.find()) {
                    sizerequest = Float.parseFloat(matcher.group());
                    System.out.println("tamanho da mensagem de requisição " + sizerequest);
                    found = true;
                }
                if (!found) {
                    System.out.println("I didn't found the text");
                }
            } else {
                matcher = pattern.matcher(teste);
                while (matcher.find()) {
                    System.out.println("2: Tempo de resposta: " + matcher.group());
                    temporesp = Float.parseFloat(matcher.group());
                    taxadetransferencia = sizerequest / ((temporesp / 1000));
                    avg += Float.parseFloat(matcher.group());
                    System.out.println("Taxa de tranferência: " + taxadetransferencia + " Somatorio tempo de resposta: " + avg);
                    found = true;
                }
                if (!found) {
                    System.out.println("I didn't found the text");
                }
            }
            times += 1;
            //        pingResult += teste;
        }
        long avglatency = avg / (times - 1);

        return avglatency;
       /* }
        catch(IOException e){
            System.out.println(abc);
        }*/
    }
}


