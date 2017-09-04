package br.unb.cic.bionimbuz.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbuz.BioNimbuZ;
import br.unb.cic.bionimbuz.constants.SystemConstants;
import br.unb.cic.bionimbuz.utils.RuntimeUtil.Command;

/**
 * @author jgomes | 14 de ago de 2017 - 18:40:22
 */
public class ZookeeperUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BioNimbuZ.class);
    private static final String NOT_RUNNING = "not running";
    private static final String DIR_ZK_LOCAL = "system/zookeeper";
    private static final String ZK_SERVER = "zkServer.sh";
    private static final String ZK_SERVER_LOCAL = DIR_ZK_LOCAL+"/bin/"+ZK_SERVER;
    private static final String ZK_LOG_ENV_VAR = "ZOO_LOG_DIR";
            
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructors.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private ZookeeperUtil() {
        super();
    }

    public static void startZookeeper() {
        String result = null;
        result = execZooCmd(ZooCommand.STATUS);
        if (result.contains(NOT_RUNNING)) {
            result = execZooCmd(ZooCommand.START);
        }
    }

    public static void stopZookeeper() {
        execZooCmd(ZooCommand.STOP);
    }

    public static String execZooCmd(ZooCommand command) {
        String result = null;
        try {
            HashMap<String, String> env = new HashMap<>();
            env.put(ZK_LOG_ENV_VAR, "./"+SystemConstants.FOLDER_LOGS);
            result = RuntimeUtil.runCommand(new Command(getZookeeperCmd() + command), env);
            LOGGER.info(result);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(result + e.getMessage());
        }
        return result;
    }
    
    private static String getZookeeperCmd() {
        File f = new File(ZK_SERVER_LOCAL);
        if (f.exists() && !f.isDirectory()) {
            return ZK_SERVER_LOCAL + " ";
        }
        return ZK_SERVER + " ";
    }

    /**
     * @author jgomes | 14 de ago de 2017 - 18:58:09
     */
    public enum ZooCommand {

        START("start"),
        START_FOREGROUND("start-foreground"),
        STOP("stop"),
        RESTART("restart"),
        STATUS("status"),
        UPGRADE("upgrade"),
        PRINT_CMD("print-cmd");

        private String cmd;

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Constructors.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        private ZooCommand(String cmd) {
            this.cmd = cmd;
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // get/set.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        public String getCommand() {
            return this.cmd;
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // * @see java.lang.Enum#toString()
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        @Override
        public String toString() {
            return this.getCommand();
        }
    }
}
