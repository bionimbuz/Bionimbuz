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

package br.biofoco.cloud.config;

import com.beust.jcommander.Parameter;

public class HostConfig {
	
	public static final int DEFAULT_HTTP_PORT = 9999;
	
	@Parameter(names="-httpPort", required=false)
	private int httpPort = DEFAULT_HTTP_PORT;
			
	public int getHttpPort() {
		return httpPort;
	}
		
	@Override
	public String toString() {
		return "httpPort=" + httpPort;
	}
	
}
