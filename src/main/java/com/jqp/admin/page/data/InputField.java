package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import com.jqp.admin.page.constants.Whether;
import lombok.Data;

@Data
public class InputField extends BaseData {
    //字段
    private String field;
    //标签
    private String label;
    //参数值
    private String value;
    //是否隐藏
    private String hidden;
    //必填,目前必填的都是模板的值
    private String must = Whether.NO;
    //数据类型
    private String type;
    //格式化
    private String format;

    //宽度 1-12
    private Integer width;

    //日期表达式1
    private String dateExpress;

    //选项sql
    private String optionSql;

    //序号
    private int seq;

    //组件类型
    private String componentType;
    //是否多选
    private String multi;
}