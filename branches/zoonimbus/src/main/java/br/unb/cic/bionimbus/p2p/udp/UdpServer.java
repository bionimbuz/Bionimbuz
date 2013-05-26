package br.unb.cic.bionimbus.p2p.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class UdpServer {

    public static final int DEFAULT_PORT = 9999;

    private int port;

    private ChannelFactory factory;
    private ConnectionlessBootstrap bootstrap;

    private volatile boolean running = false;

    private ExecutorService executorService;

    private Channel serverChannel;


    public UdpServer() {
        this(DEFAULT_PORT);
    }

    public UdpServer(int port) {
        this.port = port;
    }

    void start() throws IOException {

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setDaemon(true).setDaemon(true).setNameFormat("udp-server");

        executorService = Executors.newCachedThreadPool(builder.build());

        factory = new NioDatagramChannelFactory(executorService);
        bootstrap = new ConnectionlessBootstrap(factory);

        bootstrap.setPipelineFactory(new JsonUdpServerPipelineFactory());

        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("broadcast", false);
        bootstrap.setOption("sendBufferSize", 65536);
        bootstrap.setOption("receiveBufferSize", 65536);

        System.out.println("UDP server listening on port " + port);

        serverChannel = bootstrap.bind(new InetSocketAddress(port));

        running = true;
    }

    public void stop() {
        System.out.println("stopping UDP server");

        serverChannel.close();
//		factory.releaseExternalResources();
        bootstrap.releaseExternalResources();

        running = false;

        System.out.println("server stopped");
    }

    public boolean isRunning() {
        return running;
    }
}
