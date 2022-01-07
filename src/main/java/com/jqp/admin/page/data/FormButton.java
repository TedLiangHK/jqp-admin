package com.jqp.admin.page.data;

import com.jqp.admin.page.constants.Whether;
import lombok.Data;

@Data
public class FormButton extends BaseButton {
    //表单id
    private Long formId;
    //是否关闭
    private String close = Whether.NO;
}
