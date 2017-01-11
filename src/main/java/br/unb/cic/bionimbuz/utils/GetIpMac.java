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

/**
 *
 * @author breno-linux
 */
public class GetIpMac {

    /**
     * @return the Mac
     * @throws java.io.IOException
     */
    public static String getMac() throws IOException {
        String[] cmd = {
            "/bin/sh",
            "-c",
            "ifconfig eth0 | grep -o -E '([[:xdigit:]]{1,2}:){5}[[:xdigit:]]{1,2}'"
        };

        Runtime r = Runtime.getRuntime();
        Process p = r.exec(cmd);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        if (in.readLine() != null) {
            return in.readLine();
        } else {
            String[] teste = {"/bin/sh",
                "-c",
                "ifconfig wlan0 | grep -o -E '([[:xdigit:]]{1,2}:){5}[[:xdigit:]]{1,2}'"};
            p = r.exec(teste);
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));

            return in.readLine();
        }

    }

    /**
     * @return the Ip
     * @throws java.io.IOException
     */
    public static String getIp() throws IOException {
        String[] cmd = {
            "/bin/sh",
            "-c",
            "ifconfig eth0 | grep 'inet end.:' | cut -d: -f2 | awk '{ print $1}'"
        };

        Runtime r = Runtime.getRuntime();
        Process p = r.exec(cmd);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        if (in.readLine() != null) {
            return in.readLine();
        } else {

            String[] teste = {"/bin/sh",
                "-c",
                "ifconfig wlan0 | grep 'inet end.:' | cut -d: -f2 | awk '{ print $1}'"};
            p = r.exec(teste);
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return in.readLine();
        }
        //return ip;
    }

}
