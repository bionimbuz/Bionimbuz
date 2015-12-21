package br.unb.cic.bionimbus.plugin;

import java.util.List;

import com.google.common.primitives.Longs;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class PluginService {

    private String id;
    private String path;
    private Double presetMode = null;

    // NOT USED
    private String name;
    private List<String> arguments;
    private List<String> input;
    private List<String> output;
    private String info;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getPresetMode() {
        return presetMode;
    }

    public void setPresetMode(Double presetMode) {
        this.presetMode = presetMode;
    }

    // NOT USED
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // NOT USED
    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    // NOT USED
    public List<String> getInput() {
        return input;
    }

    public void setInput(List<String> input) {
        this.input = input;
    }

    // NOT USED
    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

    // NOT USED
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PluginService)) {
            return false;
        }

        PluginService other = (PluginService) object;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Longs.hashCode(Integer.parseInt(id));
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
