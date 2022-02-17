package com.jqp.admin.page.controller;

import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/menu")
public class MenuController {
    @Resource
    private JdbcService jdbcService;

    @Resource
    private PageService pageService;

    @RequestMapping("/initButtons/{menuId}")
    public Result initButtons(@PathVariable Long menuId){
        Map<String, Object> menu = jdbcService.getById("sys_menu", menuId);
        String code = (String) menu.get("code");
        String url = (String) menu.get("url");
        String prefix = "/crud/";
        if(StringUtils.isBlank(url) || !url.startsWith(prefix)){
            return Result.error("只能初始化地址为["+prefix+"]开头的菜单");
        }
        String pageCode = url.substring(url.indexOf(prefix)+prefix.length()+1);
        Page page = pageService.get(pageCode);
        if(page == null){
            return Result.error("页面["+pageCode+"]不存在");
        }
        String maxCode = jdbcService.findOneForObject("select max(code) from sys_menu where parent_id = ? ", String.class, menu.get("id"));
        if(StringUtils.isBlank(maxCode)){
            maxCode = code+"01";
        }else{
            code.substring(code.length()-2);
        }
        List<PageButton> pageButtons = page.getPageButtons();

        return Result.success();
    }
}
