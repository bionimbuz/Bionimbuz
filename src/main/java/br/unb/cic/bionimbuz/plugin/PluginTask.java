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

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbuz.model.Job;

public class PluginTask implements PluginOps {

    private PluginTaskState state = PluginTaskState.WAITING;

    private Float tempExec = 0f;

    private String pluginExec;

    private String id = UUID.randomUUID().toString();

    private Job jobInfo;

    public Job getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(Job jobInfo) {
        this.jobInfo = jobInfo;
    }

    public PluginTaskState getState() {
        return state;
    }

    public void setState(PluginTaskState state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getTimeExec() {
        return tempExec;
    }

    public void setTimeExec(Float tempExec) {
        this.tempExec = tempExec;
    }

    public String getPluginExec() {
        return pluginExec;
    }

    public void setPluginExec(String pluginExec) {
        this.pluginExec = pluginExec;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PluginTask)) {
            return false;
        }

        PluginTask other = (PluginTask) object;

        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException ex) {
            Logger.getLogger(Job.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
