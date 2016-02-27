/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.tests;

import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.model.Job;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.security.AESEncryptor;
import static com.sun.corba.se.impl.util.Utility.printStackTrace;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author willian
 */
public class FromMockFileTestGenerator extends FromLogFileTestGenerator {

    private int numPipelines;

    public FromMockFileTestGenerator(int numPipelines) {
        this.numPipelines = numPipelines;
        String pathHome = System.getProperty("user.dir");
        String path = (pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/");
        AESEncryptor aes = new AESEncryptor();
        try {
            //TO-DO: Remove comment after William Final Commit
            //aes.decrypt(path+"resSample.txt");
        } catch (Exception ex) {
            Logger.getLogger(FromMockFileTestGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.resFile = path + "resSample.txt";
    }

    @Override
    protected void generatePipelineTemplates() {
        Job taskList[] = null;

        // get pipeline file path
        String pathHome = System.getProperty("user.dir");
        String path = (pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/");
        AESEncryptor aes = new AESEncryptor();
        try {
            //TO-DO: Remove comment after Willian Final Commit
            //aes.decrypt(path+"pipelineSample.txt");
        } catch (Exception ex) {
            Logger.getLogger(FromMockFileTestGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int ii = 1; ii <= numPipelines; ii++) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(path + "pipelineSample" + ii + ".txt"));

                // get first line: number of tasks
                String line = br.readLine();
                int tasksNumber = Integer.parseInt(line);
                taskList = new Job[tasksNumber];

                // get next tasksNumber lines: each task
                for (int i = 0; i < tasksNumber; i++) {
                    // generate a new jobInfo from json
                    line = br.readLine();
                    Job jobInfo = new Job();
                    jobInfo.setId("i" + i);
                    jobInfo.setTimestamp(0l);

                    // set serviceId from json
                    int lastComa = line.indexOf(",");
                    jobInfo.setServiceId(line.substring(line.indexOf("serviceId:") + 10, lastComa));

                    // set args from json
                    lastComa = line.indexOf(",", lastComa + 1);
                    jobInfo.setArgs(line.substring(line.indexOf("args:") + 5, lastComa));

                    // get input list from json
                    int lastBracket = line.indexOf("]");
                    String io = line.substring(line.indexOf("inputs:[") + 8, lastBracket);
                    String inputs[] = io.split(",");

                    // set inputs
                    // TODO: change addInput to receive the filename instead of its zookeeper id
                    for (String inp : inputs) {
                        FileInfo f = new FileInfo(inp);
                        f.setName("inp0");
                        f.setUploadTimestamp("00/00/00");
                        f.setHash("hash");
                        f.setPayload(new byte[10]);

                        jobInfo.addInput(f);
                    }

                    // get output list from json
                    lastBracket = line.indexOf("]", lastBracket + 1);
                    io = line.substring(line.indexOf("outputs:[") + 9, lastBracket);
                    String outputs[] = io.split(",");

                    // set outputs
                    for (String out : outputs) {
                        jobInfo.addOutput(out);
                    }

                    // put it into the map to, furthermore, set the dependencies
                    taskList[i] = jobInfo;
                }

                // get the remaining lines: dependency matrix
                for (int i = 0; i < tasksNumber; i++) {
                    String deps[] = br.readLine().split(",");
                    for (int j = 0; j < tasksNumber; j++) {
                        if (Integer.parseInt(deps[j]) == 1) {
                            taskList[i].addDependency(taskList[j].getId());
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.getMessage();
                printStackTrace();
            }

            // push taskList to the pipelineTemplates
            Workflow p = new Workflow(Arrays.asList(taskList));
            System.out.println("[TestGen] taskList " + taskList.length);
            System.out.println("[TestGen] pipeline " + p.getJobs().size());
            pipelinesTemplates.add(p);
        }
    }

    @Override
    protected void generateServicesTemplates() {
        Job taskList[] = null;
        // get service file path
        String pathHome = System.getProperty("user.dir");
        String path = (pathHome.substring(pathHome.length()).equals("/") ? pathHome + "data-folder/" : pathHome + "/data-folder/");
        AESEncryptor aes = new AESEncryptor();
        try {
            //TO-DO: Remove comment after William Final Commit
            //aes.decrypt(path+"servicesSample.txt");
        } catch (Exception ex) {
            Logger.getLogger(FromMockFileTestGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(path + "servicesSample.txt"));

            String line;
            while ((line = br.readLine()) != null) {
                PluginService service = new PluginService();

                // set serviceId from json
                int lastComa = line.indexOf(",");
                service.setId(line.substring(line.indexOf("serviceId:") + 10, lastComa));

                // set args from json
                lastComa = line.indexOf(",", lastComa + 1);
                service.setPath(line.substring(line.indexOf("path:") + 5, lastComa));

                // set mode
                service.setPresetMode(Double.parseDouble(line.substring(line.indexOf("mode:") + 5, line.length() - 1)));

                // add service
                servicesTemplates.add(service);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            printStackTrace();
        }
    }

}
