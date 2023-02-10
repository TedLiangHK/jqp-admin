package com.jqp.admin.page.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jqp.admin.common.*;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.service.TemplateService;
import com.jqp.admin.common.service.impl.AbstractCacheService;
import com.jqp.admin.db.data.ColumnMeta;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.*;
import com.jqp.admin.page.data.*;
import com.jqp.admin.page.service.DicCacheService;
import com.jqp.admin.page.service.PageButtonService;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.util.SeqComparator;
import com.jqp.admin.util.StringUtil;
import com.jqp.admin.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("pageService")
public class PageServiceImpl extends AbstractCacheService<Page> implements PageService {

    @Resource
    JdbcService jdbcService;

    @Resource
    PageButtonService pageButtonService;

    @Resource
    @Lazy
    TemplateService templateService;

    @Override
    public Page load(String pageCode) {
        Page page = jdbcService.findOne(Page.class, "code", pageCode);
        this.get(page);
        return page;
    }

    @Override
    public void del(Page page) {
        if(page == null){
            return;
        }
        jdbcService.delete(page);
        super.invalid(page.getCode());
    }

    private void get(Page page) {
        if(page == null){
            return;
        }
        List<PageResultField> pageResultFields = jdbcService.find(PageResultField.class, "pageId", page.getId());
        page.setResultFields(pageResultFields);

        List<PageQueryField> pageQueryFields = jdbcService.find(PageQueryField.class, "pageId", page.getId());
        page.setQueryFields(pageQueryFields);

        List<PageButton> pageButtons = jdbcService.find(PageButton.class, "pageId", page.getId());
        page.setPageButtons(pageButtons);

        List<PageRef> pageRefs = jdbcService.find(PageRef.class, "pageId", page.getId());
        page.setPageRefs(pageRefs);
    }

    @Override
    public void save(Page page) {
        Page oPage = jdbcService.getById(Page.class, page.getId());
        if(oPage!=null && !page.getCode().equals(oPage.getCode())){
            super.invalid(oPage.getCode());
        }
        jdbcService.saveOrUpdate(page);
        jdbcService.delete("delete from page_result_field where page_id = ? ", page.getId());
        int seq = 0;

        Collections.sort(page.getPageButtons(), SeqComparator.instance);
        Collections.sort(page.getResultFields(), SeqComparator.instance);
        Collections.sort(page.getPageRefs(), SeqComparator.instance);
        Collections.sort(page.getQueryFields(), SeqComparator.instance);
        for (PageResultField field : page.getResultFields()) {
            field.setId(null);
            field.setPageId(page.getId());
            field.setSeq(++seq);
            jdbcService.saveOrUpdate(field);
        }
        jdbcService.delete("delete from page_query_field where page_id = ? ", page.getId());
        seq = 0;
        for (PageQueryField field : page.getQueryFields()) {
            field.setId(null);
            field.setPageId(page.getId());
            field.setSeq(++seq);
            jdbcService.saveOrUpdate(field);
        }

        jdbcService.delete("delete from page_button where page_id = ? ", page.getId());
        seq = 0;
        for (PageButton button : page.getPageButtons()) {
            button.setId(null);
            button.setPageId(page.getId());
            button.setSeq(++seq);
            jdbcService.saveOrUpdate(button);
        }

        jdbcService.delete("delete from page_ref where page_id = ? ", page.getId());
        seq = 0;
        for (PageRef ref : page.getPageRefs()) {
            ref.setId(null);
            ref.setPageId(page.getId());
            ref.setSeq(++seq);
            jdbcService.saveOrUpdate(ref);
        }
        super.invalid(page.getCode());
    }

    @Override
    public Page get(Long id) {
        Page page = jdbcService.getById(Page.class, id);
        this.get(page);
        return page;
    }


    @Override
    public Result<CrudData<Map<String, Object>>> query(String pageCode, PageParam pageParam) {
        Page page = get(pageCode);
        if(page == null){
            return Result.error("页面已被删除");
        }
        StringBuffer sql = new StringBuffer(StrUtil.format("select * from ({}) t where 1=1 ",this.getQuerySql(page.getQuerySql())));
        List<Object> values = new ArrayList<>();
        List<PageQueryField> queryFields = page.getQueryFields();
        List<String> resultNames = page.getResultFields().stream().map(f -> f.getField()).collect(Collectors.toList());
        for(PageQueryField field:queryFields){
            String fieldName = field.getField();
            if(!resultNames.contains(fieldName) && !resultNames.contains(fieldName.replace("_from","").replace("_to",""))){
                //log.warn("{},{}查询,字段{}不在结果字段里面,无法作为查询条件,请检查配置",page.getName(),page.getCode(),fieldName);
                continue;
            }
            String value = pageParam.getStr(StringUtil.toFieldColumn(fieldName));
            if(StrUtil.isBlank(value)){

                if(Whether.YES.equals(field.getRef()) && StrUtil.isNotBlank(field.getValue())){
                    value = SessionContext.getTemplateValue(field.getValue());
                }else{
                    continue;
                }
            }
            if(ComponentType.InputTree.equals(field.getComponentType()) && !Whether.YES.equals(field.getMulti())){
                //树结构查询
                Page refPage = get(field.getFormat());
                String treeSql = templateService.treeSql("and " + field.getField() + " in ({})", "(" + getQuerySql(refPage.getQuerySql()) + ") t", value);
                //log.info("treeSql:{}",treeSql);
                sql.append(treeSql);
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
                values.add(optionValues[i]);
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

        if(Whether.NO.equals(page.getOpenPage())){
            pageParam.put("page",1);
            pageParam.put("perPage",Integer.MAX_VALUE);
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
                            ,this.getQuerySql(page.getQuerySql())
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
                        ,this.getQuerySql(page.getQuerySql())
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
            if(orderField.contains(".")){
                orderField = orderField.substring(orderField.indexOf(".")+1);
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
            if(resultField.getWidth() != null){
                columnData.put("width",resultField.getWidth());
            }
            if(DataType.DIC.equals(resultField.getType())){
                columnData.put("type","mapping");
                Map<String,Object> map = new HashMap<>();
                DicCacheService dicCacheService = SpringUtil.getBean(DicCacheService.class);
                List<Map<String, Object>> options = dicCacheService.options(resultField.getFormat());
                options.forEach(o->{
                    map.put((String)o.get("value"),o.get("label"));
                });
                columnData.put("map",map);
            }else if(DataType.IMAGE.equals(resultField.getType())){
                columnData.put("type","images");
                columnData.put("enlargeAble",true);
            }else if(DataType.FILE.equals(resultField.getType())){
                columnData.put("type","input-file");
                columnData.put("multiple",true);
                columnData.put("disabled",true);
            }
            crudData.getColumns().add(columnData);
        }
        Boolean selector = (Boolean) pageParam.get("selector");
        if(!Boolean.TRUE.equals(selector)){
            int optionWidth = 0;
            int fontWidth = 13;
            int padding = 26;
            PageButtonData pageButtonData = pageButtonService.dealPageButton(page.getPageButtons(), true);
            List<Map<String,Object>> rowButtons = pageButtonData.getRowButtons();
            for(Map<String,Object> btn:rowButtons){
                String label = (String) btn.get("label");
                if(label!= null){
                    optionWidth += label.length()*fontWidth+padding;
                }
            }
            if(optionWidth>400){
                optionWidth = 400;
            }
            if(!rowButtons.isEmpty()){
                ColumnData columnData = new ColumnData();
                columnData.put("type","operation");
                columnData.put("label","操作");
                columnData.put("buttons",rowButtons);
                columnData.put("width",optionWidth);
                columnData.put("fixed","right");
                crudData.getColumns().add(columnData);
            }
        }
        if(Whether.NO.equals(page.getOpenPage())){
            crudData.setCount(null);
        }
        return Result.success(crudData);
    }

    @Override
    public void reload(Page page) {

        Map<String,PageResultField> fieldMap = new HashMap<>();
        for(PageResultField field:page.getResultFields()){
            fieldMap.put(field.getField(),field);
        }
        page.getResultFields().clear();

        List<ColumnMeta> columnMetas = jdbcService.columnMeta(this.getQuerySql(page.getQuerySql()));
        for(ColumnMeta columnMeta:columnMetas){
            if(fieldMap.containsKey(columnMeta.getColumnLabel())){
                page.getResultFields().add(fieldMap.get(columnMeta.getColumnLabel()));
                continue;
            }
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
    }

    @Override
    public Map<String, Object> optionConfig(String pageCode) {
        PageParam pageParam = new PageParam();
        pageParam.put("page",1);
        pageParam.put("perPage",Integer.MAX_VALUE);
        Result<CrudData<Map<String, Object>>> result = this.query(pageCode, pageParam);

        Page page = this.get(pageCode);

        Map<String,Object> config = new HashMap<>();
        config.put("labelField",page.getLabelField());
        config.put("valueField",page.getValueField());
        config.put("searchable",true);
        config.put("withChildren",true);
        config.put("options",result.getData().getRows());
        return config;
    }

    @Override
    public String getQuerySql(String querySql) {
        Map<String,Object> params = new HashMap<>();
        SessionContext.putUserSessionParams(params);
        querySql = TemplateUtil.getValue(querySql,params);
        return querySql;
    }

    @Override
    public Result<CrudData<Map<String, Object>>> queryAll(String pageCode) {
        return this.queryAll(pageCode,null);
    }

    @Override
    public Result<CrudData<Map<String, Object>>> queryAll(String pageCode, Map<String, Object> params) {
        PageParam pageParam = new PageParam();
        pageParam.put("page",1);
        pageParam.put("perPage",Integer.MAX_VALUE);
        if(params != null){
            pageParam.putAll(params);
        }
        return this.query(pageCode,pageParam);
    }
}
