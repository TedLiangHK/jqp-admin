package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

@Data
public class FormRef extends BaseData {
    //主表单
    private Long formId;
    //关联表单
    private Long refId;
    //关联字段
    private String refField;
    //关联表单类型   单表,列表
    private String refType;
}
