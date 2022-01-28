package com.jqp.admin.page.data;

import lombok.Data;

/***
 * 页面查询字段
 */
@Data
public class PageQueryField extends InputField {
    //关联页面id
    private Long pageId;
    //操作类型
    private String opt;
    //是否关联字段
    private String ref;
}
