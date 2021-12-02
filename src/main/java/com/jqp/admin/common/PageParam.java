package com.jqp.admin.common;

import lombok.Data;

import java.util.HashMap;

/***
 * 分页参数
 */
public class PageParam extends HashMap<String,Object> {
    public int getPage() {
        return  (int) super.get("page");
    }

    public int getPerPage() {
        return (int) super.get("perPage");
    }

    public String getStr(String key){
        Object o = super.get(key);
        return o == null ? null : o.toString();
    }
}
