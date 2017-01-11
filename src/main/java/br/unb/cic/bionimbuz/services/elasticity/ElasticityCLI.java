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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.elasticity;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author brenokx
 */
public class ElasticityCLI extends Ec2Commands {

    private static Scanner test;

    public static void main(String[] args) throws IOException, IllegalArgumentException {

        test = new Scanner(System.in);
        System.out.println("Input: ");
        String name = test.nextLine();

        if (name.equals("list")) {
            Ec2Commands.listinstances();
        } else if (name.equals("create")) {
            Ec2Commands.createinstance();
        } else if (name.equals("shutdown")) {
            Ec2Commands.shutdown();
        } else if (name.equals("stopinstance")) {
            Ec2Commands.stopinstance();
        } else if (name.equals("startinstance")) {
            Ec2Commands.startinstance();
        } else if (name.equals("terminate")) {
            Ec2Commands.terminate();
        } else if (name.equals("rebootinstance")) {
            Ec2Commands.rebootinstance();
        } else if (name.equals("createami")) {
            Ec2Commands.createami();

        } else {
            System.out.println("Something is wrong...");
        }

    }

}
