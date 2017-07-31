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

import br.unb.cic.bionimbuz.p2p.Host;

public class PluginGetFile implements PluginOps {

    private PluginFile pluginFile;

    private String taskId;

    private Host peer;

    public PluginFile getPluginFile() {
        return pluginFile;
    }

    public void setPluginFile(PluginFile pluginFile) {
        this.pluginFile = pluginFile;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Host getPeer() {
        return peer;
    }

    public void setPeer(Host peer) {
        this.peer = peer;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PluginGetFile)) {

            PluginGetFile other = (PluginGetFile) object;

            return (this.pluginFile.equals(other.pluginFile) && this.taskId.equals(other.taskId));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (pluginFile.toString() + taskId).hashCode();
    }

    @Override
    public String toString() {
        return pluginFile.toString() + taskId;
    }
}
