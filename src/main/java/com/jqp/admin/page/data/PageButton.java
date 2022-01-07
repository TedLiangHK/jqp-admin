package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

@Data
public class PageButton extends BaseButton {
    //页面id
    private Long pageId;
    //按钮位置  页面按钮(新增,导出),行按钮(编辑,删除)
    private String buttonLocation;
}
