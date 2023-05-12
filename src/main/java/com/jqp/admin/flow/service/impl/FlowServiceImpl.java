package com.jqp.admin.flow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.config.UserSession;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.flow.data.Flow;
import com.jqp.admin.flow.data.FlowDeploy;
import com.jqp.admin.flow.data.FlowInstance;
import com.jqp.admin.flow.data.FlowInstanceTask;
import com.jqp.admin.flow.graphData.Edge;
import com.jqp.admin.flow.graphData.GraphContext;
import com.jqp.admin.flow.graphData.Node;
import com.jqp.admin.flow.service.FlowService;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.service.DicService;
import com.jqp.admin.rbac.data.User;
import com.jqp.admin.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("flowService")
public class FlowServiceImpl implements FlowService {
    @Resource
    private JdbcService jdbcService;
    @Resource
    private DicService dicService;
    @Override
    @Transactional
    public FlowDeploy deploy(Flow flow) {
        FlowDeploy deploy = new FlowDeploy();
        BeanUtil.copyProperties(flow,deploy);
        deploy.setId(null);
        deploy.setFlowId(flow.getId());

        Integer maxVersion = jdbcService.findOneForObject("select max(version) from flow_deploy where flow_id = ? ", Integer.class, flow.getId());
        deploy.setVersion(maxVersion == null ? 1 : maxVersion + 1);
        flow.setLastVersion(deploy.getVersion());

        jdbcService.saveOrUpdate(deploy);
        jdbcService.saveOrUpdate(flow);

        return deploy;
    }

    @Override
    @Transactional
    public void start(String flowCode, Long id, String name) {
        Flow flow = jdbcService.findOne(Flow.class, Flow.Fields.code, flowCode);
        if(flow == null){
            throw new RuntimeException(StrUtil.format("流程【{}】不存在",flowCode));
        }
        FlowInstance flowInstance = jdbcService.findOne(FlowInstance.class, new String[]{
            FlowInstance.Fields.flowId,
            FlowInstance.Fields.refId,
        }, new Object[]{
            flow.getId(),
            id
        });
        FlowDeploy flowDeploy = null;
        if(flowInstance != null){
            flowDeploy = jdbcService.getById(FlowDeploy.class,flowInstance.getFlowDeployId());
        }else{
            if(flow.getLastVersion() == null){
                throw new RuntimeException(StrUtil.format("流程【{}】未部署",flowCode));
            }
            flowDeploy = jdbcService.findOne(FlowDeploy.class,new String[]{
                FlowDeploy.Fields.flowId,
                FlowDeploy.Fields.version
            },new Object[]{
                flow.getId(),
                flow.getLastVersion()
            });
            flowInstance = new FlowInstance();
            flowInstance.setFlowId(flow.getId());
            flowInstance.setFlowDeployId(flowDeploy.getId());
            flowInstance.setFlowCode(flowDeploy.getCode());
            flowInstance.setFlowName(flowDeploy.getName());
            flowInstance.setRefId(id);
            flowInstance.setName(name);
            flowInstance.setStartTime(new Date());
            flowInstance.setViewForm(flowDeploy.getViewForm());
            try{
                UserSession userSession = SessionContext.getSession();
                User user = SessionContext.getUser();
                flowInstance.setCreateUserId(user.getId());
                flowInstance.setCreateUserName(user.getName());
                flowInstance.setEnterpriseId(userSession.getEnterpriseId());
            }catch (Exception e){}
        }

        GraphContext graphContext = new GraphContext(flowDeploy.getGraphData());
        if(flowInstance.getId() == null){
            Node startNode = graphContext.getStartNode();
            List<Edge> edges = graphContext.getEdges(startNode.getId());
            if(edges.size() != 1){
                throw new RuntimeException("流程配置错误,开始节点有且只能有一个流转");
            }
            Node initNode = graphContext.getNode(edges.get(0).getTargetNodeId());
            flowInstance.setCurrentTaskId(initNode.getId());
            flowInstance.setCurrentTaskName(initNode.getText().getValue());
        }
        Map<String, Object> obj = jdbcService.getById(flowDeploy.getTableName(), id);

        //当前节点
        Node currentNode = graphContext.getNode(flowInstance.getCurrentTaskId());

        //单据状态
        String statusField = StringUtil.toFieldColumn(flowDeploy.getStatusField());
        String objStatus = (String) obj.get(statusField);

        //如果当前节点配置了状态,需要校验状态
        if(StringUtils.isNotBlank(currentNode.getProperties().getStatus())){
            String[] arr = StringUtil.splitStr(currentNode.getProperties().getStatus(), ",");
            boolean flag = false;
            for(String s:arr){
                if(s.equals(objStatus)){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                throw new RuntimeException("当前单据状态已经发生变化,请刷新后查看");
            }
        }

        jdbcService.saveOrUpdate(flowInstance);

        //后面的节点
        List<Edge> edges = graphContext.getEdges(currentNode.getId());

        //暂时不判断条件流转
        if(edges.size() != 1){
            throw new RuntimeException("流程配置错误,发起节点有且只能有一个流转");
        }
        Edge edge = edges.get(0);

        FlowInstanceTask initTask = createTask(flowDeploy, flowInstance, id, currentNode);
        initTask.setOrderStatus(objStatus);
        initTask.setOrderStatusName(dicService.getLabel(flowDeploy.getStatusDic(),objStatus));
        initTask.setEndTime(new Date());
        initTask.setPass(Whether.YES);
        initTask.setEdgeId(edge.getId());
        initTask.setEdgeName(edge.getText().getValue());

        try{
            User user = SessionContext.getUser();

            initTask.setAuditUserId(user.getId());
            initTask.setAuditUserName(user.getName());
        }catch (Exception e){}

        jdbcService.saveOrUpdate(initTask);

        String nextStatus = edge.getProperties().getStatus();
        if(StringUtils.isNotBlank(nextStatus)){
            obj.put(statusField,nextStatus);
        }
        //下一个节点
        Node nextNode = graphContext.getNode(edge.getTargetNodeId());
        flowInstance.setCurrentTaskId(nextNode.getId());
        flowInstance.setCurrentTaskName(nextNode.getText().getValue());

        jdbcService.saveOrUpdate(flowInstance);
        jdbcService.saveOrUpdate(obj,flowDeploy.getTableName());

        FlowInstanceTask task = createTask(flowDeploy,flowInstance,id,nextNode);

        jdbcService.saveOrUpdate(task);

    }

    @Override
    public FlowInstanceTask createTask(FlowDeploy flowDeploy, FlowInstance flowInstance, Long id, Node node){
        FlowInstanceTask task = new FlowInstanceTask();
        task.setTaskId(flowInstance.getCurrentTaskId());
        task.setFlowCode(flowDeploy.getCode());
        task.setFlowInstanceId(flowInstance.getId());
        task.setFlowCode(flowDeploy.getCode());
        task.setFlowName(flowDeploy.getName());
        task.setFlowInstanceName(flowInstance.getName());
        task.setTableName(flowDeploy.getTableName());

        if(StringUtils.isNotBlank(node.getProperties().getViewForm())){
            task.setViewForm(node.getProperties().getViewForm());
        }else{
            task.setViewForm(flowDeploy.getViewForm());
        }
        task.setStartTime(new Date());
        task.setTaskName(node.getText().getValue());
        task.setCandidateUserIds(node.getProperties().getUsers());
        task.setCandidatePositionCodes(node.getProperties().getPositionCodes());
        task.setOrderStatusDic(flowDeploy.getStatusDic());
        task.setRefId(id);
        try{
            UserSession userSession = SessionContext.getSession();
            User user = SessionContext.getUser();
            task.setEnterpriseId(userSession.getEnterpriseId());

            task.setCreateUserId(user.getId());
            task.setCreateUserName(user.getName());
        }catch (Exception e){}
        return task;
    }
}
