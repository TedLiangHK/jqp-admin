package com.jqp.admin.db.service.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.BaseData;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.ColumnInfo;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.db.service.TableService;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("jdbcService")
@ConditionalOnProperty(value="db.type",havingValue = "mysql")
@Slf4j
public class MysqlJdbcServiceImpl extends MysqlJdbcDaoImpl implements JdbcService {
    @Resource
    private TableService tableService;

    @Override
    public void insert(BaseData obj) {
        if(obj == null){
            return;
        }
        String clzName = obj.getClass().getSimpleName();
        String tableName = StringUtil.toSqlColumn(clzName);
        Result<TableInfo> tableInfo = tableService.tableInfo(tableName);
        if(!tableInfo.isSuccess()){
            throw new RuntimeException("找不到表:"+tableName);
        }
        TableInfo table = tableInfo.getData();
        List<ColumnInfo> columnInfos = table.getColumnInfos();
        List<String> columns = columnInfos.stream().map(columnInfo -> columnInfo.getColumnName()).collect(Collectors.toList());
        List<String> args = columnInfos.stream().map(columnInfo -> "?").collect(Collectors.toList());
        String sql = StrUtil.format("insert into {} ({}) values ({})",table.getTableName(),StringUtil.concatStr(columns,","),StringUtil.concatStr(args,","));
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(obj.getClass());
        List<Object> values = new ArrayList<>();
        for(String column:columns){
            Object value = null;
            String fieldName = StringUtil.toFieldColumn(column);
            if(fieldMap.containsKey(fieldName)){
                Field field = fieldMap.get(fieldName);
                value = ReflectUtil.getFieldValue(obj, field);
            }
            values.add(value);
        }

        Long id = this.insert("执行" + tableName + "insert", sql, values.toArray());
        obj.setId(id);
    }

    @Override
    public void insert(Map<String, Object> obj, String tableName) {
        if(obj == null){
            return;
        }
        Result<TableInfo> tableInfo = tableService.tableInfo(tableName);
        if(!tableInfo.isSuccess()){
            throw new RuntimeException("找不到表:"+tableName);
        }
        TableInfo table = tableInfo.getData();
        List<ColumnInfo> columnInfos = table.getColumnInfos();
        List<String> columns = columnInfos.stream().map(columnInfo -> columnInfo.getColumnName()).collect(Collectors.toList());
        List<String> args = columnInfos.stream().map(columnInfo -> "?").collect(Collectors.toList());
        String sql = StrUtil.format("insert into {} ({}) values ({})",table.getTableName(),StringUtil.concatStr(columns,","),StringUtil.concatStr(args,","));
        List<Object> values = new ArrayList<>();
        for(String column:columns){
            Object value = null;
            String fieldName = StringUtil.toFieldColumn(column);
            if(obj.containsKey(fieldName)){
                value = obj.get(fieldName);
            }
            values.add(value);
        }

        Long id = this.insert("执行" + tableName + "insert", sql, values.toArray());
        obj.put("id",id);
    }

    @Override
    public void update(BaseData obj) {
        if(obj == null){
            return;
        }
        if(obj.getId() == null){
            throw new RuntimeException("更新失败,id不能为空");
        }
        String clzName = obj.getClass().getSimpleName();
        String tableName = StringUtil.toSqlColumn(clzName);
        Result<TableInfo> tableInfo = tableService.tableInfo(tableName);
        if(!tableInfo.isSuccess()){
            throw new RuntimeException("找不到表:"+tableName);
        }

        TableInfo table = tableInfo.getData();
        List<ColumnInfo> columnInfos = table.getColumnInfos();
        List<String> columns = columnInfos.stream().map(columnInfo -> columnInfo.getColumnName()).collect(Collectors.toList());
        List<String> args = columnInfos.stream().map(columnInfo -> columnInfo.getColumnName()+"=?").collect(Collectors.toList());
        String sql = StrUtil.format("update {} set {} where id = ? ",table.getTableName(),StringUtil.concatStr(args,","),args);
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(obj.getClass());
        List<Object> values = new ArrayList<>();
        for(String column:columns){
            Object value = null;
            String fieldName = StringUtil.toFieldColumn(column);
            if(fieldMap.containsKey(fieldName)){
                Field field = fieldMap.get(fieldName);
                value = ReflectUtil.getFieldValue(obj, field);
            }
            values.add(value);
        }
        values.add(obj.getId());
        this.update(StrUtil.format("更新{},{}",tableName,obj.getId()),sql,values.toArray());
    }

    @Override
    public void update(Map<String, Object> obj, String tableName) {
        if(obj == null){
            return;
        }
        Long id = (Long) obj.get("id");
        if(id == null){
            throw new RuntimeException("更新失败,id不能为空");
        }
        Result<TableInfo> tableInfo = tableService.tableInfo(tableName);
        if(!tableInfo.isSuccess()){
            throw new RuntimeException("找不到表:"+tableName);
        }

        TableInfo table = tableInfo.getData();
        List<ColumnInfo> columnInfos = table.getColumnInfos();
        List<String> columns = columnInfos.stream().map(columnInfo -> columnInfo.getColumnName()).collect(Collectors.toList());
        List<String> args = columnInfos.stream().map(columnInfo -> columnInfo.getColumnName()+"=?").collect(Collectors.toList());
        String sql = StrUtil.format("update {} set {} where id = ? ",table.getTableName(),StringUtil.concatStr(args,","),args);
        List<Object> values = new ArrayList<>();
        for(String column:columns){
            String fieldName = StringUtil.toFieldColumn(column);
            Object value = obj.get(fieldName);
            values.add(value);
        }
        values.add(id);
        this.update(StrUtil.format("更新{},{}",tableName,id),sql,values.toArray());
    }

    @Override
    public void saveOrUpdate(BaseData obj) {
        if(obj == null){
            return;
        }
        if(obj.getId() == null){
            this.insert(obj);
        }else{
            this.update(obj);
        }
    }

    @Override
    public void saveOrUpdate(Map<String, Object> obj,String tableName) {
        if(obj == null){
            return;
        }
        if(obj.get("id") == null){
            this.insert(obj,tableName);
        }else{
            this.update(obj,tableName);
        }
    }

    @Override
    public void bathSaveOrUpdate(List<BaseData> objs) {
        if(objs == null){
            return;
        }
        objs.forEach(obj->{
            this.saveOrUpdate(obj);
        });
    }

    @Override
    public void bathSaveOrUpdate(List<Map<String, Object>> objs, String tableName) {
        if(objs == null){
            return;
        }
        objs.forEach(obj->{
            this.saveOrUpdate(obj,tableName);
        });
    }


}
