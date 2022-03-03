package com.jqp.admin.activity.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

import java.util.Date;

/***
 * 审核记录
 */
@Data
public class AuditRecord extends BaseData {
    //表
    private String tableName;
    //主表id
    private Long refId;
    //流程实例id
    private String processInstanceId;
    //流程名称
    private String processInstanceName;
    //流程部署id
    private String deploymentId;
    //流程定义id
    private String processDefinitionId;
    //当前任务id
    private String taskId;
    //当前任务名称
    private String taskName;
    //流程key
    private String processKey;
    //审核结果
    private String resultName;
    //图片
    private String imgs;
    //附件
    private String files;
    //备注
    private String remark;
    //前状态值
    private String prevStatus;
    //前状态名称
    private String prevStatusName;
    //后状态值
    private String nextStatus;
    //后状态名称
    private String nextStatusName;
    //状态字典
    private String statusDic;
    //开始时间
    private Date startTime;
    //结束时间
    private Date endTime;
}
