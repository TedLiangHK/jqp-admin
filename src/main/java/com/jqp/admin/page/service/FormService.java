package com.jqp.admin.page.service;

import com.jqp.admin.page.data.Form;

public interface FormService {
    void save(Form form);
    Form get(Long id);
}
