package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

@Data
public class BaseButton extends BaseData {
    //按钮名称
    private String label;
    //操作类型  ajax/弹出表单/二次确认
    private String optionType;
    //ajax请求url/表单编号
    private String optionValue;
    //级别
    private String level;
    //二次确认提示
    private String confirmText;
    //序号
    private int seq;
    //规则
    private String jsRule;
    //编号,用于按钮权限
    private String code;
}
