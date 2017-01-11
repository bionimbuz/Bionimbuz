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
package br.unb.cic.bionimbuz.utils;

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
