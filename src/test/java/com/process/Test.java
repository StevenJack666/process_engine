//package com.process.process;
//
//import org.camunda.bpm.engine.RuntimeService;
//import org.camunda.bpm.engine.test.Deployment;
//import org.junit.platform.commons.logging.Logger;
//import org.junit.platform.commons.logging.LoggerFactory;
//
//import java.util.Map;
//
//
//public class Test {
//
//
//    private static final Logger logger = LoggerFactory.getLogger(Test.class);
//
//    @Rule
//    public ActivitiRule activitiRule = new ActivitiRule("activiti.cfg.xml");
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process.bpmn20.xml"})
//    public void testStartProcess(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("key1", "value1");
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", params);
//        logger.info("processInstance = {}", processInstance);
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process.bpmn20.xml"})
//    public void testStartProcessId(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        ProcessDefinition processDefinition =
//                activitiRule.getRepositoryService().createProcessDefinitionQuery().singleResult();
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("key1", "value1");
//        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), params);
//        logger.info("processInstance = {}", processInstance);
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process.bpmn20.xml"})
//    public void testStartProcessInstanceBuilder(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        ProcessDefinition processDefinition =
//                activitiRule.getRepositoryService().createProcessDefinitionQuery().singleResult();
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("key1", "value1");
//        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder();
//        ProcessInstance processInstance = processInstanceBuilder.businessKey("businessKey001").processDefinitionId(processDefinition.getId()).variables(params).start();
//        logger.info("processInstance = {}", processInstance);
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process.bpmn20.xml"})
//    public void testVariables(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("key1", "value1");
//        params.put("key2", "value2");
//        params.put("key3", "value3");
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", params);
//        logger.info("processInstance = {}", processInstance);
//        runtimeService.setVariable(processInstance.getId(), "key1", "value1_");
//        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
//        logger.info("variables = {}", variables);
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process.bpmn20.xml"})
//    public void testProcessInstanceQuery(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
//        logger.info("processInstance = {}", processInstance);
//
//        ProcessInstance processInstance1 =
//                runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process.bpmn20.xml"})
//    public void testExecutionQuery(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
//        logger.info("processInstance = {}", processInstance);
//        List<Execution> executions = runtimeService.createExecutionQuery().listPage(0, 100);
//        for (Execution execution : executions) {
//            logger.info("execution = {}", execution);
//        }
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process-trigger.bpmn20.xml"})
//    public void testTrigger(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
//        Execution execution = runtimeService.createExecutionQuery().activityId("someTask").singleResult();
//        logger.info("someTask前 = {}", execution);
//        runtimeService.trigger(execution.getId());
//        execution = runtimeService.createExecutionQuery().activityId("someTask").singleResult();
//        logger.info("someTask后 = {}", execution);
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process-signal-received.bpmn20.xml"})
//    public void testSignalEventReceived(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
//        Execution execution =
//                runtimeService.createExecutionQuery().signalEventSubscriptionName("my-signal").singleResult();
//        logger.info("execution = {}", execution);
//
//        runtimeService.signalEventReceived("my-signal");
//        execution = runtimeService.createExecutionQuery().signalEventSubscriptionName("my-signal").singleResult();
//        logger.info("execution = {}", execution);
//
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process-message-received.bpmn20.xml"})
//    public void testMessageEventReceived(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
//        Execution execution =
//                runtimeService.createExecutionQuery().messageEventSubscriptionName("my-message").singleResult();
//        logger.info("execution = {}", execution);
//
//        runtimeService.messageEventReceived("my-message", execution.getId());
//        execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("my-message").singleResult();
//        logger.info("execution = {}", execution);
//    }
//
//    @Test
//    @Deployment(resources = {"diagrams/my-process-message.bpmn20.xml"})
//    public void testMessage(){
//        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("my-message");
//        logger.info("processInstance = {}", processInstance);
//
//    }
//
//}
