package br.unb.cic.bionimbus.config;

import br.unb.cic.bionimbus.model.Instance;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.services.tarifation.Amazon.AmazonData;
import br.unb.cic.bionimbus.services.tarifation.Google.GoogleData;
import br.unb.cic.bionimbus.utils.SSHCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used as a repository of configurations, to avoid hardcoded configurations
 * (like path="/home/zoonimbus/zoonimbusProject/").
 *
 * @author Vinicius
 */
public class ConfigurationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRepository.class);

    private static final BioNimbusConfig config = loadHostConfig();

    /**
     * Gets root folder of the project.
     *
     * @return
     */
    public static String getRootFolder() {
        return config.getRootFolder();
    }

    /**
     * Get Reference Folder of the project.
     *
     * @return
     */
    public static String getReferenceFolder() {
        return config.getReferenceFolder();
    }

    /**
     * Get Output Folder of generated files.
     *
     * @param id
     * @return
     */
    public static String getWorkflowOutputFolder(String id) {
        return config.getOutputFolder() + id + "/";
    }

    /**
     * Returns the path where temporary uploaded files are saved.
     *
     * @return
     */
    public static String getTemporaryUplodadedFiles() {
        return config.getTemporaryUploadedFiles();
    }

    /**
     * Returns the reference files.
     *
     * @return
     */
    public static ArrayList<String> getReferences() {
        return config.getReferences();
    }

    /**
     * Get Data Folder.
     *
     * @return
     */
    public static String getDataFolder() {
        return config.getDataFolder();
    }

    /**
     * Return a list of the supported formats of the system.
     *
     * @return
     */
    public static ArrayList<String> getSupportedFormats() {
        return config.getSupportedFormats();
    }

    /**
     * Return a list of the supported services of BioNimbuZ.
     *
     * @return
     */
    public static ArrayList<PluginService> getSupportedServices() {
        return config.getSupportedServices();
    }

    public static SSHCredentials getSSHCredentials() {
        
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            SSHCredentials sshCredentials = mapper.readValue(new File(config.getCredentialsFile()), SSHCredentials.class);

            return sshCredentials;
        } catch (IOException ex) {
            LOGGER.error("[IOException] - " + ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }
    /**
     * Get the list of instances of amazon, google, etc...
     * @return 
     */
    
    public static ArrayList<Instance> getInstances(){
        
         AmazonData listA = new AmazonData(config);
         GoogleData listG = new GoogleData(config);
        return null;
    }

    public static BioNimbusConfig getConfig() {
        return config;
    }

    /**
     * Loads configuration file from disk.
     *
     * @return
     */
    private static BioNimbusConfig loadHostConfig() {
        BioNimbusConfig configuration = null;

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            configuration = mapper.readValue(new File(System.getProperty("config.file", "conf/node.yaml")), BioNimbusConfig.class);
        } catch (IOException ex) {
            LOGGER.info("[IOException] - " + ex.getMessage());
        }

        if (configuration.getInfra() == null) {
            configuration.setInfra("linux");
        }

        configuration.setInfra(configuration.getInfra());

        return configuration;
    }

}
