package com.process.service.impl;

import com.process.DTO.RejectBean;
import com.process.service.Process;
import com.process.util.Result;
import com.process.util.ResultFactory;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessImpl implements Process {

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

    /**
     * 任务驳回:回退到上一个节点
     * @param rejectBean
     * @return
     */
    @Override
    public Result rejectToLastNode(RejectBean rejectBean) {
        //获取当前task
        Task task = taskService.createTaskQuery()
                .taskAssignee(rejectBean.getCurrentUserId()) //当前登录用户的id
                .processInstanceId(rejectBean.getProcessInstanceId())
                .singleResult();

        if(task == null){
            return   ResultFactory.buildFailResult("当前用户没有的待执行的任务");
        }
        ActivityInstance
                tree = runtimeService.getActivityInstance(rejectBean.getProcessInstanceId());
        //获取所有已办用户任务节点
        List<HistoricActivityInstance> resultList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(rejectBean.getProcessInstanceId())
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        if (null == resultList || resultList.size() == 0) {
            return ResultFactory.buildFailResult("当前任务无法驳回！");
        }
        //得到第一个任务节点的id
        HistoricActivityInstance historicActivityInstance = resultList.get(0);
        String startActId = historicActivityInstance.getActivityId();
        if (startActId.equals(task.getTaskDefinitionKey())) {
            return ResultFactory.buildFailResult("开始节点无法驳回！");
        }
        //得到上一个任务节点的ActivityId和待办人
        Map<String, String> lastNode = getLastNode(resultList, task.getTaskDefinitionKey());
        if (null == lastNode) {
            return ResultFactory.buildFailResult("回退节点异常！");
        }
        String toActId = lastNode.get("toActId");
        String assignee = lastNode.get("assignee");
        //设置流程中的可变参数
        Map<String, Object> taskVariable = new HashMap<>(2);
        taskVariable.put("user", assignee);
        //taskVariable.put("formName", "流程驳回");
        taskService.createComment(task.getId(), rejectBean.getProcessInstanceId(), "驳回原因:" + rejectBean.getRejectComment());
        runtimeService.createProcessInstanceModification(rejectBean.getProcessInstanceId())
                .cancelActivityInstance(getInstanceIdForActivity(tree, task.getTaskDefinitionKey()))//关闭相关任务
                .setAnnotation("进行了驳回到上一个任务节点操作")
                .startBeforeActivity(toActId)//启动目标活动节点
                .setVariables(taskVariable)//流程的可变参数赋值
                .execute();
        return ResultFactory.buildSuccessResult(null);
    }

    private String getInstanceIdForActivity(ActivityInstance activityInstance, String activityId) {
        ActivityInstance instance = getChildInstanceForActivity(activityInstance, activityId);
        if (instance != null) {
            return instance.getId();
        }
        return null;
    }

    private ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
        if (activityId.equals(activityInstance.getActivityId())) {
            return activityInstance;
        }
        for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
            ActivityInstance instance = getChildInstanceForActivity(childInstance, activityId);
            if (instance != null) {
                return instance;
            }
        }
        return null;
    }

    /**
     * 获取上一节点信息
     * 分两种情况：
     * 1、当前节点不在历史节点里
     * 2、当前节点在历史节点里
     * 比如，resultList={1,2,3}
     * (1)当前节点是4，表示3是完成节点，4驳回需要回退到3
     * (2)当前节点是2，表示3是驳回节点，3驳回到当前2节点，2驳回需要回退到1
     * 其他驳回过的情况也都包含在情况2中。
     *
     * @param resultList        历史节点列表
     * @param currentActivityId 当前待办节点ActivityId
     * @return 返回值：上一节点的ActivityId和待办人（toActId, assignee）
     */
    private static Map<String, String> getLastNode(List<HistoricActivityInstance> resultList, String currentActivityId) {
        Map<String, String> backNode = new HashMap<>();
        //新建一个有序不重复集合
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap();
        for (HistoricActivityInstance hai : resultList) {
            linkedHashMap.put(hai.getActivityId(), hai.getAssignee());
        }
        //分两种情况：当前节点在不在历史节点里面，当前节点在历史节点里
        //情况1、当前节点不在历史节点里
        int originSize = resultList.size();
        int duplicateRemovalSize = linkedHashMap.size();
        //判断历史节点中是否有重复节点
        //if(originSize == duplicateRemovalSize){
        boolean flag = false;
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            if (currentActivityId.equals(entry.getKey())) {
                flag = true;
                break;
            }
        }
//            if(flag){
//                //当前节点在历史节点里：最后一个节点是回退节点
//                return currentNodeInHis(linkedHashMap, currentActivityId);
//            }
        if (!flag) {
            //当前节点不在历史节点里：最后一个节点是完成节点
            HistoricActivityInstance historicActivityInstance = resultList.get(originSize - 1);
            backNode.put("toActId", historicActivityInstance.getActivityId());
            backNode.put("assignee", historicActivityInstance.getAssignee());
            return backNode;
        }
        //}
        //情况2、当前节点在历史节点里（已回退过的）
        return currentNodeInHis(linkedHashMap, currentActivityId);
    }

    private static Map<String, String> currentNodeInHis(LinkedHashMap<String, String> linkedHashMap, String currentActivityId) {
        //情况2、当前节点在历史节点里（已回退过的）
        Map<String, String> backNode = new HashMap<>();
        ListIterator<Map.Entry<String, String>> li = new ArrayList<>(linkedHashMap.entrySet()).listIterator();
        //System.out.println("已回退过的");
        while (li.hasNext()) {
            Map.Entry<String, String> entry = li.next();
            if (currentActivityId.equals(entry.getKey())) {
                li.previous();
                Map.Entry<String, String> previousEntry = li.previous();
                backNode.put("toActId", previousEntry.getKey());
                backNode.put("assignee", previousEntry.getValue());
                return backNode;
            }
        }
        return null;
    }

    /**
     * 回退至开始节点
     * @param rejectBean
     * @return
     */
    @Override
    public Result rejectToFirstNode(RejectBean rejectBean) {
        //String rejectMessage="项目的金额款项结算不正确";
        Task task = taskService.createTaskQuery()
                .taskAssignee(rejectBean.getCurrentUserId()) //当前登录用户的id
                .processInstanceId(rejectBean.getProcessInstanceId())
                .singleResult();


        if(task == null){
            return   ResultFactory.buildFailResult("当前用户没有的待执行的任务");
        }
        ActivityInstance tree = runtimeService.getActivityInstance(rejectBean.getProcessInstanceId());
        List<HistoricActivityInstance> resultList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(rejectBean.getProcessInstanceId())
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        if(null == resultList || resultList.size()<2){
            return ResultFactory.buildFailResult("第一个用户节点无法驳回！");
        }
        //得到第一个任务节点的id
        HistoricActivityInstance historicActivityInstance = resultList.get(0);
        String toActId = historicActivityInstance.getActivityId();
        String assignee = historicActivityInstance.getAssignee();
        //设置流程中的可变参数
        Map<String, Object> taskVariable = new HashMap<>(2);
        taskVariable.put("user", assignee);
        //taskVariable.put("formName", "流程驳回");
        taskService.createComment(task.getId(), rejectBean.getProcessInstanceId(), "驳回原因:" + rejectBean.getRejectComment());
        runtimeService.createProcessInstanceModification(rejectBean.getProcessInstanceId())
                .cancelActivityInstance(getInstanceIdForActivity(tree, task.getTaskDefinitionKey()))//关闭相关任务
                .setAnnotation("进行了驳回到第一个任务节点操作")
                .startBeforeActivity(toActId)//启动目标活动节点
                .setVariables(taskVariable)//流程的可变参数赋值
                .execute();
        return ResultFactory.buildSuccessResult("suc");
    }



}
