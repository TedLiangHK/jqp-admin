package com.jqp.admin.page.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if(StringUtils.isNotBlank(pageParam.getStr("code"))){
            sql += " and code like ? ";
            values.add("%"+pageParam.getStr("code")+"%");
        }
        if(StringUtils.isNotBlank(pageParam.getStr("name"))){
            sql += " and name like ? ";
            values.add("%"+pageParam.getStr("name")+"%");
        }
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
        if(jdbcService.isRepeat("select id from form where code = '$code' and id <> $id ", BeanUtil.beanToMap(form))){
            return Result.error("表单编号重复");
        }
        formService.save(form);
        return Result.success();
    }

    @RequestMapping("/getJson")
    public Result getJson(Long id){
        Form form = null;
        if(id == null){
            form = new Form();
        }else{
            form = formService.get(id);
        }
        return Result.success(MapUtil.builder().put("json", JSONUtil.toJsonPrettyStr(form)).build());
    }
    @RequestMapping("/saveJson")
    public Result saveJson(@RequestBody Map<String,Object> map){
        String json = (String) map.get("json");
        Form form = JSONUtil.toBean(json, Form.class);
        Form oldForm = formService.get(form.getCode());
        if(oldForm != null){
            form.setId(oldForm.getId());
        }else{
            form.setId(null);
        }
        formService.save(form);
        return Result.success();
    }

    @RequestMapping("/copyForm")
    public Result<Form> copyForm(Long id){
        if(id == null){
            return Result.success();
        }
        Form copy = formService.get(id);
        formService.delCache(copy);
        copy.setId(null);
        copy.setCode(copy.getCode()+"_copy");
        return Result.success(copy);
    }


    @RequestMapping("/formFields")
    public Result formFields(@RequestBody Form form){
        formService.reload(form);
        return Result.success(form,"已刷新");
    }

    @RequestMapping("/saveFields")
    public Result saveFields(Long id,String fields){
        Form form = formService.get(id);
        form.setFormFields(JSONUtil.toList(fields,FormField.class));
        formService.save(form);
        return Result.success();
    }
}
