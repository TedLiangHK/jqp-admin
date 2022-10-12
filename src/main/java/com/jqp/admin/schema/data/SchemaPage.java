package com.jqp.admin.schema.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

/***
 * @date 2022-09-02 09:41:04
 * @remark 页面配置
 */
@Data
public class SchemaPage extends BaseData {
    //编号
    private String code;
    //名称
    private String name;
    //类型
    private String schemaPageType;
    //页面配置
    private String schemaJson;
    //查询sql
    private String querySql;
}
