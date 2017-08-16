package br.unb.cic.bionimbuz.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbuz.BioNimbuZ;

/**
 * @author jgomes | 14 de ago de 2017 - 18:40:22
 */
public class ZookeeperUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BioNimbuZ.class);
    private static final String NOT_RUNNING = "not running";
    private static final String ZK_SERVER = "zkServer.sh ";

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
        String result = execZooCmd(ZooCommand.STOP);
        LOGGER.info(result);
    }

    public static String execZooCmd(ZooCommand command) {
        String result = null;
        try {
            result = RuntimeUtil.runCommand(ZK_SERVER + command);
            LOGGER.info(result);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(result + e.getMessage());
        }
        return result;
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
