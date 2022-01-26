package com.jqp.admin.page.service;

import com.jqp.admin.common.BaseData;
import com.jqp.admin.page.data.BaseButton;
import com.jqp.admin.page.data.Form;
import com.jqp.admin.page.data.PageButton;
import sun.security.krb5.internal.TGSRep;

import java.util.Map;

public interface FormService {
    void save(Form form);
    Form get(Long id);
    Form get(String code);

    Map<String,Object> getFormJson(String code, BaseButton button);

    <T extends BaseData> T getObj(T t,String formCode);
}
