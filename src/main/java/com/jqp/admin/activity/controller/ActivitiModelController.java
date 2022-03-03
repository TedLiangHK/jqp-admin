package com.jqp.admin.activity.controller;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jqp.admin.activity.constants.ModelExtraField;
import com.jqp.admin.activity.data.ModelData;
import com.jqp.admin.activity.service.ActivityService;
import com.jqp.admin.common.Result;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.data.Obj;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.util.TemplateUtil;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Zhan Xinjian
 * @date 2021/1/18
 */

@RestController
@RequestMapping("/admin/models")
public class ActivitiModelController {


    @Resource
    ProcessEngine processEngine;
    @Resource
    ObjectMapper objectMapper;

    @Resource
    ActivityService activityService;

    @Resource
    private JdbcService jdbcService;

    /**
     * 新建一个空模型
     */
    @RequestMapping("/save")
    public Result save(@RequestBody ModelData modelData) throws IOException {
        //response.sendRedirect("/activity-web/modeler.html?modelId="+id);
        Model model = activityService.saveOrUpdate(modelData);
        if(model == null){
            return Result.error("流程编码不能重复");
        }
        return Result.success();
    }
    /**
     * 获取
     */
    @RequestMapping("/get")
    public Result get(String id) throws IOException {
        //response.sendRedirect("/activity-web/modeler.html?modelId="+id);
        if(StringUtils.isBlank(id)){
            return Result.success(new ModelData());
        }
        ModelData modelData = activityService.getModelData(id);
        return Result.success(modelData);
    }

    @RequestMapping("/delete/{id}")
    public Result delete(@PathVariable String id){
        processEngine.getRepositoryService().deleteModel(id);
        return Result.success();
    }

    /**
     * 发布模型为流程定义
     */
    @RequestMapping("/deploy/{modelId}")
    public Result deploy(@PathVariable String modelId) throws Exception {

        //获取模型
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return Result.error("模型数据为空，请先设计流程并成功保存，再进行发布。");
        }
        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            return Result.error("数据模型不符要求，请至少设计一条主线流程。");
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .addString("metaInfo", modelData.getMetaInfo())
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);

        return Result.success();
    }

    /**
     *  启动流程
     */
    @RequestMapping("/start/{keyName}")
    @ResponseBody
    public Object start(@PathVariable String keyName) {

        ProcessDefinition processDefinition = activityService.getLastVersion(keyName);
        JSONObject metaInfo = activityService.getMetaInfo(processDefinition.getDeploymentId());
        String bizId = UUID.randomUUID().toString();
        Map<String,Object> params = new HashMap<>();
        params.put("id",bizId);
        if(metaInfo != null){
            String initSql = metaInfo.getStr(ModelExtraField.InitSql);
            if(StringUtils.isNotBlank(initSql)){
                Map<String, Object> sqlParams = new HashMap<>();
                SessionContext.putUserSessionParams(sqlParams);
                initSql = TemplateUtil.getValue(initSql,sqlParams);

                Map<String, Object> data = jdbcService.findOne(initSql);
                if(data != null){
                    params.putAll(data);
                }
            }
        }
        ProcessInstance process = processEngine.getRuntimeService().startProcessInstanceByKey(keyName,bizId,params);
        processEngine.getRuntimeService().setProcessInstanceName(process.getId(),processDefinition.getName()+"-"+bizId);
        return Result.success(process.getId());
    }

    /**
     *  提交任务
     */
    @RequestMapping("/run")
    @ResponseBody
    public Object run(String processInstanceId) {
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        task.getAssignee();
        processEngine.getTaskService().complete(task.getId());

        return "SUCCESS";
    }

    @RequestMapping("/getDeploymentMeta/{id}")
    public Object getDeploymentMeta(@PathVariable String id){
        JSONObject metaInfo = activityService.getMetaInfo(id);
        if(metaInfo == null){
            metaInfo = new JSONObject();
        }
        return Result.success(metaInfo);
    }

    @RequestMapping("/getNextNode/{id}")
    public Object getTaskFlows(@PathVariable String id){
        List<SequenceFlow> nodes = activityService.getNextNode(id);
        return Result.success(nodes);
    }

    @RequestMapping("/completeTask/{id}/{result}")
    @Transactional(rollbackFor = Exception.class)
    public Object completeTask(@PathVariable String id,@PathVariable String result){
        Task task = processEngine.getTaskService().createTaskQuery().taskId(id).singleResult();
        if(task == null){
            return Result.error("任务已完成");
        }
        Map<String,Object> params = new HashMap<>();
        params.put("result",result);
        processEngine.getTaskService().complete(id,params);
        return Result.success();
    }


}