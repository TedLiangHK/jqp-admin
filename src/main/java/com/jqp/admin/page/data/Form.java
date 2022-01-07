package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Form extends BaseData {
    //编号
    private String code;
    //名称
    private String name;
    //主表
    private String tableName;
    //js
    private String js = "";

    //初始化接口
    private String initApi;
    //保存接口
    private String api;

    //字段列表
    private List<FormField> formFields = new ArrayList<>();

    //表单关联
    private List<FormRef> formRefs = new ArrayList<>();

    //弹出层大小
    private String size = "default";

    //字段宽度
    private Integer fieldWidth;
}
