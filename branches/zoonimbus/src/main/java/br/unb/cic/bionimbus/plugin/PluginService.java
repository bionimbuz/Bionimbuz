package br.unb.cic.bionimbus.plugin;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.primitives.Longs;

public class PluginService {

    private long id;
    private String name;
    private List<String> arguments;
    private List<String> input;
    private List<String> output;
    private String info;
    private String path;

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
        return Objects.toStringHelper(PluginService.class).add("id", id)
                .add("name", name).toString();
    }
}
