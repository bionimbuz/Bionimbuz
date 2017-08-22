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
package br.unb.cic.bionimbuz.services;

import java.util.List;

import org.apache.zookeeper.WatchedEvent;

import br.unb.cic.bionimbuz.toSort.Listeners;

public interface Service {

    public void start(List<Listeners> listeners);

    public void shutdown();

    public void getStatus();

    /**
     * Método para tratar os watchers disparados pelo zookeeper
     */
    public void verifyPlugins();

    public void event(WatchedEvent eventType);

}
