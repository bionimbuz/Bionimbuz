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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.primitives.Longs;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class PluginService {

    private String id;

    private String name;
    
    private String path;

    private Double presetMode = null;

    private String info;
    
    @JsonIgnore
    private List<String> arguments;
    @JsonIgnore
    private List<String> input;
    @JsonIgnore
    private List<String> output;

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

    /**
     * @return the arguments
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * @param arguments the arguments to set
     */
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    /**
     * @return the input
     */
    public List<String> getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(List<String> input) {
        this.input = input;
    }

    /**
     * @return the output
     */
    public List<String> getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(List<String> output) {
        this.output = output;
    }
}
