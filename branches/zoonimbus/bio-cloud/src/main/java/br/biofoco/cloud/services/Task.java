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

public class Task {
	
	enum State {
		PENDING,
		RUNNING,
		ABORTED,
		COMPLETED;
	}
	
	private long taskId;
	
	private long serviceId;
	
	private State state;
	
	private List<String> args;

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long id) {
		this.taskId = id;
	}

	public long getServiceId() {
		return serviceId;
	}

	public void setServiceId(long serviceId) {
		this.serviceId = serviceId;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public boolean isRunning() {	
		return state == State.RUNNING;
	}

	public boolean isCompleted() {
		return state == State.COMPLETED;
	}

	public boolean isAborted() {
		return state == State.ABORTED;
	}

	public TaskResult execute() throws Exception {
		return null;
	}
}
