package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import com.jqp.admin.page.constants.Whether;
import lombok.Data;

@Data
public class FormField extends InputField{
    //表单id
    private Long formId;
    //是否禁用
    private String disabled;
}
