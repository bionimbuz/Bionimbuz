package br.unb.cic.bionimbus.p2p.plugin.proxy;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.plugin.PluginTaskRunner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.ClientResponse;
import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;

import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.TypeReference;

import static com.google.common.collect.ImmutableMap.of;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

public class ProxyClientStub {

	private final String address;
	private final int port;

	private final ScheduledExecutorService executor;
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();

	public static final int INTERVAL = 30;
	private File file;
    private final String BASE_URL;

    public ProxyClientStub(String address, int port, ScheduledExecutorService executor) {
		this.address = address;
		this.port = port;
		this.executor = executor;
        BASE_URL = "http://" + address + ":" + port + "/proxy";
	}

	public void eventLoop() throws Exception {

        executor.scheduleAtFixedRate(new Runnable(){
            @Override
            public void run() {
                try {

                    String data = doGET(BASE_URL);
                    if (data != null && data.length() > 0) {
                    	//TODO reativar este sub-projeto de conexão
//                        ObjectMapper mapper = new ObjectMapper();
//                        List<RequestMessage> requests = mapper.readValue(data, new TypeReference<List<RequestMessage>>() { });
//                        System.out.println(requests);
//                        for (RequestMessage req : requests){
//                            execute(req);
//                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    private String doGET(String url) {
        Client c = Client.create();
        WebResource r = c.resource(url);
        return r.get(String.class);
    }

    //TODO reativar este sub-projeto
//    private String doPOST(String url, Map<String, ResponseMessage> formMap) throws IOException {
//
//        Client c = Client.create();
//        c.resource(url);
//
//        MultivaluedMap<String,String> formData = new MultivaluedMapImpl();
//        for (Map.Entry<String, ResponseMessage> e: formMap.entrySet()) {
//            String value = serialize(e.getValue());
//            formData.add(e.getKey(), value);
//        }
//
//        String response = c.resource(url).type("application/x-www-form-urlencoded").post(String.class, formData);
//        System.out.println(response);
//        return response;
//    }

    String serialize(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

    public String uploadFileToProxy(File f) {
        String formName = "file";
        String fileURL = "http://" + address + ":" + port + "/upload";
        FormDataMultiPart form = new FormDataMultiPart().field(formName, f, MediaType.MULTIPART_FORM_DATA_TYPE);
        WebResource webResource = Client.create().resource(fileURL);
        return webResource.type(MULTIPART_FORM_DATA)
                   .accept(TEXT_PLAIN)
                   .post(String.class, form);
    }

    public String downloadFileFromProxy(String filename) throws IOException {
        String fileURL = "http://" + address + ":" + port + "/upload";
        Client c = Client.create();
        WebResource r = c.resource(fileURL + "?filename=" + filename);
        String content = r.get(String.class);

        String outputFile = UUID.randomUUID().toString();
        Files.write(content.getBytes(), new File("/home/edward/tmp" + File.separator + outputFile));
        return outputFile;
    }


    private void sleep(TimeUnit timeUnit, int amount) {
		try {
			timeUnit.sleep(amount);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

    //TODO reativar este sub-projeto
//	private void execute(RequestMessage request) throws Exception {
//
//		if (request.getCommand() == GET_INFO) {
//			PluginInfo info = new LinuxGetInfo().call();
//            ResponseMessage<PluginInfo> response = new ResponseMessage(request.getId(), GET_INFO, info);
//
//            Map<String, ResponseMessage> map = Maps.newHashMap();
//            map.put("data", response);
//            String result = doPOST(BASE_URL + "/info", map);
//            System.out.println(result);
//		}
////
////		if (command.startsWith("SAVE-FILE")) {
////
////			String[] split = command.split("#");
////			String filePath = split[1];
////			System.out.println("File path received: " + filePath);
////			File file = new File(filePath + ".received");
////			PluginFile pFile = new PluginFile();
////			pFile.setPath(file.getName());
////			pFile.setSize(file.length());
////
////            String absolutePath = new File(LinuxGetInfo.PATH).getAbsolutePath();
////            String name = file.getName();
////
////            String filenameReceived = downloadFileFromProxy(name);
////
////            Files.copy(new File(filenameReceived), new File(absolutePath + File.separator + name));
////
////			ObjectMapper mapper = new ObjectMapper();
////			return "SAVE-FILE#" + mapper.writeValueAsString(pFile);
////		}
////
////		if (command.startsWith("GET-FILE")) {
////
////			String[] split = command.split("#");
////			String filePath = split[1];
////
////			System.out.println("File path sent: " + filePath);
////
////            String result = uploadFileToProxy(new File(filePath));
////
////
////			return "GET-FILE#" + result;
////
////		}
////
////		if (command.startsWith("RUN-TASK")) {
////
//////            return executorService.submit(new PluginTaskRunner(this, task, service, getP2P().getConfig().getServerPath()));
////
////		}
////
////		return "NO DEFINED";
//
//    }
}