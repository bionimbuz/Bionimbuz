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

public final class EndPoint {

    private String address;
    private int port;
    private Protocol protocol;

    public EndPoint() {
    }

    public EndPoint(String address, int port, Protocol protocol) {
        super();
        this.address = address;
        this.port = port;
        this.protocol = protocol;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public Protocol getProtocol() {
        return protocol;
    }


}
