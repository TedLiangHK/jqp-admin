package com.jqp.admin.db.service;

import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.data.ColumnInfo;
import com.jqp.admin.db.data.IndexInfo;
import com.jqp.admin.db.data.TableInfo;

import java.util.List;

public interface TableService {
    Result<PageData<TableInfo>> queryTable(PageParam pageParam);
    Result<TableInfo> tableInfo(String tableName);
    Result<Void> updateTable(TableInfo tableInfo);
}
