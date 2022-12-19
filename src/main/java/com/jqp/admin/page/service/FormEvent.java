package com.jqp.admin.page.service;

import com.jqp.admin.common.Result;
import com.jqp.admin.page.data.Form;

import java.util.Map;

public interface FormEvent {
    Result beforeSave(Map<String,Object> obj, String tableName, Form form);
    Result afterSave(Map<String,Object> obj, String tableName, Form form);
    void init(Map<String,Object> obj,Form form);
}
