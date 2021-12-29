package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import com.jqp.admin.page.constants.Whether;
import lombok.Data;

@Data
public class FormField extends BaseData {
    //表单id
    private Long formId;
    //字段
    private String field;
    //标签
    private String label;
    //默认值
    private String value;
    //宽度
    private Integer width;
    //字段类型
    private String type;
    //格式化
    private String format;
    //是否隐藏
    private String hidden = Whether.NO;
    //序号
    private int seq;
    //是否多选
    private String multi = Whether.NO;
    //是否必填
    private String must = Whether.NO;
}
