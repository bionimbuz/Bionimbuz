package br.unb.cic.bionimbus.p2p.messages;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonIgnore;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;

public class BulkMessage {

    private String peerID;
    private Host host;

    @JsonIgnore
    private long timestamp;

    @JsonIgnore
    private Collection<PluginInfo> pluginList;

    @JsonIgnore
    private Collection<PluginFile> fileList;

    @JsonIgnore
    private Collection<JobInfo> jobList;

    @JsonIgnore
    private PluginTask task;

    @JsonIgnore
    private PluginInfo pluginInfo;

    @JsonIgnore
    private PluginFile pluginFile;

    @JsonIgnore
    private JobInfo jobInfo;

    @JsonIgnore
    private FileInfo fileInfo;

    @JsonIgnore
    private String jobId;

    @JsonIgnore
    private String pluginId;

    @JsonIgnore
    private String fileId;

    @JsonIgnore
    private String taskId;

    @JsonIgnore
    private String error;

    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public Host getHost() {
        return host;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPluginList(Collection<PluginInfo> pluginList) {
        this.pluginList = pluginList;
    }

    public Collection<PluginInfo> getPluginList() {
        return pluginList;
    }

    public void setFileList(Collection<PluginFile> fileList) {
        this.fileList = fileList;
    }

    public Collection<PluginFile> getFileList() {
        return fileList;
    }

    public void setJobList(Collection<JobInfo> jobList) {
        this.jobList = jobList;
    }

    public Collection<JobInfo> getJobList() {
        return jobList;
    }

    public void setTask(PluginTask task) {
        this.task = task;
    }

    public PluginTask getTask() {
        return task;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginFile(PluginFile pluginFile) {
        this.pluginFile = pluginFile;
    }

    public PluginFile getPluginFile() {
        return pluginFile;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

}
