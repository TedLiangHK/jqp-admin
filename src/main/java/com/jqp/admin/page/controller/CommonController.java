package com.jqp.admin.page.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.Result;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.db.service.TableService;
import com.jqp.admin.page.constants.CheckRepeatType;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.Form;
import com.jqp.admin.page.data.FormField;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.rbac.service.ApiService;
import com.jqp.admin.util.StringUtil;
import com.jqp.admin.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Resource
    private JdbcService jdbcService;

    @Resource
    private TableService tableService;

    @Resource
    private FormService formService;

    @Resource
    private ApiService apiService;

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

        //校验唯一性
        for(FormField formField:formFields){
            String checkSql = null;
            if(CheckRepeatType.Global.equals(formField.getCheckRepeatType())){
                checkSql = StrUtil.format("select id from {} where {} = '${}' and id <> $id",
                        form.getTableName(),
                        StringUtil.toSqlColumn(formField.getField()),
                        formField.getField());
            }else if(CheckRepeatType.Enterprise.equals(formField.getCheckRepeatType())){
                checkSql = StrUtil.format("select id from {} " +
                                "where {} = '${}' and id <> $id " +
                                "and enterprise_id = $enterpriseId ",
                        form.getTableName(),
                        StringUtil.toSqlColumn(formField.getField()),
                        formField.getField());
            }else if(CheckRepeatType.Fields.equals(formField.getCheckRepeatType())){
                checkSql = StrUtil.format("select id from {} " +
                                "where id <> $id " ,
                        form.getTableName());
                if(StringUtils.isBlank(formField.getCheckRepeatConfig())){
                    continue;
                }
                String[] fields = StringUtil.splitStr(formField.getCheckRepeatConfig(), ",");
                for(String field:fields){
                    checkSql += StrUtil.format(" and {}='${}' ",
                            StringUtil.toSqlColumn(field),
                            field
                            );
                }
            }else if(CheckRepeatType.Sql.equals(formField.getCheckRepeatType())){
                if(StringUtils.isBlank(formField.getCheckRepeatConfig())){
                    continue;
                }
                checkSql = formField.getCheckRepeatConfig();
            }else{
                continue;
            }

            Map<String,Object> checkParams = new HashMap<>();
            checkParams.putAll(obj);
            SessionContext.putUserSessionParams(checkParams);
            if(jdbcService.isRepeat(checkSql,checkParams)){
                String checkTip = formField.getLabel()+"不能重复";
                if(StringUtils.isNotBlank(formField.getCheckRepeatTip())){
                    checkTip = formField.getCheckRepeatTip();
                }
                return Result.error(checkTip);
            }
        }

        String tableName = form.getTableName();
        Map<String,Object> context = new HashMap<>();
        context.put("obj",obj);
        context.put("tableName",tableName);
        context.put("form",form);
        Result<String> call = apiService.call(form.getBeforeApi(), context);
        if(!call.isSuccess()){
            return call;
        }

        final Map<String,Object> dbObj = obj;
        try{
            jdbcService.transactionOption(()->{
                jdbcService.saveOrUpdate(dbObj,tableName);
                Result<String> r = apiService.call(form.getAfterApi(), context);
                if(!r.isSuccess()){
                    throw new RuntimeException(r.getMsg());
                }
            });
        }catch (Exception e){
            log.error("保存异常",e);
            return Result.error(e.getMessage());
        }
        return Result.success(obj);
    }

    @RequestMapping("/{model}/delete/{id}")
    public Result delete(@PathVariable("id") Long id, @PathVariable("model") String model) {
        String tableName = StringUtil.toSqlColumn(model);
        jdbcService.delete(id,tableName);
        return Result.success();
    }

    //查询管理表id
    @RequestMapping("/{model}/getRelationIds/{mainField}/{relationField}/{id}")
    public Result getRelationIds(@PathVariable("model") String model,
                              @PathVariable("mainField") String mainField,
                              @PathVariable("relationField") String relationField,
                              @PathVariable("id") Long id) {
        String tableName = StringUtil.toSqlColumn(model);
        String sql = StrUtil.format("select {} from {} where {}={} ",
                StringUtil.toSqlColumn(relationField),
                tableName,
                StringUtil.toSqlColumn(mainField),
                id
        );
        List<Map<String, Object>> maps = jdbcService.find(sql);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < maps.size(); i++) {
            Object o = maps.get(i).get(relationField);
            Long _id = Long.parseLong(o.toString());
            ids.add(_id);
        }
        Map<String,Object> data = new HashMap<>();
        data.put(relationField,StringUtil.concatStr(ids,","));
        data.put("id",id);
        data.put(mainField,id);
        return Result.success(data);
    }

    //查询管理表id--增加企业id条件
    @RequestMapping("/{model}/getRelationIdsForEnterprise/{mainField}/{relationField}/{id}")
    public Result getRelationIdsForEnterprise(@PathVariable("model") String model,
                                 @PathVariable("mainField") String mainField,
                                 @PathVariable("relationField") String relationField,
                                 @PathVariable("id") Long id) {
        Long enterpriseId = SessionContext.getSession().getEnterpriseId();
        String tableName = StringUtil.toSqlColumn(model);
        String sql = StrUtil.format("select {} from {} where {}={} and enterprise_id = {} ",
                StringUtil.toSqlColumn(relationField),
                tableName,
                StringUtil.toSqlColumn(mainField),
                id,
                enterpriseId
        );
        List<Map<String, Object>> maps = jdbcService.find(sql);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < maps.size(); i++) {
            Object o = maps.get(i).get(relationField);
            Long _id = Long.parseLong(o.toString());
            ids.add(_id);
        }
        Map<String,Object> data = new HashMap<>();
        data.put(relationField,StringUtil.concatStr(ids,","));
        data.put("id",id);
        data.put(mainField,id);
        return Result.success(data);
    }

    //重新保存关联表数据
    @RequestMapping("/{model}/reSaveRelation/{mainField}/{relationField}")
    public Result reSaveRelation(@PathVariable("model") String model,
                              @PathVariable("mainField") String mainField,
                              @PathVariable("relationField") String relationField,
                              @RequestBody Map<String,Object> params) {
        String tableName = StringUtil.toSqlColumn(model);
        Long mainId = Long.parseLong(params.get(mainField).toString());
        String[] relationIds = params.get(relationField).toString().split(",");
        List<Long> relationIdList = new ArrayList<>();
        for(String s:relationIds){
            if(StringUtils.isNotBlank(s)){
                relationIdList.add(Long.parseLong(s));
            }
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Long relationId:relationIdList){
            Map<String,Object> obj = new HashMap<>();
            obj.put(mainField,mainId);
            obj.put(relationField,relationId);
            list.add(obj);
        }

        jdbcService.transactionOption(() -> {
            jdbcService.delete(StrUtil.format("delete from {} where {} = {}",
                    tableName,
                    StringUtil.toSqlColumn(mainField),
                    mainId
            ));
            jdbcService.bathSaveOrUpdate(list,tableName);
        });
        return Result.success("操作成功");
    }

    //重新保存关联表数据---增加企业id条件
    @RequestMapping("/{model}/reSaveRelationForEnterprise/{mainField}/{relationField}")
    public Result reSaveRelationForEnterprise(@PathVariable("model") String model,
                                 @PathVariable("mainField") String mainField,
                                 @PathVariable("relationField") String relationField,
                                 @RequestBody Map<String,Object> params) {
        Long enterpriseId = SessionContext.getSession().getEnterpriseId();
        String tableName = StringUtil.toSqlColumn(model);
        Long mainId = Long.parseLong(params.get(mainField).toString());
        String[] relationIds = params.get(relationField).toString().split(",");
        List<Long> relationIdList = new ArrayList<>();
        for(String s:relationIds){
            if(StringUtils.isNotBlank(s)){
                relationIdList.add(Long.parseLong(s));
            }
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Long relationId:relationIdList){
            Map<String,Object> obj = new HashMap<>();
            obj.put(mainField,mainId);
            obj.put(relationField,relationId);
            obj.put("enterpriseId",enterpriseId);
            list.add(obj);
        }

        jdbcService.transactionOption(() -> {
            jdbcService.delete(StrUtil.format("delete from {} where {} = {} and enterprise_id = {}",
                    tableName,
                    StringUtil.toSqlColumn(mainField),
                    mainId,
                    enterpriseId
            ));
            jdbcService.bathSaveOrUpdate(list,tableName);
        });
        return Result.success("操作成功");
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
            if(StringUtils.isNotBlank(s)){
                relationIdList.add(Long.parseLong(s));
            }
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
        boolean repeated = false;
        List<Map<String, Object>> maps = jdbcService.find(existsSql);
        for (int i = 0; i < maps.size(); i++) {
            Object o = maps.get(i).get(relationField);
            Long _id = Long.parseLong(o.toString());
            if(relationIdList.contains(_id)){
                repeated = true;
                relationIdList.remove(_id);
            }
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Long relationId:relationIdList){
            Map<String,Object> obj = new HashMap<>();
            obj.put(mainField,mainId);
            obj.put(relationField,relationId);
            list.add(obj);
        }
        jdbcService.bathSaveOrUpdate(list,tableName);
        if(repeated){
            return Result.success("操作成功,重复添加数据已排除");
        }else{
            return Result.success();
        }
    }

    @RequestMapping("/{formCode}/get")
    public Result get(Long id, @PathVariable("formCode") String formCode) {
        if(id == null){
            return Result.success(new HashMap<>());
        }
        Form form = formService.get(formCode);
        Map<String,Object> data = new HashMap<>();
        if(StringUtils.isNotBlank(form.getTableName())){
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
                data.put(formField.getField(),obj.get(formField.getField()));
            }
        }
        if(StringUtils.isNotBlank(form.getInitSql())){
            Map<String,Object> params = new HashMap<>();
            params.put("id",id);
            SessionContext.putUserSessionParams(params);
            String initSql = TemplateUtil.getValue(form.getInitSql(),params);
            String[] sqls = StringUtil.splitStr(initSql, ";");
            for(String sql:sqls){
                List<Map<String, Object>> list = jdbcService.find(sql);
                if(!list.isEmpty()){
                    Map<String, Object> obj = list.get(0);
                    data.putAll(obj);
                }
            }
        }
        return Result.success(data);
    }


}
