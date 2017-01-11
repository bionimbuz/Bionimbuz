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

package br.unb.cic.bionimbuz.services.elasticity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class Ec2Commands {

    public static AmazonEC2 EC2;
    public static Scanner user_input = new Scanner(System.in);
    public static String instanceid;
    public static KeyPair keyPair;
    public static int count = 1;

    public static void setup() throws IOException, IllegalArgumentException {
        PropertiesCredentials credentials = new PropertiesCredentials(Ec2Commands.class.getResourceAsStream("/AwsCredentials.properties"));
        EC2 = new AmazonEC2Client(credentials);
        EC2.setEndpoint("ec2.us-west-2.amazonaws.com");
    }

    public static void shutdown() throws IOException {
        Ec2Commands.setup();
        EC2.shutdown();
    }

    public static void listinstances() throws IOException {
        Ec2Commands.setup();

        System.out.println("Descrevendo instancias BioNimbuZ na Amazon");
        DescribeInstancesResult describeInstancesRequest = EC2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> instances = new HashSet<Instance>();
        // add all instances to a Set.
        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }

        System.out.println("You have " + instances.size() + " Amazon EC2 instance(s).");
        for (Instance ins : instances) {

            // instance id
            String instanceId = ins.getInstanceId();

            // instance state
            InstanceState is = ins.getState();
            System.out.println(instanceId + " " + is.getName());
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Restarting the application");
        System.out.println();
//	Ec2Commands.enteroption();		

    }

    public static void createinstance() throws IOException {
        Ec2Commands.setup();

        try {

            System.out.println("Criando nova maquina BioninbuZ");
            String imageId;
            System.out.println("Enter the image AMI id (eg: ami-687b4f2d)");
            imageId = user_input.next();

            int minInstanceCount = 1; // 
            int maxInstanceCount = 1;
            RunInstancesRequest rir = new RunInstancesRequest(imageId, minInstanceCount, maxInstanceCount);
            rir.setInstanceType("t1.micro");

            try(Scanner keyscan = new Scanner(System.in)) {
	            System.out.println("Do you want to use an existing keypair or do you want to create a new one?");
	            System.out.println("#1 Use existing keypair");
	            System.out.println("#2 Create a new keypair");
	            int keypairoption;
	            String key; //existing keypair name
	            keypairoption = keyscan.nextInt();
	            if (keypairoption == 1) {
	                System.out.println("Enter the existing keypair name to use with the new instance");
	                key = keyscan.next();
	                rir.withKeyName(key);
	            } else if (keypairoption == 2) {
	                //count++;
	                System.out.println("Enter the keypair name to create");
	                String newkeyname;
	                newkeyname = keyscan.next();
	                CreateKeyPairRequest newKeyRequest = new CreateKeyPairRequest();
	                newKeyRequest.setKeyName(newkeyname);
	                CreateKeyPairResult keyresult = EC2.createKeyPair(newKeyRequest);
	
	                keyPair = keyresult.getKeyPair();
	                System.out.println("The key we created is = "
	                        + keyPair.getKeyName() + "\nIts fingerprint is="
	                        + keyPair.getKeyFingerprint() + "\nIts material is= \n"
	                        + keyPair.getKeyMaterial());
	
	                System.out.println("Enter the directory to store .pem file (eg: Windows C:\\Users\\user\\Desktop\\, Linux /user/home)");
	                String dir;
	                dir = keyscan.next();
	                String fileName = dir + newkeyname + ".pem";
	                File distFile = new File(fileName);
	                BufferedReader bufferedReader = new BufferedReader(new StringReader(keyPair.getKeyMaterial()));
	                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(distFile));
	                char buf[] = new char[1024];
	                int len;
	                while ((len = bufferedReader.read(buf)) != -1) {
	                    bufferedWriter.write(buf, 0, len);
	                }
	                bufferedWriter.flush();
	                bufferedReader.close();
	                bufferedWriter.close();
	
	            }
            }

            rir.withSecurityGroups("default");

            RunInstancesResult result = EC2.runInstances(rir);

            System.out.println("waiting");
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("OK");

            List<Instance> resultInstance = result.getReservation().getInstances();

            String createdInstanceId = null;
            for (Instance ins : resultInstance) {

                createdInstanceId = ins.getInstanceId();
                System.out.println("New instance has been created: " + ins.getInstanceId());//print the instance ID

            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println();
            System.out.println("Restarting the application");
            System.out.println();
//    Ec2Commands.enteroption();	
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
            System.out.println("Give a valid input");
            System.out.println("");
//		Ec2Commands.enteroption();
        }

    }

    public static void stopinstance() throws IOException {
        Ec2Commands.setup();
        try {

            System.out.println("#4 Stop the Instance");
            System.out.println("Enter the instance id");
            instanceid = user_input.next();

            //stop instance
            StopInstancesRequest sireq = new StopInstancesRequest().withInstanceIds(instanceid);
            StopInstancesResult sires = EC2.stopInstances(sireq);
            System.out.println("Stopping instance " + instanceid);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println();
            System.out.println("Restarting the application");
            System.out.println();
//        Ec2Commands.enteroption();	
        } catch (Exception e) {
            System.out.println("Give a valid input");
            System.out.println("");
//			Ec2Commands.enteroption();
        }

    }

    public static void startinstance() throws IOException {
        Ec2Commands.setup();
        try {

            System.out.println("#3 Start the Instance");
            System.out.println("Enter the instance id");
            instanceid = user_input.next();

            //start instance
            List<String> instancesToStart = new ArrayList<String>();
            instancesToStart.add(instanceid);
            StartInstancesRequest startr = new StartInstancesRequest();
            startr.setInstanceIds(instancesToStart);
            EC2.startInstances(startr);
            System.out.println("Starting instance " + instanceid);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println();
            System.out.println("Restarting the application");
            System.out.println();
//        Ec2Commands.enteroption();
        } catch (Exception e) {
            System.out.println("Give a valid input");
            System.out.println("");
//			Ec2Commands.enteroption();
        }

    }

    public static void terminate() throws IOException {
        Ec2Commands.setup();
        try {

            System.out.println("#6 Terminate the Instance");
            System.out.println("Enter the instance id to terminate");
            instanceid = user_input.next();

            List<String> instancesToTerminate = new ArrayList<String>();
            instancesToTerminate.add(instanceid);
            TerminateInstancesRequest tir = new TerminateInstancesRequest(instancesToTerminate);
            EC2.terminateInstances(tir);
            System.out.println("Terminating the instance : " + instanceid);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println();
            System.out.println("Restarting the application");
            System.out.println();
//        Ec2Commands.enteroption();
        } catch (Exception e) {
            System.out.println("Give a valid input");
            System.out.println("");
//			Ec2Commands.enteroption();
        }

    }

    public static void rebootinstance() throws AmazonClientException, IOException {
        Ec2Commands.setup();
        try {

            System.out.println("#5 Reboot instance");
            System.out.println("Enter the instance id to reboot");
            instanceid = user_input.next();
            List<String> instancesToReboot = new ArrayList<String>();
            instancesToReboot.add(instanceid);

            RebootInstancesRequest rir = new RebootInstancesRequest().withInstanceIds(instancesToReboot);
            EC2.rebootInstances(rir);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println();
            System.out.println("Rebooted the instance " + instanceid);
            System.out.println("Restarting the application");
            System.out.println();
//	        Ec2Commands.enteroption();
        } catch (Exception e) {
            System.out.println("Give a valid input");
            System.out.println("");
//			Ec2Commands.enteroption();
        }
    }

    public static void createami() throws IOException {
        Ec2Commands.setup();
        try {

            System.out.println("#8 Create AMI");
            System.out.println("Enter the instance id to create AMI");
            instanceid = user_input.next();

            CreateImageRequest cir = new CreateImageRequest(instanceid, instanceid);
            CreateImageResult cires = EC2.createImage(cir);
            String imageid;
            imageid = cires.getImageId();
            System.out.println("The imageid of the newly created AMI is " + imageid);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println();
            System.out.println("Restarting the application");
            System.out.println();
//        Ec2Commands.enteroption();
        } catch (Exception e) {
            System.out.println("Give a valid input");
            System.out.println("");
//			Ec2Commands.enteroption();
        }

    }

} //main end