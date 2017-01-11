/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package br.unb.cic.bionimbuz.plugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbuz.avro.gen.FileInfo;

/**
 * Classe que recebe os dados do arquivo enviado pelo cliente
 *
 * @author breno-linux
 */
public class PluginFile {

    public PluginFile() {
    }

    private String id;

    private String path;

    private String name;

    private String hash;

    private long size;

    private String service;
    
    private List<String> pluginId;

    // Recebe as informações do arquivo enviado pelo cliente e seta os dados do arquivo
    public PluginFile(FileInfo fileNode) {
        this.id = fileNode.getId();
        this.name = fileNode.getName();
        this.size = fileNode.getSize();
        this.hash = fileNode.getHash();
    }

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
            Logger.getLogger(PluginFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
