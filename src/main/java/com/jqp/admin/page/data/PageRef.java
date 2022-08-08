package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import com.jqp.admin.common.annotations.OrderBy;
import lombok.Data;

/***
 * @date 2022-02-25 10:22:41
 * @remark 页面关联
 */
@Data
@OrderBy
public class PageRef extends BaseData {
    //关联类型
    private String refType;
    //关联字段
    private String refField;
    //关联页面编号
    private String refPageCode;
    //序号
    private Integer seq;
    //页面id
    private Long pageId;
    //名称
    private String refName;
}