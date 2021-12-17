package com.jqp.admin.page.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.*;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Opt;
import com.jqp.admin.page.constants.PageType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageQueryField;
import com.jqp.admin.page.data.PageResultField;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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
        String orderBy = pageParam.getStr("orderBy");
        String orderDir = pageParam.getStr("orderDir");
        if(StrUtil.isNotBlank(orderBy) && StrUtil.isNotBlank(orderDir)){
            sql.append(StrUtil.format(" order by {} {} ",StringUtil.toSqlColumn(orderBy),orderDir));
        }else if(StrUtil.isNotBlank(page.getOrderBy())){
            sql.append(page.getOrderBy());
        }

        Result<PageData<Map<String, Object>>> result = jdbcService.query(pageParam, sql.toString(), values.toArray());

        if(PageType.tree.equals(page.getPageType())){
            Set<Long> rootIds = new HashSet<>();
            Set<Long> allIds = new HashSet<>();
            Map<Long,Map<String,Object>> allMap = new HashMap<>();
            Map<Long,Set<Long>> childIdMap = new HashMap<>();
            List<Map<String, Object>> items = result.getData().getItems();
            for(Map<String,Object> item:items){
                allIds.add((Long)item.get("id"));
                allMap.put((Long)item.get("id"),item);
            }
            for(Map<String,Object> item:items){
                Long id = (Long) item.get("id");
                Long parentId = (Long) item.get("parentId");
                if(parentId == null || !allIds.contains(parentId)){
                    rootIds.add(id);
                }

                if(parentId != null){
                    if(!childIdMap.containsKey(parentId)){
                        childIdMap.put(parentId,new HashSet<>());
                    }
                    childIdMap.get(parentId).add(id);
                }
            }

            Set<Long> childIds = new HashSet<>(rootIds);

            while(true){
                if(childIds.isEmpty()){
                    break;
                }
                String childSql = StrUtil.format("select * from ({}) t where parent_id in ({})"
                        ,page.getQuerySql()
                        ,StringUtil.concatStr(childIds.stream().map(i-> "?").collect(Collectors.toList()),","));
                List<Map<String, Object>> childs = jdbcService.find(childSql, childIds.toArray());
                childIds.clear();
                for(Map<String,Object> child:childs){
                    Long id = (Long) child.get("id");
                    allMap.put(id,child);
                    childIds.add(id);
                    Long parentId = (Long) child.get("parentId");
                    if(parentId != null){
                        if(!childIdMap.containsKey(parentId)){
                            childIdMap.put(parentId,new HashSet<>());
                        }
                        childIdMap.get(parentId).add(id);
                    }
                }
                childIds.removeAll(allIds);
                allIds.addAll(childIds);
            }

            List<Map<String,Object>> roots = new ArrayList<>();
            for(Long rootId:rootIds){
                roots.add(allMap.get(rootId));
            }
            String orderField = null;
            String orderSeq = null;
            if(StrUtil.isNotBlank(orderBy) && StrUtil.isNotBlank(orderDir)){
                orderField = orderBy;
                orderSeq = orderDir;
            }else if(StrUtil.isNotBlank(page.getOrderBy())){
                String[] arr = page.getOrderBy().toLowerCase().replace("order by", "").trim().split("\\s");
                if(arr.length == 1){
                    orderField = StringUtil.toFieldColumn(arr[0]);
                    orderSeq = "asc";
                }else if(arr.length==2){
                    orderField = StringUtil.toFieldColumn(arr[0]);
                    orderSeq = arr[1];
                }
            }
            Comparator<Map<String,Object>> comparator = null;
            if(StrUtil.isNotBlank(orderField)){
                String finalOrderField = orderField;
                String finalOrderSeq = orderSeq;
                comparator = (o1, o2) -> {
                    Object v1 = o1.get(finalOrderField);
                    Object v2 = o2.get(finalOrderField);

                    int a = 0;
                    if(v1 == null && v2 == null){
                        a = 0;
                    }else if(v1 == null && v2 != null){
                        a = -1;
                    }else if(v1 != null && v2 == null){
                        a = 1;
                    }else{
                        if(v1 instanceof Comparable){
                            a = ((Comparable<Object>) v1).compareTo(v2);
                        }else if(v1 instanceof Comparator){
                            a = ((Comparator<Object>) v1).compare(v1,v2);
                        }
                    }
                    if("desc".equalsIgnoreCase(finalOrderSeq)){
                        a = -a;
                    }
                    return a;
                };
            }
            if(comparator != null){
                Collections.sort(roots,comparator);
            }


            Comparator<Map<String, Object>> finalComparator = comparator;
            childIdMap.forEach((key, value) -> {
                Map<String, Object> p = allMap.get(key);
                if(p == null){
                    return;
                }
                if(!p.containsKey("children")){
                    p.put("children",new ArrayList<Map<String,Object>>());
                }
                for(Long childId:value){
                    ((List)p.get("children")).add(allMap.get(childId));
                }

                if(finalComparator != null){
                    List<Map<String,Object>>  children = (List<Map<String, Object>>) p.get("children");
                    Collections.sort(children, finalComparator);
                }
            });
            result.getData().setItems(roots);
        }

        PageData<Map<String, Object>> pageData = result.getData();

        CrudData<Map<String,Object>> crudData = new CrudData<>();
        crudData.setRows(pageData.getItems());
        crudData.setCount(pageData.getTotal());
        List<PageResultField> resultFields = page.getResultFields();
        for(PageResultField resultField:resultFields){
            if(Whether.YES.equals(resultField.getHidden())){
                continue;
            }
            ColumnData columnData = new ColumnData();
            columnData.setName(StringUtil.toFieldColumn(resultField.getField()));
            columnData.setLabel(resultField.getLabel());
            columnData.put("sortable",true);

            crudData.getColumns().add(columnData);
        }
        return Result.success(crudData);
    }

    @Override
    public List<Map<String, Object>> queryConfigs(Page page) {
        List<Map<String,Object>> queryConfigs = new ArrayList<>();
        for(PageQueryField field:page.getQueryFields()){
            Map<String,Object> queryConfig = new HashMap<>();
            queryConfig.put("name", StringUtil.toFieldColumn(field.getField()));
            queryConfig.put("label",field.getLabel());
            queryConfig.put("xs",12);
            queryConfig.put("sm",6);
            queryConfig.put("md",4);
            queryConfig.put("lg",3);
            queryConfig.put("columnClassName","mb-1");

            if(field.getWidth() != null){
                queryConfig.put("xs",field.getWidth());
                queryConfig.put("sm",field.getWidth());
                queryConfig.put("md",field.getWidth());
                queryConfig.put("lg",field.getWidth());
            }

            boolean isMulti = !Opt.isSingleValue(field.getOpt());

            if(StrUtil.isNotBlank(field.getValue())){
                queryConfig.put("value",field.getValue());
            }

            if(Whether.YES.equals(field.getHidden())){
                queryConfig.put("xs",0.0001);
                queryConfig.put("sm",0.0001);
                queryConfig.put("md",0.0001);
                queryConfig.put("lg",0.0001);
                queryConfig.put("label","");
                queryConfig.put("type","hidden");
            }else if(DataType.isDate(field.getType())){
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
        return queryConfigs;
    }
}
