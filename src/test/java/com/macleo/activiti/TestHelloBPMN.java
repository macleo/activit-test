package com.macleo.activiti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:test-activiti.cfg.xml" })
public class TestHelloBPMN extends PluggableActivitiTestCase{

	//@Test
	@SuppressWarnings("unused")
	@Deployment(resources = {"test-diagrams/Hello2.bpmn"})
	public void testStartProcess(){
		//ProcessInstance mProcessInstance = runtimeService.startProcessInstanceByKey("hello");
		runtimeService.startProcessInstanceByKey("hello");
		//apply user
		Task mTask = taskService.createTaskQuery().singleResult();
		assertTrue("张三".equals(mTask.getAssignee()));
		//System.out.println("hello");
		//complete task
		taskService.complete(mTask.getId());
		
		//部门领导审批
		mTask = taskService.createTaskQuery().singleResult();
		taskService.complete(mTask.getId());
		
		List<Task> tasks = taskService.createTaskQuery().list();
		List<String> taskNames = new ArrayList<String>();
		for(Task t: tasks){
			taskNames.add(t.getAssignee());
		}
		assertTrue(taskNames.contains("张三"));
		//assertTrue(taskNames.contains("李四"));
	}
	
	@Deployment(resources = {"test-diagrams/Hello2.bpmn"})
	  public void testTask() {
	    try {
	    	 runtimeService.startProcessInstanceByKey("hello");
	        fail();
	      } catch (Throwable e) {
	        assertTrue(e instanceof NullPointerException);
	      }
	      
	  }
}
