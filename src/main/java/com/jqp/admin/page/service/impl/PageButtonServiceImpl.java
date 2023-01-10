package com.jqp.admin.page.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.page.constants.ActionType;
import com.jqp.admin.page.data.*;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.page.service.PageButtonDao;
import com.jqp.admin.page.service.PageButtonService;
import com.jqp.admin.page.service.PageService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("pageButtonService")
public class PageButtonServiceImpl implements PageButtonService {

    @Resource
    @Lazy
    private FormService formService;

    @Resource
    @Lazy
    private PageService pageService;

    @Resource
    PageButtonDao pageButtonDao;

    @Override
    public Map<String,Object> getButton(BaseButton baseButton){
        Map<String,Object> btn = new HashMap<>();
        btn.put("type","button");
        btn.put("label",baseButton.getLabel());
        btn.put("level",baseButton.getLevel());
//        btn.put("reload","mainTable");
        if(StrUtil.isNotBlank(baseButton.getJsRule())){
            btn.put("disabledOn",baseButton.getJsRule());
        }
        if(ActionType.PopForm.equals(baseButton.getOptionType())){
            btn.put("actionType","dialog");

            Map<String, Object> dialog = formService.getFormJson(baseButton.getOptionValue(),baseButton);
            btn.put("dialog",dialog);
        }else if(ActionType.PopPage.equals(baseButton.getOptionType())){
            btn.put("actionType","dialog");

            Map<String, Object> dialog = formService.getPageJson(baseButton.getOptionValue(),baseButton);
            btn.put("dialog",dialog);
        }else if(ActionType.PopIframe.equals(baseButton.getOptionType())){
            btn.put("actionType","dialog");

            Map<String,Object> dialog = new HashMap<>();
            dialog.put("title",baseButton.getLabel());
            dialog.put("size","full");
            List<Map<String,Object>> dialogButtons = new ArrayList<>();

            dialog.put("actions",dialogButtons);

            Map<String, Object> iframe = new HashMap<>();
            iframe.put("type","iframe");
            iframe.put("src",baseButton.getOptionValue());
            iframe.put("height","calc( 100% - 5px )");
            dialog.put("body",iframe);

            btn.put("dialog",dialog);
        }else if(ActionType.Ajax.equals(baseButton.getOptionType())){
            btn.put("actionType","ajax");
            btn.put("api",baseButton.getOptionValue());

            String confirmText = StrUtil.isBlank(baseButton.getConfirmText()) ? "确定" + baseButton.getLabel()+"操作吗?" : baseButton.getConfirmText();
            btn.put("confirmText",confirmText);
        }else if(ActionType.OpenNew.equals(baseButton.getOptionType())){
            btn.put("actionType","url");
            btn.put("url",baseButton.getOptionValue());
        }
        return btn;
    }


    @Override
    public List<PageButton> byPageCode(String pageCode) {
        Page page = pageService.get(pageCode);
        if (page == null) {
            return new ArrayList<>();
        }
        return pageButtonDao.byPage(page);
    }

    @Override
    public void save(PageButton pageButton) {
        pageButtonDao.save(pageButton);
    }

    @Override
    public List<PageButton> byPageId(Long id) {
        return pageButtonDao.byPageId(id);
    }

    @Override
    public List<PageButton> byPage(Page page) {
        return pageButtonDao.byPage(page);
    }

    @Override
    public List<PageButton> getByForm(Form form) {
        return pageButtonDao.getByForm(form);
    }

    @Override
    public Page getPage(PageButton pageButton){
        return pageService.get(pageButton.getPageId());
    }

    @Override
    public PageButtonData dealPageButton(List<PageButton> pageButtons, boolean isRow) {
        PageButtonData pageButtonData = new PageButtonData();
        if(!isRow){
            pageButtonData.getTopButtons().add("filter-toggler");
        }
        for(PageButton pageButton:pageButtons){
            if(!SessionContext.hasButtonPermission(pageButton.getCode())){
                continue;
            }
            if(isRow){
                if("row".equals(pageButton.getButtonLocation())){
                    pageButtonData.getRowButtons().add(getButton(pageButton));
                }
            }else{
                if("top".equals(pageButton.getButtonLocation())){
                    pageButtonData.getTopButtons().add(getButton(pageButton));
                }else if("bulk".equals(pageButton.getButtonLocation())){
                    pageButtonData.getBulkButtons().add(getButton(pageButton));
                }
            }
        }

        if(!pageButtonData.getBulkButtons().isEmpty()){
            pageButtonData.getTopButtons().add("bulkActions");
        }
        return pageButtonData;
    }
}
