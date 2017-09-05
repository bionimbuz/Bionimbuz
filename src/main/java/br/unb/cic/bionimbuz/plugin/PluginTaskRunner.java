/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package br.unb.cic.bionimbuz.plugin;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbuz.avro.rpc.AvroClient;
import br.unb.cic.bionimbuz.avro.rpc.RpcClient;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.config.ConfigurationRepository;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.model.Log;
import br.unb.cic.bionimbuz.model.LogSeverity;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.model.WorkflowStatus;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowDao;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowLoggerDao;
import br.unb.cic.bionimbuz.security.HashUtil;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbuz.services.storage.bucket.BioBucket;
import br.unb.cic.bionimbuz.services.storage.bucket.CloudStorageService;

public class PluginTaskRunner implements Callable<PluginTask> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginTaskRunner.class);

    private static final String BOWTIE_ID = "1";

    private final PluginTask task;
    private final PluginService service;
    private final CloudMessageService cms;

    private final RpcClient rpcClient;

    private final WorkflowLoggerDao workflowLogger = new WorkflowLoggerDao();

    private final Workflow workflow;

    public PluginTaskRunner(PluginTask task, PluginService service, CloudMessageService cms, Workflow workflow) {
        // Creates a RPC Client

        this.rpcClient = new AvroClient("http", BioNimbusConfig.get().getAddress(), 8080);
        this.workflow = workflow;
        this.service = service;
        this.task = task;
        this.cms = cms;
    }

    @Override
    public PluginTask call() throws Exception {

        // Output folder path (../output-folder/{workflowId}
        final String configPath = ConfigurationRepository.getWorkflowOutputFolder(this.workflow.getId());
        final File outputFolder = new File(configPath);

        // Verify if folder exists
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        // Gets only options
        final String options = this.task.getJobInfo().getArgs().substring(7);

        // Gets only args
        String args = this.task.getJobInfo().getArgs().substring(0, 7);

        String reference = "";

        // Get reference file
        if (!this.task.getJobInfo().getReferenceFile().equals("") && !this.task.getJobInfo().getReferenceFile().equals(" ")) {
            reference = BioNimbusConfig.get().getReferenceFolder() + this.task.getJobInfo().getReferenceFile() + "";
        }

        final List<FileInfo> inputFiles = this.task.getJobInfo().getInputFiles();

        int i = 1;
        for (final FileInfo info : inputFiles) {
            final String input = info.getName();
            // linha comentada pois arquivos de entrada não ficam mais no AbstractPlugin
            // args = args.replaceFirst("%I" + i, path + File.pathSeparator +
            // plugin.getInputFiles().get(input).first);

            if (info.getBucket() == null) {

                args = args.replaceFirst("%I" + i, BioNimbusConfig.get().getDataFolder() + input + " ");
            } else {

                final BioBucket bucket = CloudStorageService.getBucket(info.getBucket());

                args = args.replaceFirst("%I" + i, bucket.getMountPoint() + "/" + BioNimbusConfig.get() + input + " ");
            }

            i++;
        }

        final List<String> outputs = this.task.getJobInfo().getOutputs();
        i = 1;
        for (final String output : outputs) {
            // {output_folder}/{service_name}_output_{hash}
            args = args.replaceFirst("%O" + i, " " + outputFolder + "/" + output);
            i++;
        }

        Process p;

        try {
            LOGGER.info("Command line: " + this.service.getPath() + " " + options + " " + reference + " " + args);

            // TODO checkpoint-restart

            p = Runtime.getRuntime().exec(this.service.getPath() + " " + options + " " + reference + " " + args);
            // p =
            // Runtime.getRuntime().exec(path+service.getPath().substring(1,service.getPath().length())
            // + " " + args);

            this.task.setState(PluginTaskState.RUNNING);

            if (this.cms != null) {
                this.cms.setData(Path.NODE_TASK.getFullPath(this.task.getPluginExec(), this.task.getJobInfo().getId()), this.task.toString());
            }

            final BufferedReader saidaSucesso = new BufferedReader(new InputStreamReader(p.getInputStream()));
            final BufferedReader saidaErro = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            int j = 0;
            while ((line = saidaSucesso.readLine()) != null && j < 6) {
                // First line is the comand line execution (with BioNimbuZ paths)
                if (j == 0) {
                    j++;
                    continue;
                }
                this.workflowLogger.log(new Log("Job ID <b>" + this.task.getJobInfo().getId() + "</b>: " + line, this.workflow.getUserId(), this.workflow.getId(), LogSeverity.INFO));
                LOGGER.info("<span style=\"color:#16a08(\"<5;\">Job " + this.task.getJobInfo().getId() + ": " + line + "</span>");
                j++;

            }

            /**
             * Bowtie writes its standard output as an stderror (probably internal
             * implementation of Bowtie), that's why we verify if is a bowtie sysout.
             *
             */
            while ((line = saidaErro.readLine()) != null) {
                if (this.service.getId().equals(BOWTIE_ID)) {
                    this.workflowLogger.log(new Log("<span style=\"color:#16a085;\">Job ID <b>" + this.task.getJobInfo().getId() + "</b>: " + line + "</span>", this.workflow.getUserId(),
                            this.workflow.getId(), LogSeverity.INFO));
                } else {
                    this.workflowLogger.log(new Log("<span style=\"color:#c0392b;\">Job ID: <b>" + this.task.getJobInfo().getId() + "</b>: " + line + "</span>", this.workflow.getUserId(),
                            this.workflow.getId(), LogSeverity.ERROR));
                }

                LOGGER.error("ERRO job ID:" + this.task.getJobInfo().getId() + "-  Arquivo de saída: " + this.task.getJobInfo().getOutputs() + ", Resposta :" + line);
            }

            if (p.waitFor() == 0) {
                // Get current time
                final long time = System.currentTimeMillis();
                this.task.setTimeExec((float) (time - this.task.getJobInfo().getTimestamp()) / 1000);

                // Format time
                final String formattedTime = this.formatTime(time - this.task.getJobInfo().getTimestamp());

                LOGGER.info("Tempo final do job de saída: " + this.task.getJobInfo().getOutputs() + " - MileSegundos: " + formattedTime);

                // Log it
                this.workflowLogger
                        .log(new Log("Tempo de execução do Job <b>" + this.task.getJobInfo().getId() + "</b>: " + formattedTime, this.workflow.getUserId(), this.workflow.getId(), LogSeverity.INFO));

                // AmazonAPI api = new AmazonAPI();
                // api.terminate(this.task.getJobInfo().getIpjob().get(0));
                // Thread.sleep(5000);

                // this.workflowLogger
                // .log(new Log("<span style=\"color:#873eb6;\">Deletando Máquina Virtual " +
                // this.task.getJobInfo().getIpjob().get(0) + "</span>",
                // this.workflow.getUserId(), this.workflow.getId(),
                // LogSeverity.INFO));

                this.workflowLogger.log(new Log("<span style=\"color:#873eb6;\">Deletando Máquina Virtual ", this.workflow.getUserId(), this.workflow.getId(), LogSeverity.INFO));

                this.workflowLogger
                        .log(new Log("<span style=\"color:#984eb7;\">Fim Job" + this.task.getJobInfo().getId() + "</span>", this.workflow.getUserId(), this.workflow.getId(), LogSeverity.INFO));

                // Changes its state
                this.task.setState(PluginTaskState.DONE);

                // Copies output files to data-folder
                for (final String output : outputs) {
                    final String outputPath = BioNimbusConfig.get().getDataFolder() + output;

                    // Its path
                    this.copyFileToDataFolder(outputFolder + "/" + output, output, outputPath);

                    // Calculate hash (to avoid "null of string" avro exception)
                    final String hashFile = HashUtil.computeNativeSHA3(outputPath);

                    // Creates a FileInfo
                    final FileInfo outputFileInfo = new FileInfo();
                    final File temp = new File(outputPath);
                    final java.nio.file.Path pathOut = FileSystems.getDefault().getPath(outputPath);

                    Files.copy(temp.toPath(), pathOut, ATOMIC_MOVE);

                    // Sets its fields
                    outputFileInfo.setName(output);
                    outputFileInfo.setHash(hashFile);
                    outputFileInfo.setSize(temp.length());
                    outputFileInfo.setUserId(this.workflow.getUserId());
                    outputFileInfo.setUploadTimestamp(Calendar.getInstance().getTime().toString());

                    // Converts it to Avro FileInfo object
                    final br.unb.cic.bionimbuz.avro.gen.FileInfo outputFile = this.convertToAvroObject(BioNimbusConfig.get().getDataFolder(), outputFileInfo);

                    // Write the output files to ZooKeeper
                    this.rpcClient.getProxy().uploadFile(BioNimbusConfig.get().getDataFolder(), outputFile);
                }
            } else {
                this.task.setTimeExec((float) (System.currentTimeMillis() - this.task.getJobInfo().getTimestamp()) / 1000);
                this.task.setState(PluginTaskState.ERRO);

                // Log it
                this.workflowLogger
                        .log(new Log("<span style=\"color:#984eb7;\">Fim do Job" + this.task.getJobInfo().getId() + "</span>", this.workflow.getUserId(), this.workflow.getId(), LogSeverity.INFO));

                new WorkflowDao().updateStatus(this.workflow.getId(), WorkflowStatus.FINALIZED_WITH_ERRORS);
            }

            if (this.cms != null) {
                this.cms.setData(Path.NODE_TASK.getFullPath(this.task.getPluginExec(), this.task.getJobInfo().getId()), this.task.toString());
            }

        } catch (final IOException | InterruptedException e) {
            LOGGER.info(e.getMessage());
        }

        return this.task;
    }

    /**
     * Format time from milliseconds.
     *
     * @param timeInMillis
     * @return
     */
    private String formatTime(long timeInMillis) {
        final int seconds = (int) (timeInMillis / 1000) % 60;
        final int minutes = (int) (timeInMillis / (1000 * 60) % 60);
        final int hours = (int) (timeInMillis / (1000 * 60 * 60) % 24);

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

            final File from = new File(fromPath);
            final File to = new File(outputPath);

            inStream = new FileInputStream(from);
            outStream = new FileOutputStream(to);

            final byte[] buffer = new byte[1024];

            long sum = 0;
            int length;

            // Copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
                sum += length;
            }

            LOGGER.info("Sum: " + sum);

            return length;
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                outStream.close();
            } catch (final IOException ex) {
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
    public br.unb.cic.bionimbuz.avro.gen.FileInfo convertToAvroObject(String filePath, FileInfo fileInfo) {
        try {
            final br.unb.cic.bionimbuz.avro.gen.FileInfo info = new br.unb.cic.bionimbuz.avro.gen.FileInfo();

            info.setHash(fileInfo.getHash());
            info.setId(fileInfo.getName());
            info.setName(fileInfo.getName());
            info.setSize(fileInfo.getSize());
            info.setUploadTimestamp(fileInfo.getUploadTimestamp());

            return info;

        } catch (final Exception ex) {
            LOGGER.error("Error converting objects");
            ex.printStackTrace();
        }

        return null;
    }

}
