package br.biofoco.cloud.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

public class WSClient {

	public static void main(String[] args) throws IOException {
		final String BASE_URI = "http://localhost:9999";

		Client c = Client.create();
		WebResource service = c.resource(BASE_URI);

		doGet(service);

		uploadFile(service);

		doUpload2();

		doUpload3();

	}

	private static void doUpload3() {

		File file = new File("./pom.xml");

		FormDataMultiPart fdmp = new FormDataMultiPart();
		if (file != null) {
			fdmp.bodyPart(new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
		}
		fdmp.bodyPart(new FormDataBodyPart("name", "ingredientName"));
		fdmp.bodyPart(new FormDataBodyPart("description", "ingredientDesc"));
		WebResource resource = Client.create().resource("http://localhost:9999/upload3");
		ClientResponse response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, fdmp);
		String string = response.getEntity(String.class);

		System.out.println(string);
	}

	private static void doUpload2() {

		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("log4j.properties");
		FormDataMultiPart part = new FormDataMultiPart().field("file", stream,
				MediaType.TEXT_PLAIN_TYPE);

		WebResource resource = Client.create().resource(
				"http://localhost:9999/upload2");
		String response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
				.post(String.class, part);
		System.out.println(response);
	}

	private static void doGet(WebResource service) {
		ClientResponse response = service.path("/services").get(
				ClientResponse.class);
		System.out.println("Response Status : "
				+ response.getEntity(String.class));
	}

	private static void uploadFile(WebResource service) throws IOException {
		ClientResponse response;
		// POST a file
		ByteArrayOutputStream bas = new ByteArrayOutputStream();

		InputStream fis = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("log4j.properties");

		byte[] buf = new byte[1024 * 60];
		int size;
		while ((size = fis.read(buf)) != -1) {
			bas.write(buf, 0, size);
		}

		byte[] file = bas.toByteArray();

		MultiPart multiPart = new MultiPart().bodyPart(new BodyPart(file,
				MediaType.APPLICATION_OCTET_STREAM_TYPE));
		response = service.path("/upload").type("multipart/mixed")
				.post(ClientResponse.class, multiPart);
		System.out.println("Response Status : "
				+ response.getEntity(String.class));
	}

}
