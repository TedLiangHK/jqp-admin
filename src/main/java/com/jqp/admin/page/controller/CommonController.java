package com.jqp.admin.page.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.lock.LockUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

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
        Object id = obj.get("id");
        if(id != null && StringUtils.isNotBlank(id.toString())){
            Map<String, Object> dbObj = jdbcService.getById(form.getTableName(), Long.parseLong(id.toString()));
            if(dbObj != null){
                dbObj.putAll(obj);
                obj = dbObj;
            }
        }
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

    //增加关联表数据
    @RequestMapping("/{model}/addRelation/{mainField}/{relationField}")
    public Result addRelation(@PathVariable("model") String model,
                              @PathVariable("mainField") String mainField,
                              @PathVariable("relationField") String relationField,
                              @RequestBody Map<String,Object> params) {
        String tableName = StringUtil.toSqlColumn(model);
        Long mainId = Long.parseLong(params.get(mainField).toString());
        String[] relationIds = params.get(relationField).toString().split(",");
        List<Long> relationIdList = new ArrayList<>();
        for(String s:relationIds){
            relationIdList.add(Long.parseLong(s));
        }
        if(relationIdList.isEmpty()){
            return Result.success();
        }

        String existsSql = StrUtil.format("select {} from {} where {}={} ",
                StringUtil.toSqlColumn(relationField),
                tableName,
                StringUtil.toSqlColumn(mainField),
                mainId
                );
        List<Map<String, Object>> maps = jdbcService.find(existsSql);
        for (int i = 0; i < maps.size(); i++) {
            Object o = maps.get(i).get(relationField);
            relationIdList.remove(Long.parseLong(o.toString()));
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Long relationId:relationIdList){
            Map<String,Object> obj = new HashMap<>();
            obj.put(mainField,mainId);
            obj.put(relationField,relationId);
            list.add(obj);
        }
        jdbcService.bathSaveOrUpdate(list,tableName);
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
        Map<String,Object> data = new HashMap<>();
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
            data.put(formField.getField(),obj.get(formField.getField()));
        }
        return Result.success(data);
    }


}
