package com.jqp.admin.db.controller;

import cn.hutool.core.date.DateUtil;
import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.ColumnInfo;
import com.jqp.admin.db.data.ForeignKey;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.TableService;
import com.jqp.admin.util.StringUtil;
import com.jqp.admin.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.util.*;

@RestController
@RequestMapping("/tableInfo")
@Slf4j
public class TableInfoController {

    @Resource
    private TableService tableService;
    @RequestMapping("/queryTable")
    public Result<PageData<TableInfo>> queryTable(@RequestBody PageParam pageParam){
        log.info("参数,{}",pageParam);
        return tableService.queryTable(pageParam);
    }

    @RequestMapping("/tableInfo")
    public Result<TableInfo> tableInfo(String tableName){
        return tableService.tableInfo(tableName);
    }
    @RequestMapping("/copyTableInfo")
    public Result<TableInfo> copyTableInfo(String tableName){
        Result<TableInfo> copyTableInfo = tableService.tableInfo(tableName);
        if(copyTableInfo.isSuccess()){
            TableInfo data = copyTableInfo.getData();
            data.setOldTableName(null);
            data.setTableName(data.getTableName()+"_copy");

            data.getColumnInfos().forEach(c->{
                c.setOldColumnName(null);
            });
            data.getIndexInfos().forEach(c->{
                c.setOldKeyName(null);
            });
        }
        return copyTableInfo;
    }

    @RequestMapping("/updateTable")
    public Result updateTableInfo(@RequestBody   TableInfo tableInfo){
        return tableService.updateTable(tableInfo);
    }

    @RequestMapping("/dropTable")
    public Result dropTable(String tableName){
        return tableService.dropTable(tableName);
    }
    @RequestMapping("/saveForeignKey")
    public Result saveForeignKey(@RequestBody ForeignKey foreignKey){
        return tableService.saveForeignKey(foreignKey);
    }
    @RequestMapping("/dropForeignKey")
    public Result dropForeignKey(String tableName,String constraintName){
        return tableService.dropForeignKey(tableName,constraintName);
    }
    @RequestMapping("/generateJavaCode")
    public Result generateJavaCode(String tableName){
        Result<TableInfo> result = tableService.tableInfo(tableName);
        TableInfo tableInfo = result.getData();
        List<ColumnInfo> columnInfos = tableInfo.getColumnInfos();
        Map<String,String> java = new HashMap<>();

        Map<String,Object> params = new HashMap<>();
        params.put("tableName", StringUtil.toFirstUp(StringUtil.toFieldColumn(tableInfo.getTableName())));
        params.put("tableComment",tableInfo.getTableComment());
        params.put("date", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));

        List<Map<String,Object>> columns = new ArrayList<>();
        for(ColumnInfo columnInfo:columnInfos){
            Map<String,Object> column = new HashMap<>();
            column.put("columnName",StringUtil.toFieldColumn(columnInfo.getColumnName()));
            column.put("columnComment",columnInfo.getColumnComment());
            String type = columnInfo.getColumnType();
            String javaType = "";
            if(type.startsWith("int")){
                javaType = "Integer";
            }else if(type.startsWith("bigint")){
                javaType = "Long";
            }else if(type.startsWith("varchar") || type.startsWith("longtext")){
                javaType = "String";
            }else if(type.startsWith("datetime")){
                javaType = "Date";
            }else if(type.startsWith("float")){
                javaType = "Double";
            }
            column.put("type",javaType);
            columns.add(column);
        }
        params.put("columns",columns);

        String model = TemplateUtil.getUi("model.java.vm", params);

        java.put("model",model);
        return Result.success(java);
    }
}
