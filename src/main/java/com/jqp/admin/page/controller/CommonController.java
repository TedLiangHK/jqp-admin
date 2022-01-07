package com.jqp.admin.page.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.ColumnInfo;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.db.service.TableService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.Form;
import com.jqp.admin.page.data.FormField;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.util.StringUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @Resource
    private JdbcService jdbcService;

    @Resource
    private TableService tableService;

    @Resource
    private FormService formService;

    @PostMapping("/{formCode}/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody Map<String, Object> obj, @PathVariable("formCode") String formCode) {
        Form form = formService.get(formCode);
        List<FormField> formFields = form.getFormFields();
        for(FormField formField:formFields){
            String type = formField.getType();
            Object value = obj.get(formField.getField());
            if(value == null){
                continue;
            }
            if(!Whether.YES.equals(formField.getMulti())){
                Object realValue = DataType.getValue(type, value.toString(), formField.getFormat());
                obj.put(formField.getField(),realValue);
            }
        }
        String tableName = form.getTableName();
        jdbcService.saveOrUpdate(obj,tableName);
        return Result.success(obj);
    }

    @RequestMapping("/{model}/delete/{id}")
    public Result delete(@PathVariable("id") Long id, @PathVariable("model") String model) {
        String tableName = StringUtil.toSqlColumn(model);
        jdbcService.delete(id,tableName);
        return Result.success();
    }

    @RequestMapping("/{formCode}/get")
    public Result get(Long id, @PathVariable("formCode") String formCode) {
        if(id == null){
            return Result.success(new HashMap<>());
        }
        Form form = formService.get(formCode);
        String tableName = StringUtil.toSqlColumn(form.getTableName());
        Map<String, Object> obj = jdbcService.getById(tableName, id);

        List<FormField> formFields = form.getFormFields();
        for(FormField formField:formFields){
            Object value = obj.get(formField.getField());
            if(value == null){
                continue;
            }
            if(value instanceof LocalDateTime){
                String format = StrUtil.isBlank(formField.getFormat()) ? "yyyy-MM-dd":formField.getFormat();
                String realValue = DateUtil.format((LocalDateTime) value, format);
                obj.put(formField.getField(),realValue);
            }
        }
        return Result.success(obj);
    }


}
