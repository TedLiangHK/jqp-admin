package com.jqp.admin.db.data;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/***
 * 表信息
 */
@Data
public class TableInfo {
    //表名字
    private String tableName;
    //表注释
    private String tableComment;
    //表行数
    private int tableRows;
    //列信息
    private List<ColumnInfo> columnInfos;
    //索引信息
    private List<IndexInfo> indexInfos;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableInfo tableInfo = (TableInfo) o;
        return tableName.equals(tableInfo.tableName) && Objects.equals(tableComment, tableInfo.tableComment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, tableComment);
    }
}
