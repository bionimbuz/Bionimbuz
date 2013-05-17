package br.unb.cic.bionimbus.p2p.udp;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UdpTransportTest {

	public static void main(String[] args) throws InterruptedException, IOException {
					
		final UdpServer server = new UdpServer();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}).start();
		

		UdpClient client = new UdpClient();
		for (int i = 0; i < 10; i++){
			client.sendAsyncMessage("localhost", UdpServer.DEFAULT_PORT, new WireMessage(10, "fast message number = " + i));
		}
		
		// espera um pouco...
		TimeUnit.SECONDS.sleep(3);
		
		client.stop();
		server.stop();
	}
}
