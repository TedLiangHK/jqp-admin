package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

/***
 * 页面结果字段
 */
@Data
public class PageResultField extends BaseData {
    //关联页面id
    private Long pageId;
    //字段
    private String field;
    //字段中文
    private String label;
    //宽度
    private Integer width;
    //字段类型
    private String type;
    //格式化
    private String format;
    //是否隐藏
    private String hidden = "NO";
}
