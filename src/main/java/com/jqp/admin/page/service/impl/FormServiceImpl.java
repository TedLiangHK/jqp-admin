package com.jqp.admin.page.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.*;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.page.service.PageButtonService;
import com.jqp.admin.page.service.PageConfigService;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("formService")
@Slf4j
public class FormServiceImpl implements FormService {
    @Resource
    JdbcService jdbcService;
    @Resource
    PageConfigService pageConfigService;
    @Override
    @Transactional
    public void save(Form form) {
        jdbcService.saveOrUpdate(form);
        jdbcService.update("delete from form_field where form_id = ? ",form.getId());
        int seq = 0;
        for(FormField item:form.getFormFields()){
            item.setId(null);
            item.setFormId(form.getId());
            item.setSeq(seq);
            jdbcService.saveOrUpdate(item);
        }

        jdbcService.update("delete from form_ref where form_id = ? ",form.getId());
        seq = 0;
        for(FormRef item:form.getFormRefs()){
            item.setId(null);
            item.setFormId(form.getId());
            item.setSeq(seq);
            jdbcService.saveOrUpdate(item);
        }

        jdbcService.update("delete from form_button where form_id = ? ",form.getId());
        seq = 0;
        for(FormButton item:form.getFormButtons()){
            item.setId(null);
            item.setFormId(form.getId());
            item.setSeq(seq);
            jdbcService.saveOrUpdate(item);
        }
    }

    @Override
    public Form get(Long id) {
        Form form = jdbcService.getById(Form.class, id);
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
        Form form = jdbcService.findOne(Form.class,"code",code);
        List<FormField> formFields = jdbcService.find(FormField.class, "formId", form.getId());
        form.setFormFields(formFields);

        List<FormRef> formRefs = jdbcService.find(FormRef.class, "formId", form.getId());
        form.setFormRefs(formRefs);

        List<FormButton> formButtons = jdbcService.find(FormButton.class, "formId", form.getId());
        form.setFormButtons(formButtons);
        return form;
    }


    @Override
    public Map<String, Object> getFormJson(String code, BaseButton button) {

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
        boolean formDisabled = Whether.YES.equals(f.getDisabled());

        List<Map<String,Object>> items = new ArrayList<>();

        Map<String,Object> grid = new HashMap<>();
        grid.put("type","grid");
        grid.put("columns",items);


        List<FormField> formFields = f.getFormFields();
        for(FormField field:formFields){
            boolean fieldDisabled = Whether.YES.equals(f.getDisabled());
            Map<String,Object> fieldConfig = new HashMap<>();
            fieldConfig.put("clearable",true);
            fieldConfig.put("name", field.getField());
            fieldConfig.put("label",field.getLabel());
            fieldConfig.put("xs",12);
            fieldConfig.put("sm",6);
            fieldConfig.put("md",4);
            fieldConfig.put("lg",3);
            fieldConfig.put("columnClassName","mb-1");

            if(formDisabled || fieldDisabled){
                fieldConfig.put("disabled",true);
            }

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
            }else if(DataType.Selector.equals(field.getType())){
                Map<String, Object> selectorConfig = pageConfigService.getSelectorConfig(field.getFormat(),field.getField());
                fieldConfig.putAll(selectorConfig);
            }else if(DataType.isNumber(field.getType())){
                fieldConfig.put("type","input-number");
            }else{
                fieldConfig.put("type","input-text");
            }
            items.add(fieldConfig);
        }
        form.put("body",grid);


        Map<String,Object> dialog = new HashMap<>();
        dialog.put("title",button.getLabel());
        dialog.put("size",f.getSize());
        if("default".equals(f.getSize())){
            dialog.remove("size");
        }

        PageButtonService pageButtonService = SpringUtil.getBean(PageButtonService.class);
        List<Map<String,Object>> formButtons = new ArrayList<>();
        f.getFormButtons().forEach(b->{
            Map<String, Object> config = pageButtonService.getButton(b);
            if(Whether.YES.equals(b.getClose())){
                config.put("close",true);
            }
            formButtons.add(config);
        });

        if(!f.getFormRefs().isEmpty()){

            List<Map<String,Object>> dialogButtons = new ArrayList<>();
            Map<String,Object> saveBtn = new HashMap<>();
            saveBtn.put("label","保存基本信息");
            saveBtn.put("type","button");
            saveBtn.put("actionType","submit");
            saveBtn.put("primary",true);
            saveBtn.put("close",false);

            Map<String,Object> closeBtn = new HashMap<>();
            closeBtn.put("label","取消");
            closeBtn.put("type","button");
            closeBtn.put("actionType","close");
            closeBtn.put("close",true);

            dialogButtons.add(saveBtn);
            dialogButtons.add(closeBtn);

            dialog.put("actions",dialogButtons);

            form.remove("body");

            List<Map<String,Object>> tabs = new ArrayList<>();
            grid.put("title","基本信息");
            tabs.add(grid);

            List<String> targets = new ArrayList<>();
            targets.add("mainTable");
            f.getFormRefs().forEach(ref->{
                Map<String,Object> data = new HashMap<>();
                data.put("id","");
                data.put(ref.getRefField(),"${id}");
                Map<String, Object> curdJson = pageConfigService.getCurdJson(ref.getRefPageCode());
                curdJson.put("data",data);

                Object title = curdJson.remove("title");

                List<Map<String,Object>> tabContent = new ArrayList<>();
                tabContent.add(curdJson);

                Map<String,Object> tab = new HashMap<>();
                tab.put("title",title);
                tab.put("body",tabContent);

                tabs.add(tab);
                targets.add(ref.getRefPageCode()+"Table?"+ref.getRefField()+"=${id}");
            });

            form.put("tabs",tabs);
            saveBtn.put("reload", StringUtil.concatStr(targets,","));

        }

        Map<String,Object> closeBtn = new HashMap<>();
        closeBtn.put("label","取消");
        closeBtn.put("type","button");
        closeBtn.put("actionType","close");
        closeBtn.put("close",true);


        if(formDisabled){
            List<Map<String,Object>> dialogButtons = new ArrayList<>();

            dialogButtons.add(closeBtn);
            dialog.put("actions",dialogButtons);
        }

        if(!formButtons.isEmpty()){
            List<Map<String,Object>> dialogButtons = new ArrayList<>();
            dialogButtons.addAll(formButtons);

            dialogButtons.add(closeBtn);
            dialog.put("actions",dialogButtons);
        }

        dialog.put("body",form);
        return dialog;
    }
}
