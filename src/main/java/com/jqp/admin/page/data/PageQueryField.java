package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

/***
 * 页面查询字段
 */
@Data
public class PageQueryField extends BaseData {
    //关联页面id
    private Long pageId;
    //字段
    private String field;
    //标签
    private String label;
    //参数值
    private String value;
    //参数值2,日期的第二个输入框
    private String value2;
    //操作类型
    private String opt;
    //是否隐藏
    private String hidden;
    //必填,目前必填的都是模板的值
    private String must;
    //数据类型
    private String type;
    //输入类型
    private String inputType;
    //输入类型2
    private String inputType2;
    //格式化
    private String format;

    //日期表达式1
    private String dateExpress;
    //日期表达式2
    private String dateExpress2;

    //选项sql
    private String optionSql;

}
