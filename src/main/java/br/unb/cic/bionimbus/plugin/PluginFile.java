package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.avro.gen.FileInfo;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Classe que recebe os dados do arquivo enviado pelo cliente
 *
 * @author breno-linux
 */
public class PluginFile {

    public PluginFile() {
    }
    
    //Recebe as informações do arquivo enviado pelo cliente e seta os dados do arquivo
    public PluginFile(FileInfo fileNode){
        this.id = fileNode.getFileId();
        this.name= fileNode.getName();
        this.size =fileNode.getSize();
        this.hash=fileNode.getHash();
    }
    private String id;

    private String path;

    private String name;
    
    private String hash;

    private long size;

    private String service;

    private List<String> pluginId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public List<String> getPluginId() {
        return pluginId;
    }

    public void setPluginId(List<String> pluginId) {
        this.pluginId = pluginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PluginFile)) {
            return false;
        }

        PluginFile other = (PluginFile) object;

        return this.getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception ex) {
            Logger.getLogger(PluginInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
