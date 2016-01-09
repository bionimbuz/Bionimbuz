package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
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
