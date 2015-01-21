/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.services.elasticity.Ec2Commands;
import java.io.IOException;
import java.util.Scanner;
/**
 *
 * @author brenokx
 */
public class CreateInstance implements Command{
    public static final String NAME = "createinstance";
    private final SimpleShell shell;
     private static Scanner test;
    public CreateInstance (SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {
	

        Ec2Commands.createinstance();

        return "funciona!";
    }

    @Override
    public String usage() {
        return NAME; 
    }

    @Override
    public String getName() {
        return NAME; 
    }

    @Override
    public void setOriginalParamLine(String param) {
      
    }
    
}