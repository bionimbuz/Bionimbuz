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
package br.unb.cic.bionimbuz.p2p;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

public final class Host implements Comparable<Host> {

    private String address;
    private int port;

    // o json decoder precisa deste construtor vazio
    Host() {
    }

    public Host(String address, int port) {
//        System.out.println("Host and Address configured to " + address + ":" + port);
        this.address = address.trim();
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void setAddress(String address) {
        this.address = address.trim();
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(address, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Host)) {
            return false;
        }

        Host other = (Host) obj;

        return address.equals(other.address) && (port == other.port);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("address", address)
                .add("port", port)
                .toString();
    }

    @Override
    public int compareTo(Host o) {
        return ComparisonChain.start()
                .compare(address, o.address)
                .compare(port, o.port)
                .result();
    }

}
