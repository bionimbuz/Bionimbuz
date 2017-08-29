package br.unb.cic.bionimbuz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

/**
 * @author jgomes | 27 de set de 2016 - 16:01:51
 */
public class RuntimeUtil {

    private static final String SPACE_STRING = " ";
    private static final String LINE_BREAK = "\n";

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructors.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private RuntimeUtil() {
        super();
    }
    
    public static class Command{
        
        private List<String> lstCommands = new ArrayList<>();
        
        public Command(final String... command) {
            for (final String arg : command) {
                final String[] split = arg.split(SPACE_STRING);
                for (final String args : split) {
                    lstCommands.add(args);
                }
            }
        }

        public List<String> getLstCommands() {
            return lstCommands;
        }        
    }
    
    synchronized public static String runCommand(final Command command, final Map<String, String> env) throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder(command.getLstCommands());
        Map<String, String> currentEnv = builder.environment();
        if(env != null) {
            for (Map.Entry<String, String> entry : env.entrySet()) {
                currentEnv.put(entry.getKey(), entry.getValue());
            }
        }
        
        final Process process = builder.start();
        return getProcessReturn(process);        
    }
    
    synchronized public static String runCommand(final Command command) throws IOException, InterruptedException {
        return runCommand(command, null);
    }

    synchronized private static String getProcessReturn(final Process process) throws InterruptedException {
        final StreamGobblerThread errorGobbler = new RuntimeUtil().new StreamGobblerThread(process.getErrorStream());
        final StreamGobblerThread outputGobbler = new RuntimeUtil().new StreamGobblerThread(process.getInputStream());
        process.waitFor();
        errorGobbler.join();
        outputGobbler.join();
        final StringBuilder builder = new StringBuilder();
        builder.append(outputGobbler.getOutputstream());
        builder.append(errorGobbler.getOutputstream());
        final int length = builder.length();
        final int lastLineBreak = builder.lastIndexOf(LINE_BREAK);
        if (length > 0 && lastLineBreak == length - 1) {
            builder.deleteCharAt(lastLineBreak);
        }
        builder.trimToSize();
        return builder.toString();
    }

    /**
     * @author jgomes | 27 de set de 2016 - 16:22:15
     */
    private class StreamGobblerThread extends Thread {

        private static final String RUNTIME_EXECUTION_ERROR = "Runtime execution error!";
        private final InputStream stream;
        private String outputstream;

        // --------------------------------------------------------------
        // Constructors.
        // --------------------------------------------------------------
        public StreamGobblerThread(final InputStream stream) {
            super();
            this.stream = stream;
            super.start();
        }

        // --------------------------------------------------------------
        // * @see java.lang.Thread#run()
        // --------------------------------------------------------------
        @Override
        public void run() {
            try (
                 final InputStreamReader reader = new InputStreamReader(this.stream);
                 final BufferedReader buffer = new BufferedReader(reader);) {
                final StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = buffer.readLine()) != null) {
                    builder.append(line);
                    builder.append(LINE_BREAK);
                }
                this.outputstream = builder.toString();
            } catch (final IOException e) {
                LoggerFactory.getLogger(StreamGobblerThread.class).error(RUNTIME_EXECUTION_ERROR, e);
            }
        }

        // --------------------------------------------------------------
        // Get/Set.
        // --------------------------------------------------------------
        public String getOutputstream() {
            return this.outputstream;
        }
    }
}
