package com.jqp.admin.page.data;

import com.jqp.admin.common.BaseData;
import lombok.Data;

/***
 * @date 2022-02-18 09:28:40
 * @remark 系统菜单
 */
@Data
public class SysMenu extends BaseData {
    //父菜单
    private Long parentId;
    //菜单编号
    private String menuCode;
    //菜单名称
    private String menuName;
    //菜单类型
    private String menuType;
    //菜单地址
    private String url;
    //序号
    private Integer seq;
    //图标
    private String icon;
    //是否按钮
    private String whetherButton;
}