package com.jqp.admin.page.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.page.constants.ActionType;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.page.service.PageButtonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class PageButtonServiceImpl implements PageButtonService {

    @Resource
    private FormService formService;
    @Override
    public Map<String,Object> getButton(PageButton pageButton){
        Map<String,Object> btn = new HashMap<>();
        btn.put("type","button");
        btn.put("label",pageButton.getLabel());
        btn.put("level",pageButton.getLevel());
        if(StrUtil.isNotBlank(pageButton.getJsRule())){
            btn.put("disabledOn",pageButton.getJsRule());
        }
        if(ActionType.PopForm.equals(pageButton.getOptionType())){
            btn.put("actionType","dialog");

            Map<String, Object> dialog = formService.getFormJson(pageButton.getOptionValue(),pageButton);
            btn.put("dialog",dialog);
        }else if(ActionType.Ajax.equals(pageButton.getOptionType())){
            btn.put("actionType","ajax");
            btn.put("api",pageButton.getOptionValue());

            String confirmText = StrUtil.isBlank(pageButton.getConfirmText()) ? "确定" + pageButton.getLabel()+"操作吗?" : pageButton.getConfirmText();
            btn.put("confirmText",confirmText);
        }
        return btn;
    }
}
