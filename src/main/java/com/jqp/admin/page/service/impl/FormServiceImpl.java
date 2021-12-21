package com.jqp.admin.page.service.impl;

import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.data.Form;
import com.jqp.admin.page.data.FormField;
import com.jqp.admin.page.service.FormService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class FormServiceImpl implements FormService {
    @Resource
    JdbcService jdbcService;
    @Override
    public void save(Form form) {
        jdbcService.saveOrUpdate(form);
        jdbcService.update("delete from form_field where form_id = ? ",form.getId());
        int seq = 0;
        for(FormField formField:form.getFormFields()){
            formField.setFormId(form.getId());
            formField.setSeq(seq);
            jdbcService.saveOrUpdate(formField);
        }
    }

    @Override
    public Form get(Long id) {
        Form form = jdbcService.getById(Form.class, id);
        List<FormField> formFields = jdbcService.find(FormField.class, "formId", id);
        form.setFormFields(formFields);
        return form;
    }
}
