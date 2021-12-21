package com.jqp.admin.page.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.common.CrudData;
import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.ColumnMeta;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Opt;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageQueryField;
import com.jqp.admin.page.data.PageResultField;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.util.StringUtil;
import com.jqp.admin.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/page")
@Slf4j
public class PageController {
    @Resource
    private JdbcService jdbcService;

    @Resource
    private PageService pageService;

    @RequestMapping("/query")
    public Result<PageData<Page>> query(@RequestBody PageParam pageParam){
        String sql = "select * from page where 1=1 ";
        List<Object> values = new ArrayList<>();
        return jdbcService.query(pageParam,Page.class,sql,values.toArray());
    }

    @RequestMapping("/get")
    public Result<Page> get(Long id){
        if(id == null){
            return Result.success(new Page());
        }
        return Result.success(pageService.get(id));
    }

    @RequestMapping("/save")
    public Result<String> save(@RequestBody Page page){
        pageService.save(page);
        return Result.success();
    }
    @RequestMapping("/resultFields")
    public Result resultFields(@RequestBody Page page){
        page.getResultFields().clear();

        List<ColumnMeta> columnMetas = jdbcService.columnMeta(page.getQuerySql());
        for(ColumnMeta columnMeta:columnMetas){
            PageResultField field = new PageResultField();
            field.setField(columnMeta.getColumnLabel());
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
            page.getResultFields().add(field);
        }

        log.info("元数据信息:{}",columnMetas);

        return Result.success(page,"已刷新");
    }

    @RequestMapping("/crudQuery/{pageCode}")
    public Result<CrudData<Map<String,Object>>> crudQuery(@RequestBody PageParam pageParam, @PathVariable(name="pageCode") String pageCode){
        return pageService.query(pageCode,pageParam);
    }

    @RequestMapping("/js/{pageCode}.js")
    public String js(@PathVariable("pageCode") String pageCode,HttpServletResponse response){

        response.setContentType("application/javascript");
        response.addHeader("Cache-Control","no-store");
        URL url = getClass().getClassLoader().getResource("ui-json-template/crud.js.vm");
        List<String> lines = FileUtil.readLines(url, Charset.forName("UTF-8"));
        String js = lines.stream().map(line -> line + "\n").collect(Collectors.joining());
        Map<String,Object> params = new HashMap<>();
        params.put("pageCode",pageCode);

        Page page = pageService.get(pageCode);

        List<Map<String,Object>> queryConfigs = pageService.queryConfigs(page);
        params.put("pageName",page.getName());
        params.put("queryConfigs", JSONUtil.toJsonPrettyStr(queryConfigs));

        js = TemplateUtil.getValue(js,params);
        return js;
    }

    @RequestMapping("/js/{pageCode}/{childPageCode}.js")
    public String oneToManyJs(@PathVariable("pageCode") String pageCode,@PathVariable("childPageCode") String childPageCode,HttpServletResponse response){

        response.setContentType("application/javascript");
        response.addHeader("Cache-Control","no-store");
        URL url = getClass().getClassLoader().getResource("ui-json-template/oneToMany.js.vm");
        List<String> lines = FileUtil.readLines(url, Charset.forName("UTF-8"));
        String js = lines.stream().map(line -> line + "\n").collect(Collectors.joining());
        Map<String,Object> params = new HashMap<>();
        params.put("pageCode",pageCode);
        params.put("childPageCode",childPageCode);

        Page page = pageService.get(pageCode);
        Page childPage = pageService.get(childPageCode);

        List<Map<String,Object>> queryConfigs = pageService.queryConfigs(page);
        List<Map<String,Object>> childQueryConfigs = pageService.queryConfigs(childPage);
        params.put("pageName",page.getName());
        params.put("childPageName",childPage.getName());
        params.put("queryConfigs", JSONUtil.toJsonPrettyStr(queryConfigs));
        params.put("childQueryConfigs", JSONUtil.toJsonPrettyStr(childQueryConfigs));

        js = TemplateUtil.getValue(js,params);
        return js;
    }

    @RequestMapping("/queryConfigs/{pageCode}")
    public Result queryConfigs(@PathVariable("pageCode") String pageCode){
        Page page = pageService.get(pageCode);
        List<Map<String, Object>> queryConfigs = pageService.queryConfigs(page);
        return Result.successForKey("config",queryConfigs);
    }
}
