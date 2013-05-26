package br.unb.cic.bionimbus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.unb.cic.bionimbus.utils.Pair;

public class JobInfo {

    private String id = UUID.randomUUID().toString();

    private String localId;

    private long serviceId;

    private String args = "";

    private List<Pair<String, Long>> inputs = new ArrayList<Pair<String, Long>>();

    private List<String> outputs = new ArrayList<String>();

    private long timestamp;

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
}
