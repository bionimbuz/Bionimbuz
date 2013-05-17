package br.unb.cic.bionimbus.p2p.messages;

import java.io.IOException;
import java.util.Arrays;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.plugin.PluginInfo;

import org.junit.Test;

public class BulkMessageTest {

	@Test
	public void testBulkMessage() throws JsonGenerationException, JsonMappingException, IOException {
		BulkMessage m = new BulkMessage();
		m.setPeerID("929202");
		m.setHost(new Host("localhost", 9090));
		
		PluginInfo p = new PluginInfo();
		p.setId("101");
		
		m.setPluginList(Arrays.asList(p));
		
		ObjectMapper mapper = new ObjectMapper();
		String raw = mapper.writeValueAsString(m);
		System.out.println(raw);
		
		BulkMessage message = mapper.readValue(raw.getBytes(), BulkMessage.class);
		
		System.out.println(message.getPluginList().isEmpty());
		
		
	}
}
