package br.unb.cic.bionimbus.plugin;


import com.google.common.primitives.Longs;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class PluginService {

    private String id;

    private String name;
    
    private String path;

    private Double presetMode = null;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (this == object) {
            return true;
        }
        if (!(object instanceof PluginService)) {
            return false;
        }

        PluginService other = (PluginService) object;
        return id.equals(other.getId());
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
