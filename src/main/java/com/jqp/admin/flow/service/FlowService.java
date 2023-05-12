package com.jqp.admin.flow.service;

import com.jqp.admin.flow.data.Flow;
import com.jqp.admin.flow.data.FlowDeploy;
import com.jqp.admin.flow.data.FlowInstance;
import com.jqp.admin.flow.data.FlowInstanceTask;
import com.jqp.admin.flow.graphData.Node;

public interface FlowService {
    FlowDeploy deploy(Flow flow);
    void start(String flowCode,Long id,String name);
    FlowInstanceTask createTask(FlowDeploy flowDeploy, FlowInstance flowInstance, Long id, Node node);
}
