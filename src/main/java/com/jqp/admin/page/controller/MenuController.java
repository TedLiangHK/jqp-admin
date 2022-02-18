package com.jqp.admin.page.controller;

import com.jqp.admin.common.BaseData;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.ActionType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.*;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/menu")
public class MenuController {
    @Resource
    private JdbcService jdbcService;

    @Resource
    private PageService pageService;

    @Resource
    private FormService formService;

    @RequestMapping("/initButtons/{menuId}")
    public Result initButtons(@PathVariable Long menuId){
        SysMenu menu = jdbcService.getById(SysMenu.class, menuId);
        String code = menu.getMenuCode();
        String url = menu.getUrl();
        String prefix = "/crud/";
        if(StringUtils.isBlank(url) || !url.startsWith(prefix)){
            return Result.error("只能初始化地址为["+prefix+"]开头的菜单");
        }
        String pageCode = url.substring(url.indexOf(prefix)+prefix.length());
        Page page = pageService.get(pageCode);
        if(page == null){
            return Result.error("页面["+pageCode+"]不存在");
        }
        Integer seq = jdbcService.findOneForObject("select max(seq) from sys_menu where parent_id = ? ", Integer.class, menu.getId());
        if(seq == null){
            seq = 0;
        }
        List<PageButton> pageButtons = page.getPageButtons();
        List<BaseData> btns = new ArrayList<>();
        List<SysMenu> btnMenus = new ArrayList<>();
        Set<String> btnIds = new HashSet<>();
        //页面按钮
        for(PageButton btn:pageButtons){
            if(StringUtils.isNotBlank(btn.getCode()) || btnIds.contains("p"+btn.getId())){
                continue;
            }
            SysMenu sysMenu = new SysMenu();
            sysMenu.setMenuName(page.getName()+"-"+btn.getLabel());
            sysMenu.setSeq(++seq);
            sysMenu.setMenuCode(code+StringUtil.getAddCode(sysMenu.getSeq()+"","0",2));
            sysMenu.setWhetherButton(Whether.YES);
            sysMenu.setParentId(menu.getId());
            sysMenu.setMenuType(menu.getMenuType());
            btnMenus.add(sysMenu);
            btn.setCode(sysMenu.getMenuCode());
            btns.add(btn);
            btnIds.add("p"+btn.getId());

            if(ActionType.PopForm.equals(btn.getOptionType())){
                Form form = formService.get(btn.getOptionValue());
                if(form != null){
                    List<FormButton> formButtons = form.getFormButtons();
                    //页面表单按钮
                    for(FormButton formButton:formButtons){
                        if(StringUtils.isNotBlank(formButton.getCode()) || btnIds.contains("f"+formButton.getId())){
                            continue;
                        }

                        sysMenu = new SysMenu();
                        sysMenu.setMenuName(form.getName()+"-"+formButton.getLabel());
                        sysMenu.setSeq(++seq);
                        sysMenu.setMenuCode(code+StringUtil.getAddCode(sysMenu.getSeq()+"","0",2));
                        sysMenu.setWhetherButton(Whether.YES);
                        sysMenu.setParentId(menu.getId());
                        sysMenu.setMenuType(menu.getMenuType());
                        btnMenus.add(sysMenu);
                        formButton.setCode(sysMenu.getMenuCode());
                        btns.add(formButton);
                        btnIds.add("f"+formButton.getId());
                    }
                    //表单关联页面按钮
                    List<FormRef> formRefs = form.getFormRefs();
                    for(FormRef formRef:formRefs){
                        Page refPage = pageService.get(formRef.getRefPageCode());
                        for(PageButton refPageBtn:refPage.getPageButtons()){
                            if(StringUtils.isNotBlank(refPageBtn.getCode()) || btnIds.contains("p"+refPageBtn.getId())){
                                continue;
                            }

                            sysMenu = new SysMenu();
                            sysMenu.setMenuName(refPage.getName()+"-"+refPageBtn.getLabel());
                            sysMenu.setSeq(++seq);
                            sysMenu.setMenuCode(code+StringUtil.getAddCode(sysMenu.getSeq()+"","0",2));
                            sysMenu.setWhetherButton(Whether.YES);
                            sysMenu.setParentId(menu.getId());
                            sysMenu.setMenuType(menu.getMenuType());
                            btnMenus.add(sysMenu);
                            refPageBtn.setCode(sysMenu.getMenuCode());
                            btns.add(refPageBtn);
                            btnIds.add("p"+refPageBtn.getId());
                        }
                    }
                }
            }
        }

        jdbcService.transactionOption(() -> {
            jdbcService.bathSaveOrUpdate(btnMenus);
            jdbcService.bathSaveOrUpdate(btns);
        });
        return Result.success();
    }
}
