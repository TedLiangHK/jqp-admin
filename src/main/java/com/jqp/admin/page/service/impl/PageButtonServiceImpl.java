package com.jqp.admin.page.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.page.constants.ActionType;
import com.jqp.admin.page.data.BaseButton;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.page.service.PageButtonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("pageButtonService")
public class PageButtonServiceImpl implements PageButtonService {

    @Resource
    private FormService formService;
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
            iframe.put("height","100%");
            dialog.put("body",iframe);

            btn.put("dialog",dialog);
        }else if(ActionType.Ajax.equals(baseButton.getOptionType())){
            btn.put("actionType","ajax");
            btn.put("api",baseButton.getOptionValue());

            String confirmText = StrUtil.isBlank(baseButton.getConfirmText()) ? "确定" + baseButton.getLabel()+"操作吗?" : baseButton.getConfirmText();
            btn.put("confirmText",confirmText);
        }
        return btn;
    }
}
