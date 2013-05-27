package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/21/13
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class AvroClient implements RpcClient {

    public static void nettyTransport() throws IOException {
        NettyTransceiver client = new NettyTransceiver(new InetSocketAddress(65111));
        // client code - attach to the server and send a message
        BioProto proxy = (BioProto) SpecificRequestor.getClient(BioProto.class, client);
        System.out.println("Client built, got proxy");


        client.close();

    }

    public static void httpTransport() throws IOException {
        HttpTransceiver transceiver = new HttpTransceiver(new URL("http://localhost:9090/"));
        BioProto proxy = (BioProto) SpecificRequestor.getClient(BioProto.class, transceiver);

        long init = System.currentTimeMillis();
        System.out.println(proxy.ping());
        long end = System.currentTimeMillis();

        System.out.println("latencia: (end - init) = " + (end - init) + "ms");

    }

    public static void main(String[] args) throws IOException {
        httpTransport();
        System.out.println("end");
    }
}
