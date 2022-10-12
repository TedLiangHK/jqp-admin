package com.jqp.admin.schema.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.schema.data.SchemaPage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/schemaPage")
public class SchemaPageController {
    @Resource
    private JdbcService jdbcService;
    @RequestMapping("/get/{code}")
    public Result get(@PathVariable String code){
        SchemaPage page = jdbcService.findOne(SchemaPage.class, "code", code);

        String schemaJson = page.getSchemaJson();
        JSON json = JSONUtil.parse(schemaJson);
        Map<String,Object> data = new HashMap<>();
        data.put("type",page.getSchemaPageType());
        data.put("schema",json);
        return Result.success(data);
    }

    @RequestMapping("/query/{code}")
    public Result query(@PathVariable String code,@RequestBody  PageParam pageParam){
        SchemaPage page = jdbcService.findOne(SchemaPage.class, "code", code);
        String sql = page.getQuerySql();

        Result<PageData<Map<String, Object>>> result = jdbcService.query(pageParam, sql);
        return result;
    }
}
