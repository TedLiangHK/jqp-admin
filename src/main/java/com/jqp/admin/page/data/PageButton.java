package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

@Data
public class PageButton extends BaseData {
    //页面id
    private Long pageId;
    //按钮名称
    private String label;
    //按钮位置  页面按钮(新增,导出),行按钮(编辑,删除)
    private String buttonLocation;
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
}
