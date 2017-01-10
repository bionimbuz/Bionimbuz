package br.unb.cic.bionimbuz.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 */
@Entity
@Table(name = "tb_job")
public class Job implements Serializable {

    @Id
    private String id;

    private long testId;

    private String localId;

    private String serviceId;

    private String args = "";
    
    @Transient
    private List< String> ipjob;

//    @Transient
    // inputs = [{input.id, input.size}]
//    private List<Pair<String, Long>> inputs;
    @Transient
    private List<FileInfo> inputFiles;

    @Transient
    private String inputURL;

    @Transient
    private List<String> outputs;

    private long timestamp;

    private Double worstExecution = null;

    @Transient
    private final List<String> dependencies;

    private String referenceFile;

    public Job() {
//        inputs = new ArrayList<>();
        inputFiles = new ArrayList<>();
        outputs = new ArrayList<>();
        dependencies = new ArrayList<>();
        ipjob =new ArrayList<>();
    }

    /**
     * Receives only the String ID
     *
     * @param id
     */
    public Job(String id) {
        this.id = id;
//        inputs = new ArrayList<>();
        inputFiles = new ArrayList<>();
        outputs = new ArrayList<>();
        dependencies = new ArrayList<>();
        ipjob=new ArrayList<>();
    }

    /**
     * This constructor is for testing purposes only
     *
     * @param worstExecution
     */
    public Job(double worstExecution) {
        this.worstExecution = worstExecution;
//        inputs = new ArrayList<>();
        inputFiles = new ArrayList<>();
        outputs = new ArrayList<>();
        dependencies = new ArrayList<>();
        ipjob=new ArrayList<>();
    }
    
    /**
     * Receives the String ID and ipjob
     *
     * @param id
     * @param ipjob
     */
    public Job(String id, List<String> ipjob) {
        this.id = id;
        inputFiles = new ArrayList<>();
        outputs = new ArrayList<>();
        dependencies = new ArrayList<>();
        this.ipjob = ipjob;
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

    /**
     * Web application verifies if it is duplicated, so do not need to iterate
     * over the input files.
     *
     * @param fileInfo
     */
    public void addInput(FileInfo fileInfo) {
        inputFiles.add(fileInfo);
    }

    public List<FileInfo> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(List<FileInfo> inputFiles) {
        this.inputFiles = inputFiles;
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

    /**
     * Add a dependency to be executed beforehand
     *
     * @param id The unique id of a job
     */
    public void addDependency(String id) {
        dependencies.add(id);
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public String getInputURL() {
        return inputURL;
    }

    public void setInputURL(String inputURL) {
        this.inputURL = inputURL;
    }

    public String getReferenceFile() {
        return referenceFile;
    }

    public void setReferenceFile(String referenceFile) {
        this.referenceFile = referenceFile;
    }

    public long getTestId() {
		return testId;
	}

	public void setTestId(long testId) {
		this.testId = testId;
	}

	@Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException ex) {
            Logger.getLogger(Job.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * @return the ipjob
     */
    public List<String> getIpjob() {
        return ipjob;
    }

    /**
     * @param ipjob the ipjob to set
     */
    public void setIpjob(List<String> ipjob) {
        this.ipjob = ipjob;
    }

}
