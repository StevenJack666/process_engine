package com.process.contoller;

import com.process.DTO.ProcessBean;
import com.process.DTO.ProcessInstanceBean;
import com.process.DTO.RejectBean;
import com.process.DTO.TaskBean;
import com.process.service.Process;
import com.process.util.Result;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * zhangmm
 */
@RestController
public class ProcessController {
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;
    @Autowired
    HistoryService historyService;
    @Autowired
    ProcessEngine processEngine;

    @Autowired
    ProcessEngine engine;

    @Autowired
    Process process;


    /**
     * 查看流程信息
     */
    @GetMapping("/process/list")
    public List<ProcessBean> getProcessList() {
        List<Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();

        List<ProcessBean> resProcess = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ProcessBean processBean = new ProcessBean();

            processBean.setProcessId(list.get(i).getId());
            processBean.setProcessKey(list.get(i).getKey());
            processBean.setDeploymentId(list.get(i).getDeploymentId());
            String aa = list.get(i).getDeploymentId();

            Optional<Deployment> res = deploymentList.stream().filter(vv -> vv.getId().equals(aa)).findFirst();
            if (res.isPresent()) {
                processBean.setName(res.get().getName());
            }
            resProcess.add(processBean);
        }
        return resProcess;
    }

    /**
     * 26     * @Description: 流程定义部署
     * 27     * @Author: zhangmm
     * 28     * @Date: 2023.8
     * 29
     */
    @GetMapping("/deploy")
    public String deploy(String processName) {
        //获取所有的文档列表
        List<Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
        if (deploymentList.stream().filter(vv -> processName.equals(vv.getName())).findFirst().isPresent()) {
            //当前实力名称已经存在
            return null;
        }

        Deployment deploy = repositoryService.createDeployment()
//                .addInputStream("aa", new FileInputStream(""))
                .name(processName)
                .addClasspathResource("BPMN/res_5.bpmn")
                .deploy();
        System.out.println(deploy.getId());
        return deploy.getId();
    }

    /**
     * @Description: 开启一个流程实例
     * @Author: zhangmm
     * @Date: 2023.8
     */
    @GetMapping("/start")
    public String runProcinst(String processKey) {

        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();

        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).getKey());
            System.out.println(list.get(i).getDeploymentId());
            if (list.get(i).getKey().equals("Process_0lgf96b")) {

            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("money", 2001);
//        ProcessInstance apply = runtimeService.startProcessInstanceByKey(processKey, params);
        ProcessInstance apply = runtimeService.startProcessInstanceById(processKey, params);
        return apply.getProcessInstanceId();
    }

    /**
     * @Description: 流程任务查询
     * @Author: zhangmm
     * @Date: 2023.8
     */
    @GetMapping("/taskquery")
    public List<TaskBean> taskQuery(String instanceId, String processKey) {
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey(processKey)
                //查询某个流程下，某个实例的待执行任务
                .processInstanceId(instanceId)
                .list();

        List<TaskBean> list = new ArrayList<>();
        for (Task task : tasks) {
            TaskBean taskBean = new TaskBean();
            taskBean.setAssignee(task.getAssignee());
            taskBean.setName(task.getName());
            taskBean.setId(task.getId());
            taskBean.setTenanted(task.getTenantId());
            list.add(taskBean);
        }
        return list;
    }

    /**
     * @Description: 当前需要处理的任务
     * @Author: zhangmm
     * @Date: 2023.8
     */
    @GetMapping("/mytaskquery")
    public List<HistoricTaskInstance> myTaskQuery(String userId, String instanceId) {
        List<HistoricTaskInstance> instances = engine.getHistoryService().createHistoricTaskInstanceQuery()
                .taskAssignee(userId).processInstanceId(instanceId).unfinished().orderByHistoricActivityInstanceStartTime().asc().list();
        return instances;
    }


    /**
     * @Description: 流程任务委派
     * @Author: zhangmm
     * @Date: 2023.8
     */
    @GetMapping("/taskDelegate")
    public String taskDelegate(String userId, String instanceId) {
        //业务中根据场景选择其他合适的方式
        List<Task> task = taskService.createTaskQuery()
                .taskAssignee(userId)
                .processInstanceId(instanceId)
                .list();
        if (CollectionUtils.isEmpty(task)) {
            //任务不存在
            return "failed";
        }
        Map<String, Object> params = new HashMap<>();
        params.put("approve2", "zhangsan");
//        taskService.complete(task.get(0).getId(), params);
        taskService.delegateTask(task.get(0).getId(), "weipai");
//        taskService.resolveTask();
        return "suc";
    }

    /**
     * @Description: 流程任务执行
     * @Author:  
     * @Date:  
     */
    @GetMapping("/taskComplete")
    public String taskComplete(String userId, String instanceId) {
        //目前lisi只有一个任务，业务中根据场景选择其他合适的方式
        List<Task> task = taskService.createTaskQuery()
                .taskAssignee(userId)
                .processInstanceId(instanceId)
                .list();
        if (CollectionUtils.isEmpty(task)) {
            //任务不存在
            return "failed";
        }
        Map<String, Object> params = new HashMap<>();
        params.put("type", 2);
        taskService.complete(task.get(0).getId(), params);
//        taskService.resolveTask();
        return "suc";
    }

    /**
     * @Description: 流程定义查询:具体某个流程的实例信息
     * @Author:  
     * @Date:  
     */
    @GetMapping("/queryDefine")
    public List<ProcessInstanceBean> queryDefine(String processkey) {
        ProcessDefinition myProcessDefinition =
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(processkey)
                        .latestVersion()
                        .singleResult();

        List<ProcessInstance> processInstances =
                runtimeService.createProcessInstanceQuery()
                        .processDefinitionId(myProcessDefinition.getId())
                        .active() // we only want the unsuspended process instances
                        .list();
        List<ProcessInstanceBean> instanceBeanList = new ArrayList<>();

        for (ProcessInstance instance : processInstances) {
            ProcessInstanceBean instanceBean = new ProcessInstanceBean();
            instanceBean.setProcessId(instance.getProcessDefinitionId());
            instanceBean.setProcessInstanceId(instance.getId());
            instanceBeanList.add(instanceBean);
        }
        return instanceBeanList;
    }

    /**
     * 删除流程定义
     */
    @PostMapping("/deleteDefine")
    public void deleteDefine() {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> definitions = processDefinitionQuery.processDefinitionKey("apply")
                .orderByProcessDefinitionVersion()
                .asc()
                .list();
        ProcessDefinition processDefinition = definitions.get(0);
        if (processDefinition != null) {
            repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
        }
    }

    /**
     * 查询历史信息
     */
    @GetMapping("/queryHistory")
    public void queryHistory() {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        for (HistoricActivityInstance
                instance : list) {
            System.out.println(instance.getActivityId());
            System.out.println(instance.getProcessDefinitionKey());
            System.out.println(instance.getAssignee());
            System.out.println(instance.getStartTime());
            System.out.println(instance.getEndTime());
            System.out.println("=============================");
        }
    }

    /**
     * 任务驳回到上一个节点
     */
    @GetMapping("/rejectPreNode")
    public Result rejectPreNode(String userId, String instanceId) {
        RejectBean rejectBean = new RejectBean();
        rejectBean.setCurrentUserId(userId);
        rejectBean.setProcessInstanceId(instanceId);
        Result result = process.rejectToLastNode(rejectBean);

        return result;
    }


    /**
     * 任务驳回到第一个节点
     */
    @GetMapping("/rejectToFirstNode")
    public Result rejectToFirstNode(String userId, String instanceId) {

        RejectBean rejectBean = new RejectBean();
        rejectBean.setCurrentUserId(userId);
        rejectBean.setProcessInstanceId(instanceId);
        Result result = process.rejectToFirstNode(rejectBean);

        return result;
    }

    /**
     * 判断当前任务是否结束
     *   
     */
    @GetMapping("/ins/status")
    public Boolean currentInsStatus(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            System.out.println("当前实例已经结束了");
            return true;
        } else {
            System.out.println("当前实例正在运转");
            return false;
        }

    }

    /**
     * 启动一个流程实例，并且添加一个业务key
     * 业务key 可以在 act_ru_execution 中看到
     */

    public void startProcInstAddBusinessKey() {
        ProcessInstance apply = runtimeService.startProcessInstanceByKey("apply", "aaaa-scsc-89uc");
        System.out.println(apply.getBusinessKey());
    }

}
