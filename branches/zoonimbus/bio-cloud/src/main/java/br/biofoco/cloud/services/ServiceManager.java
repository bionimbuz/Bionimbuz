/**
 * Copyright (C) 2011 University of Brasilia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.biofoco.cloud.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class ServiceManager {
	
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private final ConcurrentMap<Long, ServiceInfo> serviceMap = new ConcurrentHashMap<Long, ServiceInfo>(100);

	private Map<String, String> resultMap = new HashMap<String, String>();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);
	
	private static final String serviceDir = "services";
	
	private static ServiceManager INSTANCE = null;
	
	private ServiceManager() {
		init();
	}
	
	//TODO: refactor to use Guice singleton
	public static synchronized ServiceManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ServiceManager();
		
		return INSTANCE;
	}
		
	private void loadServices() throws IOException {
		
		File dir = new File(serviceDir);
		
		for (File file : dir.listFiles()){
			if (file.isFile() && file.canRead() && file.getName().endsWith(".json")) {
				ObjectMapper mapper = new ObjectMapper();
				ServiceInfo service = mapper.readValue(file, ServiceInfo.class);
				serviceMap.put(service.getId(), service);
			}
		}
		
		
	}
	
	public void init() {
		LOGGER.debug("Initializing service map");
		try {
			loadServices();
			LOGGER.debug(serviceMap.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		executor.scheduleWithFixedDelay(new ServiceInspector(), 30, 30, TimeUnit.SECONDS);
	}
	
	public void stop() {
		executor.shutdown();
	}
	
	public Set<ServiceInfo> listServices() {
		return ImmutableSet.copyOf(serviceMap.values());
	}
	
	private final class ServiceInspector implements Runnable {
		
		public void run() {
			try {
				loadServices();
			}
			catch (IOException ie) {			
				ie.printStackTrace();
			}	
		}
	}

	/**
	 * 
	 * @param id the service
	 * @return the task id
	 */
	public String invokeService(String id) {
		LOGGER.debug("Invoking service " + id);
		
		ServiceInfo service = serviceMap.get(Long.parseLong(id));
		
		if (service != null) {			
			try {
				
				Process p = Runtime.getRuntime().exec(service.getName());				
				InputStream is = p.getInputStream();
				
				int c;
				StringBuilder sb = new StringBuilder();
				while ((c = is.read()) != -1) {
					sb.append((char) c);
				}
				
				String taskID = UUID.randomUUID().toString();
				
				String result = sb.toString();
				
				System.out.println(result);
				
				resultMap.put(taskID, result);
				
				return taskID;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return "-1";
	}

	public String getTaskResult(String taskID) {
		
		String result = resultMap.get(taskID);
		if (result == null)
			result = "result unavailable!";
		return result;
	}
}
