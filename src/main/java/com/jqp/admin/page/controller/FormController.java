package com.jqp.admin.page.controller;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.ColumnMeta;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.data.Form;
import com.jqp.admin.page.data.FormField;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.util.StringUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/form")
public class FormController {
    @Resource
    private FormService formService;
    @Resource
    private JdbcService jdbcService;

    @RequestMapping("/query")
    public Result<PageData<Page>> query(@RequestBody PageParam pageParam){
        String sql = "select * from form where 1=1 ";
        List<Object> values = new ArrayList<>();
        return jdbcService.query(pageParam,Page.class,sql,values.toArray());
    }

    @RequestMapping("/get")
    public Result<Form> get(Long id){
        if(id == null){
            return Result.success(new Form());
        }
        return Result.success(formService.get(id));
    }

    @RequestMapping("/save")
    public Result<String> save(@RequestBody Form form){
        formService.save(form);
        return Result.success();
    }
    @RequestMapping("/copyForm")
    public Result<Form> copyForm(Long id){
        if(id == null){
            return Result.success();
        }
        Form copy = formService.get(id);
        copy.setId(null);
        copy.setCode(copy.getCode()+"_copy");
        return Result.success(copy);
    }


    @RequestMapping("/formFields")
    public Result formFields(@RequestBody Form form){
        form.getFormFields().clear();


        List<ColumnMeta> columnMetas = jdbcService.columnMeta(StrUtil.format("select * from {} ",form.getTableName()));
        for(ColumnMeta columnMeta:columnMetas){
            FormField field = new FormField();
            field.setField(StringUtil.toFieldColumn(columnMeta.getColumnLabel()));
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

        return Result.success(form,"已刷新");
    }
}
