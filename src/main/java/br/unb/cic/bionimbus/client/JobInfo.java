package br.unb.cic.bionimbus.client;

import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.utils.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class JobInfo {

    private String id;

    public long testId;

    private String localId;

    private String serviceId;

    private String args = "";

    // inputs = [{input.id, input.size}]
    private List<Pair<String, Long>> inputs;

    private final List<FileInfo> inputFiles;

    private final List<String> inputURL;

    private final List<String> outputs;

    private long timestamp;

    private Double worstExecution = null;

    private final List<String> dependencies;

    public JobInfo() {
        inputs = new ArrayList<>();
        inputFiles = new ArrayList<>();
        inputURL = new ArrayList<>();
        outputs = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    /**
     * Receives only the String ID
     *
     * @param id
     */
    public JobInfo(String id) {
        this.id = id;
        inputs = new ArrayList<>();
        inputFiles = new ArrayList<>();
        inputURL = new ArrayList<>();
        outputs = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    /**
     * This constructor is for testing purposes only
     *
     * @param worstExecution
     */
    public JobInfo(double worstExecution) {
        this.worstExecution = worstExecution;
        inputs = new ArrayList<>();
        inputFiles = new ArrayList<>();
        inputURL = new ArrayList<>();
        outputs = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocalId() {
        return this.localId;
    }

    public void setLocalId(String id) {
        this.localId = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public List<Pair<String, Long>> getInputs() {
        return inputs;
    }

    public void setInputs(List<Pair<String, Long>> inputs) {
        this.inputs = inputs;
    }
        
    public void addInput(String id, Long size) {
//        TODO: change from string id to string filename
//                or: split jobinfo into 2 subclasses: staticSchedJobInfo and dynamicSchedJobInfo
        for (Pair<String, Long> pair : getInputs()) {
            if (pair.first.equals(id)) {
                getInputs().remove(pair);
                break;
            }
        }
        getInputs().add(new Pair<>(id, size));
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void addOutput(String name) {
        outputs.add(name);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * To be used only by avro
     *
     * @param worstExecution
     */
    public void setWorstExecution(double worstExecution) {
        this.worstExecution = worstExecution;
    }

    public Double getWorstExecution() {
        return worstExecution;
    }

//    /**
//     * Add a dependency to be executed beforehand
//     * @param id The unique id of a job
//     */
    public void addDependency(String id) {
        dependencies.add(id);
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException ex) {
            Logger.getLogger(JobInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}

//menas de 163 linhas

