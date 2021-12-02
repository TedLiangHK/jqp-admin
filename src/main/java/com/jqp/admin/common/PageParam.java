package com.jqp.admin.common;

import lombok.Data;

/***
 * 分页参数
 */
@Data
public class PageParam {
    private int page;
    private int limit;
}
