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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.messaging;

import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

/**
 *
 * @author willian
 */
public interface CloudMessageService {
    
    public void connect(String connectionString);
    
    public CuratorMessageService.Path getPath();
    
    public void createZNode(CreateMode cm, String node, String desc);
    
    public Boolean getZNodeExist(String path, Watcher watcher);
    
    public List<String> getChildren(String path, Watcher watcher);
    
    public int getChildrenCount(String path, Watcher watcher);
    
    public String getData(String path, Watcher watcher);
    
    public void setData(String path, String data);
    
    public void delete(String path);
    
    public void close();
}
