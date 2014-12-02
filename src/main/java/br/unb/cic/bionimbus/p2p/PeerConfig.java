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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @Deprecated see HostConfig
 */
public class PeerConfig {

    private ID peerID = null;
    private final Set<EndPoint> endpoints = new HashSet<EndPoint>();

    public ID getPeerID() {
        return peerID;
    }

    public void setPeerID(ID peerID) {
        this.peerID = peerID;
    }

    public Collection<EndPoint> getEndPoints() {
        return endpoints;
    }

    public void add(EndPoint endpoint) {
        endpoints.add(endpoint);
    }

    public void addEndPoints(Collection<EndPoint> endpoints) {
        this.endpoints.addAll(endpoints);
    }

}
