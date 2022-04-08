package com.jqp.admin.page.service;

import com.jqp.admin.common.BaseData;
import com.jqp.admin.page.data.BaseButton;
import com.jqp.admin.page.data.Form;

import java.util.Map;

public interface FormService {
    void save(Form form);
    Form get(Long id);
    Form get(String code);
    void del(Form form);
    //重新生成关联数据
    void reload(Form form);

    Map<String,Object> getFormJson(String code, BaseButton button);
    Map<String,Object> getFormJson(Form form, BaseButton button);

    Map<String,Object> getPageJson(String code, BaseButton button);

    <T extends BaseData> T getObj(T t,String formCode);
}
