package com.macleo.activiti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class TestVariable extends PluggableActivitiTestCase {

	
	
	 /**
	   * 查看流程定义
	   * 流程定义 ProcessDefinition
	   * id : {key}:{version}:{随机值}
	   * name ： 对应流程文件process节点的name属性
	   * key ： 对应流程文件process节点的id属性
	   * version ： 发布时自动生成的。如果是第一发布的流程，veresion默认从1开始；如果当前流程引擎中已存在相同key的流程，则找到当前key对应的最高版本号，在最高版本号上加1
	   */
	public void testQueryProcessDefinition() throws Exception {
	  // 获取仓库服务对象
	  RepositoryService repositoryService = processEngine.getRepositoryService();
	  // 获取流程定义查询对象
	  ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
	  // 配置查询对象
	  processDefinitionQuery
	      //添加过滤条件
//	     .processDefinitionName(processDefinitionName)
//	     .processDefinitionId(processDefinitionId)
//	     .processDefinitionKey(processDefinitionKey)
	     //分页条件
//	     .listPage(firstResult, maxResults)
	      //排序条件
	     .orderByProcessDefinitionVersion().desc();
	  /**
	   * 执行查询
	   * list : 执行后返回一个集合
	   * singelResult 执行后，首先检测结果长度是否为1，如果为一则返回第一条数据；如果不唯一，抛出异常
	   * count： 统计符合条件的结果数量
	   */
	  List<ProcessDefinition> pds = processDefinitionQuery.list();
	  // 遍历集合，查看内容
	  for (ProcessDefinition pd : pds) {
	   System.out.print("id:" + pd.getId() +",");
	   System.out.print("name:" + pd.getName() +",");
	   System.out.print("key:" + pd.getKey() +",");
	   System.out.println("version:" + pd.getVersion());
	  }
	 }
	   
	   
	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	@Deployment(resources = { "test-diagrams/vacation.bpmn" })
	public void testStartProcess1() {
		// ProcessInstance mProcessInstance =
		// runtimeService.startProcessInstanceByKey("hello");
		String initiator = "macleo";
		Map params = new HashMap();
		params.put("initiator", initiator);
		runtimeService.startProcessInstanceByKey("outgoing", params);
		// apply user
		Task mTask = taskService.createTaskQuery().singleResult();
		assertTrue(initiator.equals(mTask.getAssignee()));
		// System.out.println("hello");
		// complete task
		taskService.complete(mTask.getId());

		// 部门领导审批
		params.clear();
		params.put("leaderComment", "同意");
		// params.put("leaderid", 11);
		mTask = taskService.createTaskQuery().singleResult();
		taskService.complete(mTask.getId(), params);

		// 老板审批
		params.clear();
		params.put("bossComment", "同意");
		// params.put("bossid", 3);
		mTask = taskService.createTaskQuery().singleResult();
		taskService.complete(mTask.getId(), params);

		// 销假

		params.clear();
		params.put("initiator", initiator);
		mTask = taskService.createTaskQuery().singleResult();
		taskService.complete(mTask.getId(), params);
		/*
		 * List<Task> tasks = taskService.createTaskQuery().list(); List<String>
		 * taskNames = new ArrayList<String>(); for(Task t: tasks){
		 * taskNames.add(t.getAssignee()); }
		 * assertTrue(taskNames.contains("张三"));
		 * //assertTrue(taskNames.contains("李四"));
		 */
	}

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	@Deployment(resources = { "test-diagrams/vacation.bpmn" })
	public void testStartProcess2() {
		// ProcessInstance mProcessInstance =
		// runtimeService.startProcessInstanceByKey("hello");
		String initiator = "macleo";
		Map params = new HashMap();
		params.put("initiator", initiator);
		runtimeService.startProcessInstanceByKey("outgoing", params);
		// apply user
		Task mTask = taskService.createTaskQuery().singleResult();
		assertTrue(initiator.equals(mTask.getAssignee()));
		// System.out.println("hello");
		// complete task
		taskService.complete(mTask.getId());

		// 部门领导审批
		params.clear();
		params.put("leaderComment", "不同意");
		// params.put("leaderid", 11);
		mTask = taskService.createTaskQuery().singleResult();
		taskService.complete(mTask.getId(), params);

		// 重新申请
		params.clear();
		params.put("initiator", initiator);
		params.put("initiatorComment", "重新申请");
		// params.put("initiatorComment", "撤销申请");
		// params.put("bossid", 3);
		mTask = taskService.createTaskQuery().singleResult();
		taskService.complete(mTask.getId(), params);

		// 老板审批
		params.clear();
		params.put("bossComment", "同意");
		// params.put("bossid", 3);
		mTask = taskService.createTaskQuery().singleResult();
		taskService.complete(mTask.getId(), params);

		// 销假

		params.clear();
		params.put("initiator", initiator);
		mTask = taskService.createTaskQuery().singleResult();
		taskService.complete(mTask.getId(), params);
	}

	@SuppressWarnings("unused")
	@Deployment(resources = { "test-diagrams/vacation.bpmn" })
	public void viewVar() throws Exception {
		String processInstanceId = "1901";
		Task task = taskService.createTaskQuery().taskAssignee("user").processInstanceId(processInstanceId).singleResult();
		System.out.println("taskName:" + task.getName());
		// String variableName = "请假人";
		// String val = (String)taskService.getVariable(task.getId(),
		// variableName );
		Map<String, Object> vars = taskService.getVariables(task.getId());
		for (String variableName : vars.keySet()) {
			String val = (String) vars.get(variableName);
			System.out.println(variableName + " = " + val);
		}
	}
}
