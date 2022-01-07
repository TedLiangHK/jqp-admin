package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

@Data
public class FormRef extends BaseData {
    //主表单
    private Long formId;
    //关联页面
    private String refPageCode;
    //关联字段
    private String refField;
    //序号
    private Integer seq;
}
