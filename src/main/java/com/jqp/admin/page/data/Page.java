package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Page extends BaseData {
    //唯一编码
    private String code;
    //名称
    private String name;
    //查询sql
    private String querySql = "";
    //页面类型,list,tree
    private String pageType;
    //排序
    private String orderBy;
    //js脚本
    private String js = "";

    //查询条件
    private List<PageQueryField> queryFields = new ArrayList<>();
    //查询结果
    private List<PageResultField> resultFields = new ArrayList<>();
    //页面按钮
    private List<PageButton> pageButtons = new ArrayList<>();

    //页面关联
    private List<PageRef> pageRefs = new ArrayList<>();

    //名称字段
    private String labelField;
    //值字段
    private String valueField;

    //宽度
    private Integer width;
}
