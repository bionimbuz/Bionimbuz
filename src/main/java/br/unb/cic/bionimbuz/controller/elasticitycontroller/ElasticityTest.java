/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package br.unb.cic.bionimbuz.controller.elasticitycontroller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class ElasticityTest {

      private static Scanner test;

    public static void main(String[] args) throws IOException{

        AmazonAPI api = new AmazonAPI();
        test = new Scanner(System.in);
        System.out.println("Input: ");
        String name = test.nextLine();
        if (name.equals("create")) {
            long timeIp = System.currentTimeMillis();
            api.createinstance("t2.micro", "bionimbuz");
            String ip = api.getIpInstance();
            if(ip== null){
                System.out.println("erradoo" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(timeIp-System.currentTimeMillis())));
                
            }else{
                System.out.println("certo ip:"+ip+" - " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(timeIp-System.currentTimeMillis())));
            }
            
            
        } else if (name.equals("terminate")) {
            api.terminate("i-037d33975539331ad");
        } else {
            System.out.println("Something is wrong...");
        }

    }
}
