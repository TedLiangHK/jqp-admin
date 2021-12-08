package com.jqp.admin.page.controller;

import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.ColumnMeta;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageResultField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/admin/page")
@Slf4j
public class PageController {
    @Resource
    private JdbcService jdbcService;

    @Resource
    private JdbcTemplate jdbcTemplate;

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
        return Result.success(jdbcService.getById(Page.class,id));
    }

    @RequestMapping("/save")
    public Result<String> save(@RequestBody Page page){
        jdbcService.saveOrUpdate(page);
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
            }else if(columnMeta.getColumnClassName().equalsIgnoreCase(Integer.class.getCanonicalName())
                    || columnMeta.getColumnClassName().equalsIgnoreCase(Long.class.getCanonicalName())){
                field.setType(DataType.NUMBER);
            }else if(columnMeta.getColumnClassName().equalsIgnoreCase(Float.class.getCanonicalName())
                    || columnMeta.getColumnClassName().equalsIgnoreCase(Double.class.getCanonicalName())){
                field.setType(DataType.DOUBLE);
            }
            page.getResultFields().add(field);
        }

        log.info("元数据信息:{}",columnMetas);

        return Result.success(page,"已刷新");
    }


}
