package com.jqp.admin.db.controller;

import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
