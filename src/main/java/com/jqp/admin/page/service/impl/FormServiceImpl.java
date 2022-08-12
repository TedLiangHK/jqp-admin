package com.jqp.admin.page.service.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jqp.admin.common.BaseData;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.db.data.ColumnMeta;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.*;
import com.jqp.admin.page.service.*;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("formService")
@Slf4j
public class FormServiceImpl implements FormService {
    @Resource
    FormDao formDao;
    @Resource
    JdbcService jdbcService;
    @Resource
    PageConfigService pageConfigService;
    @Resource
    InputFieldService inputFieldService;

    @Resource
    PageButtonDao pageButtonDao;

    @Resource
    PageDao pageDao;

    @Override
    public void save(Form form){
       Form oForm = jdbcService.getById(Form.class, form.getId());
        if(oForm!=null && !form.getCode().equals(oForm.getCode())){
            //修改表单code之后， 修改按钮关联表单的code
            pageButtonDao.getByForm(oForm).forEach(pageButton -> {
                pageButton.setOptionValue(form.getCode());
                pageButtonDao.save(pageButton);
                Page page = pageDao.get(pageButton.getPageId());
                if(page != null){
                    pageDao.delCache(page); //删除页面缓存
                }
            });

        }

        formDao.save(form);
    }
    @Override
    public Form get(Long id){
        return formDao.get(id);
    }
    @Override
    public Form get(String code){
        return formDao.get(code);
    }

    @Override
    public void del(Form form) {
        //删除表单表时未删除， 页面上关联该表单的按钮， 请手工删除
        // pageButtonDao.getByForm(form).forEach(pageButton -> pageButtonDao.del(pageButton));
        formDao.del(form);
    }

    @Override
    public Map<String, Object> getFormJson(String code, BaseButton button) {
        return getFormJson(get(code),button);
    }
    @Override
    public Map<String, Object> getFormJson(Form f, BaseButton button) {

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
            items.add(this.buildFormField(f,field));
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
            if(!SessionContext.hasButtonPermission(b.getCode())){
                return;
            }
            Map<String, Object> config = pageButtonService.getButton(b);
            if(Whether.YES.equals(b.getClose())){
                config.put("close",true);
            }
            formButtons.add(config);
        });

        if(!f.getFormRefs().isEmpty()){

            List<Object> dialogButtons = new ArrayList<>();
            dialogButtons.add("基本信息");
            Map<String,Object> saveBtn = new HashMap<>();
            if(!formDisabled && formButtons.isEmpty()){
                saveBtn.put("label","保存");
                saveBtn.put("type","button");
                saveBtn.put("actionType","submit");
                saveBtn.put("primary",true);
                saveBtn.put("close",false);
                saveBtn.put("className","m-l");

                Map<String,Object> resetBtn = new HashMap<>();
                resetBtn.put("label","重置");
                resetBtn.put("type","button");
                resetBtn.put("actionType","reset");
                resetBtn.put("close",true);
                resetBtn.put("className","m-l");

                dialogButtons.add(saveBtn);
                dialogButtons.add(resetBtn);
            }

            if(!formButtons.isEmpty()){
                dialogButtons.addAll(formButtons);
                formButtons.clear();
            }

//            dialog.put("actions",dialogButtons);
            dialog.put("actions",new ArrayList<>());



//            form.remove("body");

            List<Map<String,Object>> tabs = new ArrayList<>();
            grid.put("title","基本信息");
//            tabs.add(grid);

            List<String> targets = new ArrayList<>();
            targets.add("mainTable");
            f.getFormRefs().forEach(ref->{
                Map<String,Object> data = new HashMap<>();
                data.put("id","");

                String[] arr = StringUtil.splitStr(ref.getRefField(), "&");
                for(String p:arr){
                    String[] kv = StringUtil.splitStr(p, "=");
                    data.put(kv[0],kv[1]);
                }

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

//            form.put("tabs",tabs);

            List<Map<String,Object>> formBodys = new ArrayList<>();


            Map<String,Object> panel = new HashMap<>();
            panel.put("title","基本信息");
            panel.put("body",grid);
            panel.put("type","panel");
            panel.put("header",dialogButtons);

            formBodys.add(panel);

            form.remove("body");

            Map<String,Object> tab = new HashMap<>();
            tab.put("type","tabs");
            tab.put("tabs",tabs);
            formBodys.add(tab);

            form.put("body",formBodys);

            saveBtn.put("reload", StringUtil.concatStr(targets,","));

        }

        if(formDisabled){
            List<Map<String,Object>> dialogButtons = new ArrayList<>();

            dialog.put("actions",dialogButtons);
        }

        if(!formButtons.isEmpty()){
            List<Map<String,Object>> dialogButtons = new ArrayList<>();
            dialogButtons.addAll(formButtons);

            dialog.put("actions",dialogButtons);
        }

        dialog.put("body",form);
        return dialog;
    }

    @Override
    public Map<String, Object> buildFormField(Form f, FormField field) {
        boolean formDisabled = Whether.YES.equals(f.getDisabled());
        Map<String,Object> fieldConfig = inputFieldService.buildInputField(field,false);
        if(Whether.YES.equals(field.getHidden())){
            fieldConfig.put("columnClassName","mb-0");
        }else{
            fieldConfig.put("columnClassName","mb-3");
        }
        if(f.getFieldWidth() != null && !Whether.YES.equals(field.getHidden())){
            fieldConfig.put("xs",f.getFieldWidth());
            fieldConfig.put("sm",f.getFieldWidth());
            fieldConfig.put("md",f.getFieldWidth());
            fieldConfig.put("lg",f.getFieldWidth());
        }
        if(Whether.YES.equals(f.getDisabled()) || formDisabled){
            fieldConfig.put("disabled",true);
        }
        if(StringUtils.isNotBlank(field.getValidations())){
            fieldConfig.put("validations",field.getValidations());
        }
        return fieldConfig;
    }

    @Override
    public Map<String, Object> getPageJson(String code, BaseButton button) {

        String[] arr = StringUtil.splitStr(code,",");
        String pageCode = arr[0];
        String refField = arr[1];

        Map<String,Object> dialog = new HashMap<>();
        dialog.put("title",button.getLabel());
        dialog.put("size","xl");
        List<Map<String,Object>> dialogButtons = new ArrayList<>();

        dialog.put("actions",dialogButtons);

        Map<String,Object> data = new HashMap<>();
        data.put("id","");
        data.put(refField,"${id}");
        Map<String, Object> curdJson = pageConfigService.getCurdJson(pageCode);
        curdJson.put("data",data);
        dialog.put("body",curdJson);
        return dialog;
    }

    @Override
    public <T extends BaseData> T getObj(T obj, String formCode) {
        if(obj.getId() == null){
            return obj;
        }
        T dbObj = (T)jdbcService.getById(obj.getClass(), obj.getId());
        if(dbObj == null){
            return obj;
        }
        Form form = get(formCode);
        List<FormField> formFields = form.getFormFields();
        for(FormField formField:formFields){
            Object fieldValue = ReflectUtil.getFieldValue(obj, formField.getField());
            ReflectUtil.setFieldValue(dbObj,formField.getField(),fieldValue);
        }
        return dbObj;
    }

    @Override
    public void reload(Form form) {
        Map<String, FormField> fieldMap = form.getFormFields().stream().collect(Collectors.toMap(FormField::getField, f -> f));

        form.getFormFields().clear();
        List<ColumnMeta> columnMetas = jdbcService.columnMeta(StrUtil.format("select * from {} ",form.getTableName()));
        for(ColumnMeta columnMeta:columnMetas){
            String name = StringUtil.toFieldColumn(columnMeta.getColumnLabel());
            if(fieldMap.containsKey(name)){
                form.getFormFields().add(fieldMap.get(name));
                continue;
            }
            FormField field = new FormField();
            field.setField(name);
            field.setHidden(Whether.NO);
            field.setDisabled(Whether.NO);
            field.setLabel(columnMeta.getColumnComment());
            if(columnMeta.getColumnName() != null && columnMeta.getColumnName().toLowerCase().contains("id")){
                field.setHidden("YES");
            }

            if(columnMeta.getColumnClassName().equalsIgnoreCase(String.class.getCanonicalName())){
                //字符串类型
                if(columnMeta.getColumnType().toLowerCase().contains("longtext")){
                    if(columnMeta.getColumnName() != null && columnMeta.getColumnName().toLowerCase().contains("sql")){
                        field.setType(DataType.SQL);
                    }else if(columnMeta.getColumnName() != null && columnMeta.getColumnName().toLowerCase().contains("js")){
                        field.setType(DataType.JS);
                    }else if(columnMeta.getColumnName() != null && columnMeta.getColumnName().toLowerCase().contains("article")){
                        field.setType(DataType.ARTICLE);
                    }else{
                        field.setType(DataType.LONG_TEXT);
                    }

                }else{
                    field.setType(DataType.STRING);
                }
            }else if(columnMeta.getColumnClassName().toLowerCase().contains("date")){
                field.setType(DataType.DATE);
                field.setFormat("yyyy-MM-dd");
            }else if(columnMeta.getColumnClassName().equalsIgnoreCase(Integer.class.getCanonicalName())){
                field.setType(DataType.INT);
            }else if(columnMeta.getColumnClassName().equalsIgnoreCase(Long.class.getCanonicalName())){
                field.setType(DataType.LONG);
            }else if(columnMeta.getColumnClassName().equalsIgnoreCase(Float.class.getCanonicalName())
                    || columnMeta.getColumnClassName().equalsIgnoreCase(Double.class.getCanonicalName())){
                field.setType(DataType.DOUBLE);
            }
            form.getFormFields().add(field);
        }
    }
}
