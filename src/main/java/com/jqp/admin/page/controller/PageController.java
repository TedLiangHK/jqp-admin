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
            }else if(columnMeta.getColumnClassName().equalsIgnoreCase(Date.class.getCanonicalName())){
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

    @RequestMapping("/crudQuery/{pageId}")
    public Result<CrudData<Map<String,Object>>> crudQuery(@RequestBody PageParam pageParam, @PathVariable(name="pageId") Long pageId){
        return pageService.query(pageId,pageParam);
    }

    @RequestMapping("/js/{pageId}.js")
    public String js(@PathVariable("pageId") Long pageId,HttpServletResponse response){

        response.setContentType("application/javascript");
        response.addHeader("Cache-Control","no-store");
        URL url = getClass().getClassLoader().getResource("ui-json-template/crud.js.vm");
        List<String> lines = FileUtil.readLines(url, Charset.forName("UTF-8"));
        String js = lines.stream().map(line -> line + "\n").collect(Collectors.joining());
        Map<String,Object> params = new HashMap<>();
        params.put("pageId",pageId);

        Page page = pageService.get(pageId);
        List<PageQueryField> queryFields = page.getQueryFields();

        List<Map<String,Object>> queryConfigs = new ArrayList<>();
        for(PageQueryField field:queryFields){
            Map<String,Object> queryConfig = new HashMap<>();
            queryConfig.put("name", StringUtil.toFieldColumn(field.getField()));
            queryConfig.put("label",field.getLabel());
            queryConfig.put("xs",12);
            queryConfig.put("sm",6);
            queryConfig.put("md",4);
            queryConfig.put("lg",3);
            queryConfig.put("columnClassName","mb-1");

            boolean isMulti = !Opt.isSingleValue(field.getOpt());

            if(DataType.isDate(field.getType())){
                queryConfig.put("format",field.getFormat().replace("yyyy-MM-dd","YYYY-MM-DD"));
                if("yyyy-MM-dd".equals(field.getFormat())){
                    queryConfig.put("type","input-date");
                }else if("yyyy-MM-dd HH:mm:ss".equals(field.getFormat())){
                    queryConfig.put("type","input-datetime");
                }
            }else if(DataType.DIC.equals(field.getType())){
                queryConfig.put("type","select");
                if(isMulti){
                    queryConfig.put("multiple",true);
                }
            }else if(DataType.isNumber(field.getType())){
                queryConfig.put("type","input-number");
            }else{
                queryConfig.put("type","input-text");
            }
            if(Opt.betweenAnd.equals(field.getOpt())){
                if(DataType.isDate(field.getType())){
                    if("yyyy-MM-dd".equals(field.getFormat())){
                        queryConfig.put("type","input-date-range");
                    }else if("yyyy-MM-dd HH:mm:ss".equals(field.getFormat())){
                        queryConfig.put("type","input-datetime-range");
                    }
                }
            }
            queryConfigs.add(queryConfig);
        }
        params.put("pageName",page.getName());
        params.put("queryConfigs", JSONUtil.toJsonPrettyStr(queryConfigs));

        js = TemplateUtil.getValue(js,params);
        return js;
    }

}
