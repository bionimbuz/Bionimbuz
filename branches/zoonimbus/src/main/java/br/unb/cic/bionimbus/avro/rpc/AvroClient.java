package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;

public class AvroClient implements RpcClient {

    private final int port;
    private final String address;
    private final String transport;
    private NettyTransceiver nettyClient;

    public AvroClient(String transport, String address, int port) {
        this.port = port;
        this.address = address;
        this.transport = transport;
    }

    private BioProto getNettyTransport() throws IOException {
        System.out.println("Netty client built, got proxy");
        nettyClient = new NettyTransceiver(new InetSocketAddress(address, port));
        // client code - attach to the server and send a message
        BioProto proxy = (BioProto) SpecificRequestor.getClient(BioProto.class, nettyClient);
        return proxy;
    }

    private BioProto getHttpTransport() throws IOException {
        System.out.println("HTTP client built, got proxy");
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
