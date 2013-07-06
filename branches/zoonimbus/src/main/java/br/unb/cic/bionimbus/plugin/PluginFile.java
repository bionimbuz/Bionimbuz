package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.avro.gen.FileInfo;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Classe que recebe os dados do arquivo enviado pelo cliente
 * @author breno-linux
 */

public class PluginFile {

    public PluginFile(){
    }
    
    public PluginFile(FileInfo filenode){
        this.id = filenode.getFileId();
        this.name= filenode.getName();
        this.size =filenode.getSize();
    }
    private String id;

    private String path;
    
    private String name;

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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(String service) {
        this.service = service;
    }    
    
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

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
