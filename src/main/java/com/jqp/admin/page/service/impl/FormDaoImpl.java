package com.jqp.admin.page.service.impl;

import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.data.Form;
import com.jqp.admin.page.data.FormButton;
import com.jqp.admin.page.data.FormField;
import com.jqp.admin.page.data.FormRef;
import com.jqp.admin.page.service.FormDao;
import com.jqp.admin.page.service.PageButtonDao;
import com.jqp.admin.page.service.PageDao;
import com.jqp.admin.util.SeqComparator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * com.jqp.admin.page.service.impl
 *
 * @author Leo Liu
 * @created 2022/4/7 5:46 PM
 */
@Component
public class FormDaoImpl implements FormDao {
    @Resource
    JdbcService jdbcService;

    @Override
    @Transactional
    public void save(Form form) {
        jdbcService.saveOrUpdate(form);
        Collections.sort(form.getFormButtons(), SeqComparator.instance);
        Collections.sort(form.getFormFields(), SeqComparator.instance);
        Collections.sort(form.getFormRefs(), SeqComparator.instance);

        jdbcService.delete("delete from form_field where form_id = ? ", form.getId());
        int seq = 0;
        for (FormField item : form.getFormFields()) {
            item.setId(null);
            item.setFormId(form.getId());
            item.setSeq(++seq);
            jdbcService.saveOrUpdate(item);
        }

        jdbcService.delete("delete from form_ref where form_id = ? ", form.getId());
        seq = 0;
        for (FormRef item : form.getFormRefs()) {
            item.setId(null);
            item.setFormId(form.getId());
            item.setSeq(++seq);
            jdbcService.saveOrUpdate(item);
        }

        jdbcService.delete("delete from form_button where form_id = ? ", form.getId());
        seq = 0;
        for (FormButton item : form.getFormButtons()) {
            item.setId(null);
            item.setFormId(form.getId());
            item.setSeq(++seq);
            jdbcService.saveOrUpdate(item);
        }
    }

    @Override
    public void save(Form form, String oldCode) {
        save(form);
    }

    @Override
    public Form get(Long id) {
        Form            form       = jdbcService.getById(Form.class, id);
        List<FormField> formFields = jdbcService.find(FormField.class, "formId", id);
        form.setFormFields(formFields);

        List<FormRef> formRefs = jdbcService.find(FormRef.class, "formId", id);
        form.setFormRefs(formRefs);

        List<FormButton> formButtons = jdbcService.find(FormButton.class, "formId", id);
        form.setFormButtons(formButtons);
        return form;
    }

    @Override
    public Form get(String code) {
        Form form = jdbcService.findOne(Form.class, "code", code);
        if (form == null) {
            return null;
        }
        List<FormField> formFields = jdbcService.find(FormField.class, "formId", form.getId());
        form.setFormFields(formFields);

        List<FormRef> formRefs = jdbcService.find(FormRef.class, "formId", form.getId());
        form.setFormRefs(formRefs);

        List<FormButton> formButtons = jdbcService.find(FormButton.class, "formId", form.getId());
        form.setFormButtons(formButtons);
        return form;
    }

    @Override
    public void del(Form form) {
        if (form == null || form.getId() == null) {
            return;
        }
        //删除表单时， 未删除页面上关联此表单的按钮
        jdbcService.delete(form.getId(), "form");
    }

    @Override
    public void delCache(Form form) {

    }
}
