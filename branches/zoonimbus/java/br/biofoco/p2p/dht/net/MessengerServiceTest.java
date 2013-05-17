package br.biofoco.p2p.dht.net;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import br.unb.cic.bionimbus.p2p.dht.MessengerService;
import br.unb.cic.bionimbus.p2p.dht.PeerConfig;
import br.unb.cic.bionimbus.p2p.transport.WireFormatException;
import br.unb.cic.bionimbus.p2p.transport.WireMessage;

public class MessengerServiceTest extends TestCase {
	
	public static void main(String[] args) throws IOException, InterruptedException, WireFormatException {
		
		PeerConfig config = new PeerConfig();
		config.setPort(9191);
		MessengerService service = new MessengerService(config);
		
//		service.addListener(new MyListener());
		
		service.start();
		
		TimeUnit.SECONDS.sleep(5);
		
		System.out.println(service.sendMessage("Hello world!"));		
	}

	public static class MyListener implements MessengerListener {

		@Override
		public void onMessage(String message) {
			System.out.println("Message: " + message);			
		}

	}
}
