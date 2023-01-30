package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import com.jqp.admin.common.annotations.OrderBy;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.rbac.service.InputParam;
import lombok.Data;

import java.beans.Transient;

/***
 * 页面结果字段
 */
@Data
@OrderBy
public class PageResultField extends BaseData implements InputParam {
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
    //序号
    private int seq;

    @Override
    @Transient
    public String getMust() {
        return Whether.NO;
    }

    @Override
    @Transient
    public String getComponentType() {
        return null;
    }
}
