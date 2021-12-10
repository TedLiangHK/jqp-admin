package com.jqp.admin.page.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.*;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Opt;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageQueryField;
import com.jqp.admin.page.data.PageResultField;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PageServiceImpl implements PageService {

    @Resource
    JdbcService jdbcService;

    @Override
    @Transactional
    public void save(Page page) {
        jdbcService.saveOrUpdate(page);

        jdbcService.update("delete from page_result_field where page_id = ? ",page.getId());
        int seq = 0;
        for (PageResultField field:
             page.getResultFields()) {
            field.setId(null);
            field.setPageId(page.getId());
            field.setSeq(++seq);
            jdbcService.saveOrUpdate(field);
        }
        jdbcService.update("delete from page_query_field where page_id = ? ",page.getId());
        seq = 0;
        for (PageQueryField field:
                page.getQueryFields()) {
            field.setId(null);
            field.setPageId(page.getId());
            field.setSeq(++seq);
            jdbcService.saveOrUpdate(field);
        }


    }

    @Override
    public Page get(Long id) {
        Page page = jdbcService.getById(Page.class, id);
        List<PageResultField> pageResultFields = jdbcService.find("select * from page_result_field where page_id = ? order by seq asc", PageResultField.class, id);
        page.setResultFields(pageResultFields);

        List<PageQueryField> pageQueryFields = jdbcService.find("select * from page_query_field where page_id = ? order by seq asc", PageQueryField.class, id);
        page.setQueryFields(pageQueryFields);
        return page;
    }

    @Override
    public Result<CrudData<Map<String, Object>>> query(Long pageId, PageParam pageParam) {
        Page page = get(pageId);
        StringBuffer sql = new StringBuffer(StrUtil.format("select * from ({}) t where 1=1 ",page.getQuerySql()));
        List<Object> values = new ArrayList<>();
        List<PageQueryField> queryFields = page.getQueryFields();
        List<String> resultNames = page.getResultFields().stream().map(f -> f.getField()).collect(Collectors.toList());
        for(PageQueryField field:queryFields){
            String fieldName = field.getField();
            if(!resultNames.contains(fieldName)){
                //log.warn("{},{}查询,字段{}不在结果字段里面,无法作为查询条件,请检查配置",page.getName(),page.getCode(),fieldName);
                continue;
            }
            String value = pageParam.getStr(StringUtil.toFieldColumn(fieldName));
            if(StrUtil.isBlank(value)){
                continue;
            }
            Opt.getSql(fieldName,field.getOpt(),field.getType(),value,field.getFormat(),sql,values);
        }

        if(StrUtil.isNotBlank(page.getOrderBy())){
            sql.append(page.getOrderBy());
        }
        Result<PageData<Map<String, Object>>> result = jdbcService.query(pageParam, sql.toString(), values.toArray());
        PageData<Map<String, Object>> pageData = result.getData();

        CrudData<Map<String,Object>> crudData = new CrudData<>();
        crudData.setRows(pageData.getItems());
        crudData.setCount(pageData.getTotal());
        List<PageResultField> resultFields = page.getResultFields();
        for(PageResultField resultField:resultFields){
            ColumnData columnData = new ColumnData();
            columnData.setName(StringUtil.toFieldColumn(resultField.getField()));
            columnData.setLabel(resultField.getLabel());
            crudData.getColumns().add(columnData);
        }
        return Result.success(crudData);
    }
}
