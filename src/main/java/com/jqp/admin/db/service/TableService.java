package com.jqp.admin.db.service;

import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.TableInfo;

public interface TableService {
    Result<PageData<TableInfo>> queryTable(PageParam pageParam);
    Result<TableInfo> tableInfo(String tableName);
    Result<Void> updateTable(TableInfo tableInfo);

    Result dropTable(String tableName);
}
