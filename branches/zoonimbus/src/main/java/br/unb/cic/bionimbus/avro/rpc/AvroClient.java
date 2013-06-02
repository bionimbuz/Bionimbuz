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

public class AvroClient implements RpcClient {

    private final int port;
    private final String address;
    private final String transport;
    private NettyTransceiver nettyClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroClient.class);

    public AvroClient(String transport, String address, int port) {
        this.port = port;
        this.address = address;
        this.transport = transport;
    }

    private BioProto getNettyTransport() throws IOException {
        LOGGER.debug("Netty client built");
        nettyClient = new NettyTransceiver(new InetSocketAddress(address, port));
        // client code - attach to the server and send a message
        BioProto proxy = (BioProto) SpecificRequestor.getClient(BioProto.class, nettyClient);
        return proxy;
    }

    private BioProto getHttpTransport() throws IOException {
        LOGGER.debug("HTTP client built");
        HttpTransceiver transceiver = new HttpTransceiver(new URL("http://" + address + ":" + port));
        BioProto proxy = (BioProto) SpecificRequestor.getClient(BioProto.class, transceiver);
        return proxy;
    }

    @Override
    public BioProto getProxy() throws IOException {
        if ("netty".equalsIgnoreCase(transport)){
             return getNettyTransport();
        }
        else {
            return getHttpTransport();
        }
    }

    @Override
    public void close() throws Exception {
        LOGGER.debug("Closing Avro RPC client");
        // only Netty protocol needs explicit close
        if ("netty".equalsIgnoreCase(transport)){
            nettyClient.close();
        }
    }

    public static void main(String[] args) throws IOException {

        BioProto proxy = new AvroClient("http", "localhost", 9999).getProxy();
        long init = System.currentTimeMillis();
        System.out.println(proxy.ping());
        long end = System.currentTimeMillis();

        System.out.println("latencia: (end - init) = " + (end - init) + "ms");
        System.out.println("end");
    }
}
