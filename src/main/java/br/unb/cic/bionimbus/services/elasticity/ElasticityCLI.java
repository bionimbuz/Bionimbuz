/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.unb.cic.bionimbus.services.elasticity;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author brenokx
 */
public class ElasticityCLI extends Ec2Commands {
  
        private static Scanner test;

	public static void main(String[] args) throws IOException,IllegalArgumentException {
	
        test = new Scanner(System.in);
        System.out.println("Input: ");
        String name = test.nextLine();

        if (name.equals("list")){
            Ec2Commands.listinstances();
        }else if(name.equals("create")){
			Ec2Commands.createinstance();
        }else if(name.equals("shutdown")){
			Ec2Commands.shutdown();
        }else if(name.equals("stopinstance")){
			Ec2Commands.stopinstance();
        }else if(name.equals("startinstance")){
			Ec2Commands.startinstance();
        }else if(name.equals("terminate")){
			Ec2Commands.terminate();
        }else if(name.equals("rebootinstance")){
			Ec2Commands.rebootinstance();
        }else if(name.equals("createami")){
			Ec2Commands.createami();

        }else{
            System.out.println("Something is wrong...");
        }

    }

}    

