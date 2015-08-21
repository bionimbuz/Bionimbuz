package br.unb.cic.bionimbus.client;

import br.unb.cic.bionimbus.toSort.RepositoryService;
import br.unb.cic.bionimbus.utils.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class JobInfo {

    private String id = UUID.randomUUID().toString();

    private String localId;

    private long serviceId;

    private String args = "";
    
    // inputs = [{input.id, input.size}]
    final private List<Pair<String, Long>> inputs = new ArrayList<Pair<String, Long>>();

    final private List<String> outputs = new ArrayList<String>();

    private long timestamp;
    
    private List<Long> execHistory = null;
    
    final private List<String> dependencies = new ArrayList<String>();

    public JobInfo() {}
    
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

    public List<Pair<String, Long>> getInputs() {
        return inputs;
    }

    public void addInput(String id, Long size) {
        TODO: change from string id to string filename
                or: split jobinfo into 2 subclasses: staticSchedJobInfo and dynamicSchedJobInfo
        for (Pair<String, Long> pair : inputs) {
            if (pair.first.equals(id)) {
                inputs.remove(pair);
                break;
            }
        }
        inputs.add(new Pair<String, Long>(id, size));
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
    
    public List<Long> getHistory(RepositoryService rs) {
        if (execHistory == null)
            execHistory = rs.getTaskHistory(serviceId);
        return execHistory;
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

