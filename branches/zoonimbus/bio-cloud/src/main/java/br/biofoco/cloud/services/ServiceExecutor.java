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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class ServiceExecutor {
 
	private final BlockingQueue<Future<TaskResult>> completed = new LinkedBlockingQueue<Future<TaskResult>>();
	private final ExecutorService executor = Executors.newFixedThreadPool(2);			
	private final CompletionService<TaskResult> taskExecutor = new ExecutorCompletionService<TaskResult>(executor, completed);
		
	public void submitTask(Task task) {
		taskExecutor.submit(new TaskExecutor(task));
	}
	
	public void stop() {
		executor.shutdownNow();
	}
	
	public class TaskExecutor implements Callable<TaskResult> {
		
		private final Task task;
		
		public TaskExecutor(Task task){
			this.task = task;
		}

		@Override
		public TaskResult call() throws Exception {
			return task.execute();
		}		
	}
}
