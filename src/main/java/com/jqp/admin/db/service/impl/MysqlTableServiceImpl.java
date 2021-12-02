package com.jqp.admin.db.service.impl;

import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.config.DbConfig;
import com.jqp.admin.db.data.ColumnInfo;
import com.jqp.admin.db.data.IndexInfo;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.TableService;
import com.jqp.admin.util.RowMapperUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@ConditionalOnProperty(value="db.type",havingValue = "mysql")
public class MysqlTableServiceImpl implements TableService {
    @Resource
    private DbConfig dbConfig;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public Result<PageData<TableInfo>> queryTable(PageParam pageParam) {

        String sql = getTableSql();
        String countSql = "select count(*) from ("+sql+") t";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
        int start = (pageParam.getPage() - 1) * pageParam.getLimit();
        String pageSql = sql + "limit "+start+","+ pageParam.getLimit();
        List<TableInfo> tableInfos = jdbcTemplate.query(pageSql, RowMapperUtil.newRowMapper(TableInfo.class));
        Result<PageData<TableInfo>> result = new Result<>();
        PageData<TableInfo> data = new PageData<>();
        data.setItems(tableInfos);
        data.setTotal(count);
        result.setData(data);
        return result;
    }

    private String getTableSql(){
        return "select t.TABLE_NAME,t.TABLE_COMMENT,t.TABLE_ROWS from "+dbConfig.getManageSchema()+".`TABLES` t where t.TABLE_SCHEMA = '"+dbConfig.getSchema()+"'";
    }

    @Override
    public Result<TableInfo> tableInfo(String tableName) {

        String tableSql = getTableSql() + " and table_name='"+tableName+"'";
        TableInfo tableInfo = jdbcTemplate.query(tableSql, RowMapperUtil.newRowMapper(TableInfo.class)).get(0);

        String columnSql = "select c.COLUMN_NAME,c.COLUMN_COMMENT,c.COLUMN_TYPE,c.IS_NULLABLE from "+dbConfig.getManageSchema()+".`COLUMNS` c where c.TABLE_SCHEMA = '"+dbConfig.getSchema()+"' and c.TABLE_NAME = '"+tableName+"'";
        List<ColumnInfo> columnInfos = jdbcTemplate.query(columnSql, RowMapperUtil.newRowMapper(ColumnInfo.class));
        tableInfo.setColumnInfos(columnInfos);

        String indexSql = "show keys from "+tableName;
        List<Map<String, Object>> indexList = jdbcTemplate.query(indexSql, RowMapperUtil.newMapMapper());

        List<IndexInfo> indexInfos = new ArrayList<>();
        Map<String,IndexInfo> indexInfoMap = new HashMap<>();

        Collections.sort(indexList, (o1, o2) -> {
            String keyName1 = (String) o1.get("keyName");
            String keyName2 = (String) o2.get("keyName");
            Long seqIndex1 = (Long) o1.get("seqInIndex");
            Long seqIndex2 = (Long) o2.get("seqInIndex");

            int compare = keyName1.compareTo(keyName2);
            compare = compare == 0 ? seqIndex1.compareTo(seqIndex2) : compare;
            return compare;
        });
        for(Map<String,Object> index:indexList){
            String keyName = (String) index.get("keyName");
            String columnName = (String) index.get("columnName");
            String indexComment = (String) index.get("indexComment");
            IndexInfo indexInfo = indexInfoMap.get(keyName);
            if(indexInfo == null){
                indexInfo = new IndexInfo();
                indexInfo.setKeyName(keyName);
                indexInfo.setIndexComment(indexComment);
                indexInfo.setColumnName(columnName);

                indexInfoMap.put(keyName,indexInfo);
                indexInfos.add(indexInfo);
            }else{
                indexInfo.setColumnName(indexInfo.getColumnName()+","+columnName);
            }
        }
        tableInfo.setIndexInfos(indexInfos);

        return new Result<>(tableInfo);
    }

    @Override
    public Result<Void> updateTable(TableInfo tableInfo) {
        return null;
    }
}
