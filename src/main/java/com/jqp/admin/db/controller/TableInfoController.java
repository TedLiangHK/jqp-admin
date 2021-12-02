package com.jqp.admin.db.controller;

import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.TableService;
import javafx.scene.control.Tab;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/tableInfo")
public class TableInfoController {

    @Resource
    private TableService tableService;
    @RequestMapping("/queryTable")
    public Result<PageData<TableInfo>> queryTable(PageParam pageParam){
        return tableService.queryTable(pageParam);
    }

    @RequestMapping("/tableInfo")
    public Result<TableInfo> tableInfo(String tableName){
        return tableService.tableInfo(tableName);
    }
}
