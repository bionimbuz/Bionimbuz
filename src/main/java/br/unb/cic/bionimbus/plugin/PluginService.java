package br.unb.cic.bionimbus.plugin;

import java.util.List;

import com.google.common.primitives.Longs;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class PluginService {

    private long id;
    private String name;
    private List<String> arguments;
    private List<String> input;
    private List<String> output;
    private String info;
    private String path;
    private final List<Double> historyMode = new ArrayList<Double>();
    private Double historyStep;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public List<String> getInput() {
        return input;
    }

    public void setInput(List<String> input) {
        this.input = input;
    }

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public List<Double> getHistoryMode () {
        return historyMode;
    }
    
    public void addModeToHistory (Double mode) {
        historyMode.add(mode);
    }
    
    public void setHistoryStep(Double historyStep) {
        this.historyStep = historyStep;
    }

    public Double getHistoryStep() {
        return historyStep;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof PluginService))
            return false;

        PluginService other = (PluginService) object;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Longs.hashCode(id);
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception ex) {
            Logger.getLogger(PluginService.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }
}
