package com.jqp.admin.page.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.*;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Opt;
import com.jqp.admin.page.constants.PageType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.data.PageQueryField;
import com.jqp.admin.page.data.PageResultField;
import com.jqp.admin.page.service.PageButtonService;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("pageService")
public class PageServiceImpl extends PageDaoImpl implements PageService {

    @Resource
    JdbcService jdbcService;

    @Resource
    PageButtonService pageButtonService;


    @Override
    public Result<CrudData<Map<String, Object>>> query(String pageCode, PageParam pageParam) {
        Page page = get(pageCode);
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
        Long treeId = (Long) pageParam.remove("treeId");
        Object op = pageParam.get("op");
        if("loadOptions".equals(op)){
            pageParam.put("page",1);
            pageParam.put("perPage",100);
            String[] optionValues = StringUtil.splitStr(pageParam.get("value").toString(),",");
            List<String> args = new ArrayList<>();
            for (int i = optionValues.length - 1; i >= 0; i--) {
                args.add("?");
                values.add(Long.valueOf(optionValues[i]));
            }
            sql.append(StrUtil.format(" and {} in ({}) ",
                    page.getValueField(),
                    StringUtil.concatStr(args,",")));
        }
        String orderBy = pageParam.getStr("orderBy");
        String orderDir = pageParam.getStr("orderDir");
        if(StrUtil.isNotBlank(orderBy) && StrUtil.isNotBlank(orderDir)){
            sql.append(StrUtil.format(" order by {} {} ",StringUtil.toSqlColumn(orderBy),orderDir));
        }else if(StrUtil.isNotBlank(page.getOrderBy())){
            sql.append(page.getOrderBy());
        }


        Result<PageData<Map<String, Object>>> result = jdbcService.query(pageParam, sql.toString(), values.toArray());

        Map<String,PageResultField> dateFields = new HashMap<>();
        for(PageResultField resultField:page.getResultFields()){
            if(DataType.isDate(resultField.getType())){
                dateFields.put(StringUtil.toFieldColumn(resultField.getField()),resultField);
            }
        }
        if(!dateFields.isEmpty()){
            for(Map<String,Object> item:result.getData().getItems()){
                for(Map.Entry<String,PageResultField> en:dateFields.entrySet()){
                    Object value = item.get(en.getKey());
                    if(value != null){
                        String format = StrUtil.isBlank(en.getValue().getFormat()) ? "yyyy-MM-dd":en.getValue().getFormat();
                        if(value instanceof LocalDateTime){
                            item.put(en.getKey(), DateUtil.format((LocalDateTime) value,format));
                        }else if(value instanceof java.sql.Date){
                            item.put(en.getKey(), DateUtil.format((java.sql.Date) value,format));
                        }else if(value instanceof java.util.Date){
                            item.put(en.getKey(), DateUtil.format((java.util.Date) value,format));
                        }
                    }
                }
            }
        }

        if(PageType.tree.equals(page.getPageType())){

            //树状结构选择父节点需要过滤当前节点,避免循环引用
            Set<Long> filterIds = new HashSet<>();
            if(treeId != null){
                Set<Long> childIds = new HashSet<>();
                childIds.add(treeId);
                filterIds.add(treeId);
                while(true){
                    if(childIds.isEmpty()){
                        break;
                    }
                    String childSql = StrUtil.format("select id from ({}) t where parent_id in ({})"
                            ,page.getQuerySql()
                            ,StringUtil.concatStr(childIds.stream().map(i-> "?").collect(Collectors.toList()),","));
                    List<Map<String, Object>> childs = jdbcService.find(childSql, childIds.toArray());
                    childIds.clear();
                    for(Map<String,Object> child:childs){

                        Long id = (Long) child.get("id");
                        childIds.add(id);
                        filterIds.add(id);
                    }
                }
            }


            Set<Long> rootIds = new HashSet<>();
            Set<Long> allIds = new HashSet<>();
            Map<Long,Map<String,Object>> allMap = new HashMap<>();
            Map<Long,Set<Long>> childIdMap = new HashMap<>();
            List<Map<String, Object>> items = result.getData().getItems();
            for(Map<String,Object> item:items){
                Long id = (Long)item.get("id");
                if(filterIds.contains(id)){
                    continue;
                }
                allIds.add(id);
                allMap.put(id,item);
            }
            for(Map<String,Object> item:items){
                Long id = (Long) item.get("id");
                if(filterIds.contains(id)){
                    continue;
                }
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
                    if(filterIds.contains(id)){
                        continue;
                    }

                    if(!dateFields.isEmpty()){
                        for(Map.Entry<String,PageResultField> en:dateFields.entrySet()){
                            Object value = child.get(en.getKey());
                            if(value != null){
                                String format = StrUtil.isBlank(en.getValue().getFormat()) ? "yyyy-MM-dd":en.getValue().getFormat();
                                if(value instanceof LocalDateTime){
                                    child.put(en.getKey(), DateUtil.format((LocalDateTime) value,format));
                                }else if(value instanceof java.sql.Date){
                                    child.put(en.getKey(), DateUtil.format((java.sql.Date) value,format));
                                }else if(value instanceof java.util.Date){
                                    child.put(en.getKey(), DateUtil.format((java.util.Date) value,format));
                                }
                            }
                        }
                    }
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
                if(filterIds.contains(rootId)){
                    continue;
                }
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
            if(DataType.DIC.equals(resultField.getType())){
                columnData.put("type","mapping");
                Map<String,Object> map = new HashMap<>();
                List<Map<String, Object>> options = jdbcService.find("select label,value from dic_item where parent_id in(" +
                        "select id from dic where dic_code = ? " +
                        ") order by value asc ", resultField.getFormat());
                options.forEach(o->{
                    map.put((String)o.get("value"),o.get("label"));
                });
                columnData.put("map",map);
            }
            crudData.getColumns().add(columnData);
        }
        Boolean selector = (Boolean) pageParam.get("selector");
        if(!Boolean.TRUE.equals(selector)){
            List<Map<String,Object>> rowButtons = new ArrayList<>();
            List<PageButton> pageButtons = page.getPageButtons();
            for(PageButton pageButton:pageButtons){
                if("row".equals(pageButton.getButtonLocation())){
                    rowButtons.add(pageButtonService.getButton(pageButton));
                }else if("top".equals(pageButton.getButtonLocation())){

                }
            }
            if(!rowButtons.isEmpty()){
                ColumnData columnData = new ColumnData();
                columnData.put("type","operation");
                columnData.put("label","操作");
                columnData.put("buttons",rowButtons);
                columnData.put("width",rowButtons.size()*50);
                crudData.getColumns().add(columnData);
            }
        }
        return Result.success(crudData);
    }
}
