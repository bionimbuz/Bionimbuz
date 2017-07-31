/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.storage;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe para calculo de latencia
 *
 * @author deric
 */
public class Ping {

    /**
     * Método para realizar um ping no ip de destino para que seja calculada a
     * latência.
     *
     * @param ip - Ip onde será enviado os pacotes para o calculo necessario
     * @return - Latencia média entre quem enviou os pacotes e o destino
     * @throws IOException
     */
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
        try {
            /*
             * Para uma latência mais exata preferimos pingar 3 pacotes no IP e pegar a média.
             */
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(Ping.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (in.ready()) {
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
        } else {
            p.destroy();
            return Double.MAX_VALUE;
        }
        p.destroy();
        double avglatency = avg / (times - 1);

        return avglatency;
    }
}
