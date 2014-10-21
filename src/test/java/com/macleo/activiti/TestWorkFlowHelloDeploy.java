package com.macleo.activiti;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml" })
public class TestWorkFlowHelloDeploy {

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();
	@Autowired
	ProcessEngine processEngine;

	private static String bpmn_location = "test-diagrams/";
	// @Before
	public void initDeploy() {
		processEngine = ProcessEngines.getDefaultProcessEngine();
	}

	/**
	 * 部署Hello流程
	 * 
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = { "classpath:diagrams/Hello.bpmn" })
	public void deployHello() throws Exception {
		// 创建流程发布配置对象
		RepositoryService repositoryService = processEngine.getRepositoryService();
		DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
		// 在配置对象中添加发布的规则信息
		deploymentBuilder.name("Hello流程") // 当前部署流程的“显示别名”
				.addClasspathResource(bpmn_location+"vacation.bpmn")// 设置规则文件
				.addClasspathResource(bpmn_location+"vacation.png"); // 设置流程图片
		// 调用方法完成流程的发布
		Deployment d = (Deployment) deploymentBuilder.deploy();
		System.out.println(d.toString());
	}

	@Test
	@Deployment(resources = { "classpath:test-diagrams/vacation.bpmn" })
	public void startProcess() throws Exception {
		// RepositoryService repositoryService =
		// processEngine.getRepositoryService();
		// repositoryService.createDeployment().addInputStream("leave.bpmn", new
		// FileInputStream(filename)).deploy();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("initiator", "mark");
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("outgoing", params);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " " + processInstance.getProcessDefinitionId());
	}

	@Test
	public void delDeployment() throws Exception {
		// 获取仓库服务对象
		RepositoryService repositoryService = processEngine.getRepositoryService();
		// 删除发布信息
		String deploymentId = "601";
		// 普通删除，如果当前规则下有正在执行的流程，则抛异常
		// repositoryService.deleteDeployment(deploymentId);
		// 级联删除,会删除和当前规则相关的所有信息，包括历史
		repositoryService.deleteDeployment(deploymentId, true);
	}

	/*
	 * @Test public void startProcess() throws Exception { // 获取仓库服务对象
	 * RepositoryService repositoryService =
	 * processEngine.getRepositoryService(); // 删除发布信息 String deploymentId =
	 * "1"; // 普通删除，如果当前规则下有正在执行的流程，则抛异常 //
	 * repositoryService.deleteDeployment(deploymentId); //
	 * 级联删除,会删除和当前规则相关的所有信息，包括历史
	 * repositoryService.deleteDeployment(deploymentId, true); }
	 */

	@Test
	public void viewHistoryProcess() {
		String id = "2501";
		HistoryService historyService = processEngine.getHistoryService();
		HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(id).singleResult();
		System.out.println("Process instance end time: " + historicProcessInstance.getEndTime());
		List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().finished().list();

		for (HistoricTaskInstance m : list) {
			System.out.println(m.getAssignee());
		}
	}

	@Test
	public void queryProcessDefinition() throws Exception {
		// 获取流程定义对应的查询对象
		RepositoryService repositoryService = processEngine.getRepositoryService();
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		// 添加查询参数
		String processDefinitionId = "leave:1:104";
		query
		// 过滤条件
		// .processDefinitionKey(processDefinitionKey)
		// .processDefinitionName(processDefinitionName )
		.processDefinitionId(processDefinitionId)
		// 分页条件
		// .listPage(firstResult, maxResults)
				// 排序条件
				.orderByProcessDefinitionVersion().desc();
		// 执行查询得到结果
		ProcessDefinition pd = query.singleResult();
		// 遍历结果，显示相关信息
		System.out.println("id:" + pd.getId() + ",name:" + pd.getName() + ",key:" + pd.getKey() + ",version:" + pd.getVersion());

		// 查询指定流程定义下的活动信息
		ProcessDefinitionImpl pdImpl = (ProcessDefinitionImpl) repositoryService.getProcessDefinition(pd.getId());
		System.out.println(pdImpl.getActivities());
	}

	// 2,.查看任务
	// 2.1查看私有任务
	@Test
	public void queryMyTask() throws Exception {
		// 创建服务对象的实例
		TaskService taskService = processEngine.getTaskService();
		String assignee = "mark";
		// String assignee = "user";
		// 查询
		// 1)创建查询对象
		TaskQuery qo = taskService.createTaskQuery();
		// 2)配置查询参数
		qo
		// 过滤条件
		.taskAssignee(assignee)
		// 排序条件
				.orderByTaskCreateTime().desc();
		// 分页条件
		// .listPage(firstResult, maxResults)

		// 3)执行查询
		List<Task> tasks = qo.list();
		for (Task task : tasks) {
			System.out.println("taskId:" + task.getId() + ",taskName:" + task.getName() + ",assigne:" + task.getAssignee() + ",createTime:" + task.getCreateTime());
		}
	}

	/**
	 * 查看当前流程执行的位置
	 * 
	 * @throws Exception
	 */
	@Test
	public  void getProcessPic() throws Exception {
		// String taskId =
		// "2901";//getRequest().getParameter("taskId");3016,552,3020
		String procDefId = "hello:1:804";
		RepositoryService repositoryService = processEngine.getRepositoryService();
		// String procDefId = getRequest().getParameter("procDefId");
		ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
		String diagramResourceName = procDef.getDiagramResourceName();
		InputStream imageStream = repositoryService.getResourceAsStream(procDef.getDeploymentId(), diagramResourceName);
		//getRequest().setAttribute("inputStream", imageStream);
		//return SUCCESS;
		List<String> names = repositoryService.getDeploymentResourceNames(procDef.getDeploymentId());
		String imageName = null;
		for (String name : names) {
			if (name.indexOf(".png") >= 0) {
				imageName = name;
			}
		}
		if (imageName != null) {
			// System.out.println(imageName);
			File f = new File("e:/" + imageName);
			// 通过部署ID和文件名称得到文件的输入流
			//InputStream in = repositoryService.getResourceAsStream(deploymentId, imageName);
			FileUtils.copyInputStreamToFile(imageStream, f);
		}
	}

	
	/**
	 * 取出图片，没有红框
	 * @throws Exception
	 */
	@Test
	public void viewImage() throws Exception {
		// 创建仓库服务对对象
		RepositoryService repositoryService = processEngine.getRepositoryService();
		// 从仓库中找需要展示的文件
		String deploymentId = "1";
		List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);
		String imageName = null;
		for (String name : names) {
			if (name.indexOf(".png") >= 0) {
				imageName = name;
			}
		}
		System.out.println(System.getProperty("user.dir"));
		if (imageName != null) {
			// System.out.println(imageName);
			File f = new File("e:/" + imageName);
			// 通过部署ID和文件名称得到文件的输入流
			InputStream in = repositoryService.getResourceAsStream(deploymentId, imageName);
			FileUtils.copyInputStreamToFile(in, f);
		}
	}
}
