/**
 * Copyright (C) 2011 University of Brasilia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.unb.cic.bionimbus.p2p;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

public final class Host implements Comparable<Host> {

    private String address;
    private int port;

    // o json decoder precisa deste construtor vazio
    Host() {   
    }

    public Host(String address, int port) {
        System.out.println("Host and Address configured to " + address + ":" + port);
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
        if (this == obj)
            return true;

        if (!(obj instanceof Host))
            return false;

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
