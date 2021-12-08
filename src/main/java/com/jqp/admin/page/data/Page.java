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

    //表
    private String tables;

    //查询条件
    private List<PageQueryField> queryFields = new ArrayList<>();
    //查询结果
    private List<PageResultField> resultFields = new ArrayList<>();
}
