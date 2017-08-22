package br.unb.cic.bionimbuz.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.plugin.PluginService;
import br.unb.cic.bionimbuz.services.tarification.Amazon.AmazonIndex;
import br.unb.cic.bionimbuz.services.tarification.Google.GoogleCloud;
import br.unb.cic.bionimbuz.utils.SSHCredentials;

/**
 * Used as a repository of configurations, to avoid hardcoded configurations
 * (like path="/home/zoonimbus/zoonimbusProject/").
 *
 * @author Vinicius
 */
public class ConfigurationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationRepository.class);

    /**
     * Gets root folder of the project.
     * COMENTEI POIS NÂO ESTÁ SENDO USADA
     * @return
     */
//    public static String getRootFolder() {
//        return config.getRootFolder();
//    }

    /**
     * Get Reference Folder of the project.
     *
     * @return
     */
    public static String getReferenceFolder() {
        return BioNimbusConfig.get().getReferenceFolder();
    }

    /**
     * Get Output Folder of generated files.
     *
     * @param id
     * @return
     */
    public static String getWorkflowOutputFolder(String id) {
        return BioNimbusConfig.get().getOutputFolder() + id + "/";
    }

    /**
     * Returns the path where temporary uploaded files are saved.
     *
     * @return
     */
    public static String getTemporaryUplodadedFiles() {
        return BioNimbusConfig.get().getTemporaryUploadedFiles();
    }

    /**
     * Returns the reference files.
     *
     * @return
     */
    public static ArrayList<String> getReferences() {
        return BioNimbusConfig.get().getReferences();
    }

    /**
     * Get Data Folder.
     *
     * @return
     */
    public static String getDataFolder() {
        return BioNimbusConfig.get().getDataFolder();
    }

    /**
     * Return a list of the supported formats of the system.
     *
     * @return
     */
    public static ArrayList<String> getSupportedFormats() {
        return BioNimbusConfig.get().getSupportedFormats();
    }

    /**
     * Return a list of the supported services of BioNimbuZ.
     *
     * @return
     */
    public static ArrayList<PluginService> getSupportedServices() {
        return BioNimbusConfig.get().getSupportedServices();
    }
    
    public static ArrayList<Instance> getInstances(){
        ArrayList<Instance> result =new ArrayList<>();
        AmazonIndex idx= new AmazonIndex();
        GoogleCloud gc= new GoogleCloud();
        result.addAll(idx.getListInstanceEc2());
        result.addAll(gc.getListInstanceGCE());
//        idx.EC2Instances("r3.xlarge").toString(4);
//        gc.GoogleComputeEngineInstances("N1.STANDARD-4.PREEMPTIBLE", "").toString(4);
//        AmazonIndex idx = new AmazonIndex(); 
        
        return result;
    }
    
    public static SSHCredentials getSSHCredentials() {
        
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            SSHCredentials sshCredentials = mapper.readValue(new File(BioNimbusConfig.get().getCredentialsFile()), SSHCredentials.class);

            return sshCredentials;
        } catch (IOException ex) {
            LOGGER.error("[IOException] - " + ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }
}
