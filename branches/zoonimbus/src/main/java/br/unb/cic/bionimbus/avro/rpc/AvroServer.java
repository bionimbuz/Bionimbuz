package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import com.google.common.base.Preconditions;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.ResponderServlet;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import javax.servlet.Servlet;
import java.net.InetSocketAddress;

public class AvroServer implements RpcServer {

    private static NettyServer nettyServer;
    private static Server httpServer;
    private final String transport;
    private final int port;

    public AvroServer(String transport, int port) {
        Preconditions.checkNotNull(transport);
        this.transport = transport;
        this.port = port;
    }


    public void start() throws Exception {
        if ("netty".equalsIgnoreCase(transport)){
            startNettyServer(port);
        }
        else {
            startHTTPServer(port);
        }
    }

    // Netty Transport
    private void startNettyServer(int port) throws Exception {
        System.out.println("starting rpc nettyServer");
        nettyServer = new NettyServer(new SpecificResponder(BioProto.class, new BioProtoImpl()), new InetSocketAddress(port));
        nettyServer.start();
    }

    // HTTP Transport
    private Server createHttpServer(int port,
                                    String name,
                                    int maxThreads,
                                    int maxIdleTimeMs) throws BioNimbusException {
        try {
            SpecificResponder responder = new SpecificResponder(BioProto.class, new BioProtoImpl());
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
            new Context(httpServer, "/").addServlet(new ServletHolder(servlet), "/*");
            return httpServer;
        } catch (Exception e) {
            throw new BioNimbusException(e);
        }
    }

    // HTTP Transport
    private void startHTTPServer(int port) throws Exception {

        System.out.println("starting avro http server");
        httpServer = createHttpServer(port, "avro-rpc", 5, 10000);
        httpServer.start();
        httpServer.join(); // block this thread waiting for http thread to finish (i.e. goes 'forevever')
    }

    public static void main(String[] args) throws Exception {
        String config = "netty";
        new AvroServer(config, 9999).start();
    }
}
