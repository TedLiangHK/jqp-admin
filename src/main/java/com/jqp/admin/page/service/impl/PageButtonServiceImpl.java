package com.jqp.admin.page.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.page.constants.ActionType;
import com.jqp.admin.page.data.BaseButton;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.page.service.PageButtonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
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
        if(StrUtil.isNotBlank(baseButton.getJsRule())){
            btn.put("disabledOn",baseButton.getJsRule());
        }
        if(ActionType.PopForm.equals(baseButton.getOptionType())){
            btn.put("actionType","dialog");

            Map<String, Object> dialog = formService.getFormJson(baseButton.getOptionValue(),baseButton);
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
