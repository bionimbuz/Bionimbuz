package br.unb.cic.bionimbus.p2p.plugin.proxy;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;
import br.unb.cic.bionimbus.p2p.P2PService;


public class ServerTest {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		
		String configFile = System.getProperty("config.file", "exported-folders/conf/server.json");
		BioNimbusConfig config = BioNimbusConfigLoader.loadHostConfig(configFile);
		
	    ExecutorService executor = Executors.newCachedThreadPool();
		
		RemotePlugin remote = new RemotePlugin(new P2PService(config), executor);
		
		//ACTIONS
		System.out.println("Retorno: " + remote.startGetInfo().get());		
//		remote.saveFile("/tmp/teste.txt");
//		remote.getFile(null, null, "aaa", "/tmp/teste.txt");
	}
	
}
