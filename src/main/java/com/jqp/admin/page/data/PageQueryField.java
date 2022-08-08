package com.jqp.admin.page.data;

import com.jqp.admin.common.annotations.OrderBy;
import lombok.Data;

/***
 * 页面查询字段
 */
@Data
@OrderBy
public class PageQueryField extends InputField {
    //关联页面id
    private Long pageId;
    //操作类型
    private String opt;
    //是否关联字段
    private String ref;
}
