package com.jqp.admin.page.data;

import com.jqp.admin.common.annotations.OrderBy;
import lombok.Data;

@Data
@OrderBy
public class FormField extends InputField{
    //表单id
    private Long formId;
    //是否禁用
    private String disabled;
    //校验重复类型
    private String checkRepeatType;
    //校验重复配置
    private String checkRepeatConfig;
    //校验重复提示
    private String checkRepeatTip;
    //表单项校验
    private String validations;
}
