package com.jqp.admin.db.data;

import lombok.Data;

import java.util.*;

/***
 * 表信息
 */
@Data
public class TableInfo {
    //用于复制,和tableName一致
    private String id;
    //表名,用于更新
    private String oldTableName;
    //表名字
    private String tableName;
    //表注释
    private String tableComment;
    //表行数
    private int tableRows;
    //列信息
    private List<ColumnInfo> columnInfos = new ArrayList<>();
    //索引信息
    private List<IndexInfo> indexInfos = new ArrayList<>();

    private List<Map<String,Object>> buttons = new ArrayList<>();

    {
        Random r = new Random();
        if(r.nextBoolean()){
            HashMap<String, Object> map = new HashMap<>();
            map.put("name","按钮1");

            buttons.add(map);
        }
        if(r.nextBoolean()){
            HashMap<String, Object> map = new HashMap<>();
            map.put("name","按钮2");

            buttons.add(map);
        }
        if(r.nextBoolean()){
            HashMap<String, Object> map = new HashMap<>();
            map.put("name","按钮3");

            buttons.add(map);
        }
    }

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
