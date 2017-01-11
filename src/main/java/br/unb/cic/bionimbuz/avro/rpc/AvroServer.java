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
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.ResponderServlet;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.net.InetSocketAddress;

public class AvroServer implements RpcServer {

    private static NettyServer nettyServer;
    private static Server httpServer;
    private final String transport;
    private final int port;
    @Inject
    private BioProto bioProto;
    private static final Logger LOGGER = LoggerFactory.getLogger(AvroServer.class);

    public AvroServer() {
        this("http", 8080);
    }

    public AvroServer(String transport, int port) {
        Preconditions.checkNotNull(transport);
        this.transport = transport;
        this.port = port;
    }

    @Override
    public void start() throws Exception {
        if ("netty".equalsIgnoreCase(transport)) {
            startNettyServer(port);
        } else {
            startHTTPServer(port);
        }
    }

    // Netty Transport
    private void startNettyServer(int port) throws Exception {
        LOGGER.info("Starting RPC NettyServer on port 8080");
        nettyServer = new NettyServer(new SpecificResponder(BioProto.class, bioProto), new InetSocketAddress(port));
        nettyServer.start();
    }

    // HTTP Transport
    private Server createHttpServer(int port, String name, int maxThreads, int maxIdleTimeMs) throws BioNimbusException {
        try {
            // Creates HTTP Server on int port
            Server httpServer = new Server(port);

            SpecificResponder responder = new SpecificResponder(BioProto.class, bioProto);

            // QueuedThreadPool is jetty's thread pool implementation;
            // this lets us give it a name.
            QueuedThreadPool qtp = new QueuedThreadPool();
            qtp.setName(name);
            qtp.setDaemon(true);
            qtp.setMaxThreads(maxThreads);
            qtp.setMaxIdleTimeMs(maxIdleTimeMs);
            httpServer.setThreadPool(qtp);

            // Creates Responder Servlet
            Servlet servlet = new ResponderServlet(responder);

            // Creates Context adding a holder
            new Context(httpServer, "/").addServlet(new ServletHolder(servlet), "/*");

            // Return the early created Server
            return httpServer;
        } catch (Exception e) {
            throw new BioNimbusException(e);
        }
    }

    // HTTP Transport
    private void startHTTPServer(int port) throws Exception {

        LOGGER.info("Starting Avro HTTP Server with configs [port=8080, maxThreads=5, maxIdleTimeMs=10000]");
        httpServer = createHttpServer(port, "avro-rpc", 5, 10000);
        httpServer.start();
//        httpServer.join(); // block this thread waiting for http thread to finish (i.e. goes 'forevever')
    }

    public static void main(String[] args) throws Exception {
        String config = "http";
        new AvroServer(config, 8080).start();
    }
}
