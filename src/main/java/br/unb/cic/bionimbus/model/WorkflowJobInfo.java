package br.unb.cic.bionimbus.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vinicius
 */
public class WorkflowJobInfo {

    private String id;

    private String localId;

    private long serviceId;

    private String args = "";

    private List<UploadedFileInfo> inputFiles;

    private List<URL> inputURL;

    private List<String> outputs;

    private String timestamp;

    public WorkflowJobInfo() throws MalformedURLException {
        this.inputFiles = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.inputURL = new ArrayList<>();
    }

    public WorkflowJobInfo(String id) throws MalformedURLException {
        this.id = id;
        this.inputFiles = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.inputURL = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public List<UploadedFileInfo> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(List<UploadedFileInfo> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public void addInputFile(UploadedFileInfo file) {
        inputFiles.add(file);
    }

    public List<URL> getInputURL() {
        return inputURL;
    }

    public void setInputURL(List<URL> inputURL) {
        this.inputURL = inputURL;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
