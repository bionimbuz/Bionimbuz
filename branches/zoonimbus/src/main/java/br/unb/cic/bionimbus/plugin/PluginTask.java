package br.unb.cic.bionimbus.plugin;

import java.util.UUID;

import br.unb.cic.bionimbus.client.JobInfo;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class PluginTask implements PluginOps {

    private PluginTaskState state = PluginTaskState.WAITING;
    
    private Float tempExec = 0f;
    
    private String pluginExec;

    private String id = UUID.randomUUID().toString();
    
    private String pluginTaskPathZk;

    private JobInfo jobInfo;

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public PluginTaskState getState() {
        return state;
    }

    public void setState(PluginTaskState state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getTimeExec() {
        return tempExec;
    }

    public void setTimeExec(Float tempExec) {
        this.tempExec = tempExec;
    }

    public String getPluginExec() {
        return pluginExec;
    }

    public void setPluginExec(String pluginExec) {
        this.pluginExec = pluginExec;
    }
    
    public String getPluginTaskPathZk() {
        return pluginTaskPathZk;
    }

    public void setPluginTaskPathZk(String pathZk) {
        this.pluginTaskPathZk = pathZk;
    }

    
    
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (!(object instanceof PluginTask)) {
            return false;
        }

        PluginTask other = (PluginTask) object;

        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

//    @Override
//    public String toString() {
//        return id + ":" + state;
//    }
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
