package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.ResponderServlet;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import javax.servlet.Servlet;
import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/21/13
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class AvroServer {

    private static NettyServer nettyServer;
    private static Server httpServer;

    // Netty Transport
    public static void main(String[] args) throws Exception {
//        System.out.println("starting rpc nettyServer");
        nettyServer = new NettyServer(new SpecificResponder(BioProto.class, new BioProtoImpl()), new InetSocketAddress(65111));

        startHTTP();
    }

    public static Server createHttpAvroServer(int port,
                                              String name,
                                              int maxThreads,
                                              int maxIdleTimeMs,
                                              SpecificResponder responder) throws BioNimbusException {
        try {
            Server httpServer = new Server(port);
            QueuedThreadPool qtp = new QueuedThreadPool();
            // QueuedThreadPool is jetty's thread pool implementation;
            // this lets us give it a name.
            qtp.setName(name);
            qtp.setDaemon(true);
            qtp.setMaxThreads(maxThreads);
            qtp.setMaxIdleTimeMs(maxIdleTimeMs);
            httpServer.setThreadPool(qtp);
            Servlet servlet = new ResponderServlet(responder);
            new Context(httpServer, "/").addServlet(new ServletHolder(servlet),
                    "/*");
            return httpServer;
        } catch (Exception e) {
            throw new BioNimbusException(e);
        }
    }

    public static void startHTTP() throws Exception {

        // main:
        SpecificResponder responder = new SpecificResponder(BioProto.class, new BioProtoImpl());
        httpServer = createHttpAvroServer(9090, "avro-rpc", 5, 10000, responder);
        httpServer.start();
        httpServer.join();

    }

}
