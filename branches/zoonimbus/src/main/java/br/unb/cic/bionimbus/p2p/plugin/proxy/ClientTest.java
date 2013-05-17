package br.unb.cic.bionimbus.p2p.plugin.proxy;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class ClientTest {
	
	public static void main(String[] args) throws Exception {

        String configFile = System.getProperty("config.file", "exported-folders/conf/client.json");
        BioNimbusConfig config = BioNimbusConfigLoader.loadHostConfig(configFile);
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        String host = config.getProxyHost();
        int port = config.getProxyPort();
		
		ProxyClientStub client = new ProxyClientStub(host, port, executor);
		client.eventLoop();

        //String filenameName = client.uploadFileToProxy(new File("/home/edward/AsyncOp.java"));
        //String filenameReceived = client.downloadFileFromProxy(filenameName);
        //System.out.println(filenameReceived);
		
		executor.shutdownNow();
	}

}
