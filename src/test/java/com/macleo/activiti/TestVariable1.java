package com.macleo.activiti;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml" })
public class TestVariable1 {

	@Autowired
	ProcessEngine processEngine;
	 /**
	   * 查看流程定义
	   * 流程定义 ProcessDefinition
	   * id : {key}:{version}:{随机值}
	   * name ： 对应流程文件process节点的name属性
	   * key ： 对应流程文件process节点的id属性
	   * version ： 发布时自动生成的。如果是第一发布的流程，veresion默认从1开始；如果当前流程引擎中已存在相同key的流程，则找到当前key对应的最高版本号，在最高版本号上加1
	   */
	@Test
	public void testQueryProcessDefinition() throws Exception {
		//ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration().buildProcessEngine();
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
	
	 /**
	   * 1.发布流程
	   * 会在三张表中产生数据：
	   * act_ge_bytearray 产生两条数据
	   * act_re_deployment 产生一条数据
	   * act_re_procdef 产生一条数据
	   */
	 @Test
	 public void deploy() throws Exception {
	  // 获取仓库服务
	  RepositoryService repositoryService = processEngine.getRepositoryService();
	  // 创建发布配置对象
	  DeploymentBuilder builder = repositoryService.createDeployment();
	  // 设置发布信息
	  builder.name("外出流程")// 添加部署规则的显示别名
	  .addClasspathResource("test-diagrams/vacation.bpmn");// 添加规则文件
	  //.addClasspathResource("test-diagrams/Leave2.png");// 添加规则图片  不添加会自动产生一个图片不推荐
	  // 完成发布
	  Deployment mDeployment =  builder.deploy();
			  System.out.println(mDeployment);
	 }
	 
		@Test
		public void queryProcessDefinition() throws Exception {
			// 获取流程定义对应的查询对象
			RepositoryService repositoryService = processEngine.getRepositoryService();
			ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
			// 添加查询参数
			String processDefinitionId = "outgoing:1:4";
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
		
		
		/**
		 * 查看当前流程执行的位置
		 * 
		 * @throws Exception
		 */
		@Test
		public  void getProcessPic() throws Exception {
			// String taskId =
			// "2901";//getRequest().getParameter("taskId");3016,552,3020
			String procDefId = "outgoing:1:4";
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
}
