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

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.primitives.Longs;

public class ServiceInfo {

	private long id;
	private String name;
	private List<String> arguments;
	private List<String> input;
	private List<String> output;
	private String info;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getArguments() {
		return arguments;
	}
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
	public List<String> getInput() {
		return input;
	}
	public void setInput(List<String> input) {
		this.input = input;
	}
	public List<String> getOutput() {
		return output;
	}
	public void setOutput(List<String> output) {
		this.output = output;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}	
	
	@Override
	public int hashCode() {
		return Longs.hashCode(id);
	}
	
	@Override
	public boolean equals(Object object) {
		
		if (this == object)
			return true;
		if (!(object instanceof ServiceInfo))
			return false;
		
		ServiceInfo other = (ServiceInfo) object;
		
		return id == other.id;
	}
	
	@Override
	public String toString() {
		
		return Objects.toStringHelper(ServiceInfo.class)
			   .add("id", id)
			   .add("name", name)
			   .toString();		
	}
	
}
