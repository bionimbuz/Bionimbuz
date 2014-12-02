/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

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
       if (in.readLine()!=null){
           return in.readLine();
       }
       else {
          String[] teste = {"/bin/sh",
                            "-c", 
                            "ifconfig wlan0 | grep -o -E '([[:xdigit:]]{1,2}:){5}[[:xdigit:]]{1,2}'"};
          p= r.exec(teste);
          in = new BufferedReader(new InputStreamReader(p.getInputStream()));
          
          return in.readLine();
       }
       
    }

    /**
     * @return the Ip
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
            if(in.readLine()!= null){
                return in.readLine();
            }
            else{

               String[] teste = {"/bin/sh",
                                 "-c", 
                                 "ifconfig wlan0 | grep 'inet end.:' | cut -d: -f2 | awk '{ print $1}'"};
               p= r.exec(teste);
               in = new BufferedReader(new InputStreamReader(p.getInputStream()));
               return in.readLine();
            }
            //return ip;
    }
    
    
}
