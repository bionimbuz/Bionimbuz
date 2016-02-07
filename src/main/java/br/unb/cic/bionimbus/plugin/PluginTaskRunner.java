package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import static br.unb.cic.bionimbus.config.BioNimbusConfigLoader.loadHostConfig;
import br.unb.cic.bionimbus.config.ConfigurationRepository;
import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.model.Log;
import br.unb.cic.bionimbus.model.LogSeverity;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.model.WorkflowStatus;
import br.unb.cic.bionimbus.persistence.dao.WorkflowDao;
import br.unb.cic.bionimbus.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbus.security.Hash;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginTaskRunner implements Callable<PluginTask> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginTaskRunner.class);

    private static final String BOWTIE_ID = "1";

    private final PluginTask task;
    private final PluginService service;
    private final String path;
    private final CloudMessageService cms;
    private final String PATHFILES = "data-folder/";
    private RpcClient rpcClient;

    private final WorkflowLoggerDao workflowLogger = new WorkflowLoggerDao();

    private final Workflow workflow;

    public PluginTaskRunner(AbstractPlugin plugin, PluginTask task, PluginService service, String path, CloudMessageService cms, Workflow workflow) {
        // Creates a RPC Client
        try {
            rpcClient = new AvroClient("http", loadHostConfig(System.getProperty("config.file", "conf/node.yaml")).getAddress(), 8080);
        } catch (IOException ex) {
            LOGGER.error("Error creating RPC Client for PluginTaskRunner");
            ex.printStackTrace();
        }

        this.workflow = workflow;
        this.service = service;
        this.task = task;
        this.path = path;
        this.cms = cms;
    }

    @Override
    public PluginTask call() throws Exception {

        // Output folder path (../output-folder/{workflowId}
        String outputFolder = ConfigurationRepository.getWorkflowOutputFolder(workflow.getId());

        File f = new File(outputFolder);

        // Verify if folder exists
        if (!f.exists()) {
            f.mkdir();
        }

        // Gets only options
        String options = task.getJobInfo().getArgs().substring(7);

        // Gets only args
        String args = task.getJobInfo().getArgs().substring(0, 7);

        String reference = "";

        // Get reference file      
        if (!task.getJobInfo().getReferenceFile().equals("")) {
            reference = ConfigurationRepository.getReferenceFolder() + task.getJobInfo().getReferenceFile() + "";
        }

        List<FileInfo> inputFiles = task.getJobInfo().getInputFiles();

        int i = 1;
        for (FileInfo info : inputFiles) {
            String input = info.getName();
            //linha comentada pois arquivos de entrada não ficam mais no AbstractPlugin
            //args = args.replaceFirst("%I" + i, path + File.pathSeparator + plugin.getInputFiles().get(input).first);
            args = args.replaceFirst("%I" + i, path + PATHFILES + input + " ");
            i++;
        }

        List<String> outputs = task.getJobInfo().getOutputs();
        i = 1;
        for (String output : outputs) {
            // {output_folder}/{service_name}_output_{hash}
            args = args.replaceFirst("%O" + i, " " + outputFolder + output);
            i++;
        }

        Process p;

        try {
            LOGGER.info("Command line: " + service.getPath() + " " + options + " " + reference + " " + args);

            p = Runtime.getRuntime().exec(service.getPath() + " " + options + " " + reference + " " + args);
            // p = Runtime.getRuntime().exec(path+service.getPath().substring(1,service.getPath().length()) + " " + args);

            task.setState(PluginTaskState.RUNNING);

            if (cms != null) {
                cms.setData(Path.NODE_TASK.getFullPath(task.getPluginExec(), task.getJobInfo().getId()), task.toString());
            }

            BufferedReader saidaSucesso = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader saidaErro = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            int j = 0;
            while ((line = saidaSucesso.readLine()) != null && j < 6) {
                // First line is the comand line execution (with BioNimbuZ paths)
                if (j == 0) {
                    j++;
                    continue;
                }
                workflowLogger.log(new Log("Job ID <b>" + task.getJobInfo().getId() + "</b>: " + line, workflow.getUserId(), workflow.getId(), LogSeverity.INFO));
                LOGGER.info("<span style=\"color:#16a085;\">Job " + task.getJobInfo().getId() + ": " + line + "</span>");
                j++;

            }

            /**
             * Bowtie writes its standard output as an stderror (probably
             * internal implementation of Bowtie), that's why we verify if is a
             * bowtie sysout.
             *
             */
            while ((line = saidaErro.readLine()) != null) {
                if (service.getId().equals(BOWTIE_ID)) {
                    workflowLogger.log(new Log("<span style=\"color:#16a085;\">Job ID <b>" + task.getJobInfo().getId() + "</b>: " + line + "</span>", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));
                } else {
                    workflowLogger.log(new Log("<span style=\"color:#c0392b;\">Job ID: <b>" + task.getJobInfo().getId() + "</b>: " + line + "</span>", workflow.getUserId(), workflow.getId(), LogSeverity.ERROR));
                }

                LOGGER.error("ERRO job ID:" + task.getJobInfo().getId() + "-  Arquivo de saída: " + task.getJobInfo().getOutputs() + ", Resposta :" + line);
            }

            if (p.waitFor() == 0) {
                // Get current time
                long time = System.currentTimeMillis();
                task.setTimeExec(((float) (time - task.getJobInfo().getTimestamp()) / 1000));

                // Format time
                String formattedTime = formatTime(time - task.getJobInfo().getTimestamp());

                LOGGER.info("Tempo final do job de saída: " + task.getJobInfo().getOutputs() + " - MileSegundos: " + formattedTime);

                // Log it
                workflowLogger.log(new Log("Tempo de execução do Job <b>" + task.getJobInfo().getId() + "</b>: " + formattedTime, workflow.getUserId(), workflow.getId(), LogSeverity.INFO));
                workflowLogger.log(new Log("<span style=\"color:#984eb7;\">Fim Job" + task.getJobInfo().getId() + "</span>", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));

                // Changes its state
                task.setState(PluginTaskState.DONE);

                // Copies output files to data-folder
                for (String output : outputs) {
                    String outputPath = ConfigurationRepository.getDataFolder() + output;

                    // Its path
                    long fileSize = copyFileToDataFolder(outputFolder + output, output, outputPath);

                    // Calculate hash (to avoid "null of string" avro exception)
                    String hashFile = Hash.calculateSha3(outputPath);

                    // Creates a FileInfo
                    FileInfo outputFileInfo = new FileInfo();

                    File temp = new File(outputPath);

                    // Sets its fields
                    outputFileInfo.setName(output);
                    outputFileInfo.setHash(hashFile);
                    outputFileInfo.setSize(temp.length());
                    outputFileInfo.setUserId(workflow.getUserId());
                    outputFileInfo.setUploadTimestamp(Calendar.getInstance().getTime().toString());

                    // Converts it to Avro FileInfo object
                    br.unb.cic.bionimbus.avro.gen.FileInfo outputFile = convertToAvroObject(outputPath, outputFileInfo);

                    // Write the output files to ZooKeeper
                    rpcClient.getProxy().uploadFile(outputPath, outputFile);
                }
            } else {
                task.setTimeExec(((float) (System.currentTimeMillis() - task.getJobInfo().getTimestamp()) / 1000));
                task.setState(PluginTaskState.ERRO);

                // Log it
                workflowLogger.log(new Log("<span style=\"color:#984eb7;\">Fim do Job" + task.getJobInfo().getId() + "</span>", workflow.getUserId(), workflow.getId(), LogSeverity.INFO));
                new WorkflowDao().updateStatus(workflow.getId(), WorkflowStatus.FINALIZED_WITH_ERRORS);
            }

            if (cms != null) {
                cms.setData(Path.NODE_TASK.getFullPath(task.getPluginExec(), task.getJobInfo().getId()), task.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return task;
    }

    /**
     * Format time from milliseconds.
     *
     * @param timeInMillis
     * @return
     */
    private String formatTime(long timeInMillis) {
        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);
        int hours = (int) ((timeInMillis / (1000 * 60 * 60)) % 24);

        return String.format("%02d hora(s) %02d minuto(s) %02d segundo(s)", hours, minutes, seconds);
    }

    /**
     * It's needed because next job may need it.
     *
     * @param from
     */
    private long copyFileToDataFolder(String fromPath, String filename, String outputPath) {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            File from = new File(fromPath);
            File to = new File(outputPath);

            inStream = new FileInputStream(from);
            outStream = new FileOutputStream(to);

            byte[] buffer = new byte[1024];

            long sum = 0;
            int length;

            // Copy the file content in bytes 
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
                sum += length;
            }

            LOGGER.info("Sum: " + sum);

            return length;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                outStream.close();
            } catch (IOException ex) {
                LOGGER.error("Error closing streams");
                ex.printStackTrace();
            }
        }

        return 0;
    }

    /**
     * Convert from FileInfo to Avro FileInfo.
     *
     * @param filePath
     * @param fileInfo
     * @return
     */
    public br.unb.cic.bionimbus.avro.gen.FileInfo convertToAvroObject(String filePath, FileInfo fileInfo) {
        try {
            br.unb.cic.bionimbus.avro.gen.FileInfo info = new br.unb.cic.bionimbus.avro.gen.FileInfo();

            info.setHash(fileInfo.getHash());
            info.setId(fileInfo.getName());
            info.setName(fileInfo.getName());
            info.setSize(fileInfo.getSize());
            info.setUploadTimestamp(fileInfo.getUploadTimestamp());

            return info;

        } catch (Exception ex) {
            LOGGER.error("Error converting objects");
            ex.printStackTrace();
        }

        return null;
    }

}
