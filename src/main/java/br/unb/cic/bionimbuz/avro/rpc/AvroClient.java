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
package br.unb.cic.bionimbuz.avro.rpc;

import br.unb.cic.bionimbuz.avro.gen.BioProto;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;

/**
 * Implements RPC Client Uses port 8080
 */
public class AvroClient implements RpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroClient.class);

    private final int port;
    private final String address;
    private final String transport;
    private NettyTransceiver nettyClient;

    /**
     * Initializes AvroClient.
     *
     * @param transport
     * @param address
     * @param port
     */
    public AvroClient(String transport, String address, int port) {
        this.port = port;
        this.address = address;
        this.transport = transport;
    }

    /**
     * Return Netty Transport
     *
     * @return
     */
    public BioProto getNettyTransport() {
        BioProto proxy = null;

        try {
            nettyClient = new NettyTransceiver(new InetSocketAddress(address, port));
            proxy = (BioProto) SpecificRequestor.getClient(BioProto.class, nettyClient);

        } catch (IOException ex) {
            LOGGER.error("[IOException] " + ex.getMessage());
        }

        return proxy;
    }

    /**
     * Return HTTP Transport
     *
     * @return
     */
    public BioProto getHttpTransport() {
        BioProto proxy = null;

        try {
            HttpTransceiver transceiver = new HttpTransceiver(new URL("http://" + address + ":" + port));
            proxy = (BioProto) SpecificRequestor.getClient(BioProto.class, transceiver);
        } catch (IOException ex) {
            LOGGER.error("[IOException] " + ex.getMessage());
        }

        return proxy;
    }

    @Override
    public BioProto getProxy() throws IOException {
        if ("netty".equalsIgnoreCase(transport)) {
            return getNettyTransport();
        } else {
            return getHttpTransport();
        }
    }

    @Override
    public void close() throws Exception {
        // Only Netty protocol needs explicit close
        if ("netty".equalsIgnoreCase(transport)) {
            nettyClient.close();
        }
    }

    public static void main(String[] args) throws IOException {

        BioProto proxy = new AvroClient("http", "localhost", 8080).getProxy();
        long init = System.currentTimeMillis();
        System.out.println(proxy.ping());
        long end = System.currentTimeMillis();

        System.out.println("latencia: (end - init) = " + (end - init) + "ms");
        System.out.println("end");
    }

}
