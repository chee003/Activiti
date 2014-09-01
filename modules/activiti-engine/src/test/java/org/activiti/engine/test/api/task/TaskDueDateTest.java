/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.api.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 */
public class TaskDueDateTest extends PluggableActivitiTestCase {
	
	@Override
	protected void tearDown() throws Exception {
		
		for (Task task : taskService.createTaskQuery().list()) {
			taskService.deleteTask(task.getId(), true);
		}
		
	  super.tearDown();
	}

	 /**
   * See http://jira.codehaus.org/browse/ACT-2089
   */
  public void testDueDateSortingWithNulls() {
  	Date now = processEngineConfiguration.getClock().getCurrentTime();
  	
  	// 4 tasks with a due date
  	Task task0 = createTask("task0", new Date(now.getTime() + (4L * 7L * 24L * 60L * 60L * 1000L))); // 4 weeks in future
  	Task task1 = createTask("task1", new Date(now.getTime() + (2 * 24L * 60L * 60L * 1000L))); // 2 days in future
  	Task task2 = createTask("task2", new Date(now.getTime() + (7L * 24L * 60L * 60L * 1000L))); // 1 week in future
  	Task task3 = createTask("task3", new Date(now.getTime() + (24L * 60L * 60L * 1000L))); // 1 day in future
  	
  	// 2 tasks without a due date
  	createTask("task4", null);
  	createTask("task5", null);
  	
  	assertEquals(6, taskService.createTaskQuery().count());
  	
  	// Sorting on due date asc should put the nulls at the end
  	List<Task> tasks = taskService.createTaskQuery().orderByDueDateNullsLast().asc().list();
  	
  	for (int i=0; i<4; i++) {
  		assertNotNull(tasks.get(i).getDueDate());
  	}
  	
  	assertEquals("task3", tasks.get(0).getName());
  	assertEquals("task1", tasks.get(1).getName());
  	assertEquals("task2", tasks.get(2).getName());
  	assertEquals("task0", tasks.get(3).getName());
  	assertNull(tasks.get(4).getDueDate());
  	assertNull(tasks.get(5).getDueDate());
  	
  	// The same, but now desc
  	tasks = taskService.createTaskQuery().orderByDueDateNullsLast().desc().list();
  	
  	for (int i=0; i<4; i++) {
  		assertNotNull(tasks.get(i).getDueDate());
  	}
  	
  	assertEquals("task0", tasks.get(0).getName());
  	assertEquals("task2", tasks.get(1).getName());
  	assertEquals("task1", tasks.get(2).getName());
  	assertEquals("task3", tasks.get(3).getName());
  	assertNull(tasks.get(4).getDueDate());
  	assertNull(tasks.get(5).getDueDate());
  	
  	// The same but now nulls first
  	tasks = taskService.createTaskQuery().orderByDueDateNullsFirst().asc().list();
   	
   	assertNull(tasks.get(0).getDueDate());
   	assertNull(tasks.get(1).getDueDate());
   	assertEquals("task3", tasks.get(2).getName());
   	assertEquals("task1", tasks.get(3).getName());
   	assertEquals("task2", tasks.get(4).getName());
   	assertEquals("task0", tasks.get(5).getName());
   	
   	// And now desc
   	tasks = taskService.createTaskQuery().orderByDueDateNullsFirst().desc().list();
  	
  	assertNull(tasks.get(0).getDueDate());
  	assertNull(tasks.get(1).getDueDate());
  	assertEquals("task0", tasks.get(2).getName());
  	assertEquals("task2", tasks.get(3).getName());
  	assertEquals("task1", tasks.get(4).getName());
  	assertEquals("task3", tasks.get(5).getName());
   	
  }
  
  private Task createTask(String name, Date dueDate) {
  	Task task = taskService.newTask();
  	task.setName(name);
  	task.setDueDate(dueDate);
  	taskService.saveTask(task);
  	return task;
  }

}