package com.jqp.admin.page.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Opt;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.Form;
import com.jqp.admin.page.data.FormField;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            formField.setId(null);
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

    @Override
    public Form get(String code) {
        Form form = jdbcService.findOne(Form.class,"code",code);
        List<FormField> formFields = jdbcService.find(FormField.class, "formId", form.getId());
        form.setFormFields(formFields);
        return form;
    }


    @Override
    public Map<String, Object> getFormJson(String code, PageButton pageButton) {

        Form f = this.get(code);
        Map<String,Object> form = new HashMap<>();
        form.put("type","form");
        if(StrUtil.isNotBlank(f.getTableName())){
            form.put("initApi",StrUtil.format("post:/admin/common/{}/get",f.getCode())+"?id=${id}");
            form.put("api",StrUtil.format("post:/admin/common/{}/saveOrUpdate",f.getCode()));
        }
        if(StrUtil.isNotBlank(f.getInitApi())){
            form.put("initApi",f.getInitApi());
        }
        if(StrUtil.isNotBlank(f.getApi())){
            form.put("api",f.getApi());
        }

        List<Map<String,Object>> items = new ArrayList<>();

        Map<String,Object> grid = new HashMap<>();
        grid.put("type","grid");
        grid.put("columns",items);


        List<FormField> formFields = f.getFormFields();
        for(FormField field:formFields){
            Map<String,Object> fieldConfig = new HashMap<>();
            fieldConfig.put("name", field.getField());
            fieldConfig.put("label",field.getLabel());
            fieldConfig.put("xs",12);
            fieldConfig.put("sm",6);
            fieldConfig.put("md",4);
            fieldConfig.put("lg",3);
            fieldConfig.put("columnClassName","mb-1");

            if(f.getFieldWidth() != null){
                fieldConfig.put("xs",f.getFieldWidth());
                fieldConfig.put("sm",f.getFieldWidth());
                fieldConfig.put("md",f.getFieldWidth());
                fieldConfig.put("lg",f.getFieldWidth());
            }

            if(field.getWidth() != null){
                fieldConfig.put("xs",field.getWidth());
                fieldConfig.put("sm",field.getWidth());
                fieldConfig.put("md",field.getWidth());
                fieldConfig.put("lg",field.getWidth());
            }

            boolean isMulti = Whether.YES.equals(field.getMulti());

            if(StrUtil.isNotBlank(field.getValue())){
                fieldConfig.put("value",field.getValue());
            }

            if(Whether.YES.equals(field.getMust())){
                fieldConfig.put("required",true);
            }

            if(Whether.YES.equals(field.getHidden())){
                fieldConfig.put("xs",0.0001);
                fieldConfig.put("sm",0.0001);
                fieldConfig.put("md",0.0001);
                fieldConfig.put("lg",0.0001);
                fieldConfig.put("label","");
                fieldConfig.put("type","hidden");
            }else if(DataType.isDate(field.getType())){
                fieldConfig.put("format",field.getFormat().replace("yyyy-MM-dd","YYYY-MM-DD"));
                if("yyyy-MM-dd".equals(field.getFormat())){
                    fieldConfig.put("type","input-date");
                }else if("yyyy-MM-dd HH:mm:ss".equals(field.getFormat())){
                    fieldConfig.put("type","input-datetime");
                }
            }else if(DataType.DIC.equals(field.getType())){
                fieldConfig.put("type","select");
                fieldConfig.put("source",StrUtil.format("/options/{}",field.getFormat()));
                if(isMulti){
                    fieldConfig.put("multiple",true);
                }
            }else if(DataType.isNumber(field.getType())){
                fieldConfig.put("type","input-number");
            }else{
                fieldConfig.put("type","input-text");
            }
            items.add(fieldConfig);
        }
        form.put("body",grid);


        Map<String,Object> dialog = new HashMap<>();
        dialog.put("title",pageButton.getLabel());
        dialog.put("size",f.getSize());
        if("default".equals(f.getSize())){
            dialog.remove("size");
        }
        dialog.put("body",form);

        return dialog;
    }
}
