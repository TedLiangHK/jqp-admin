package com.jqp.admin.db.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.config.DbConfig;
import com.jqp.admin.db.data.ColumnInfo;
import com.jqp.admin.db.data.IndexInfo;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.JdbcDao;
import com.jqp.admin.db.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value="db.type",havingValue = "mysql")
@Slf4j
public class MysqlTableServiceImpl implements TableService {
    @Resource
    private DbConfig dbConfig;

    @Resource
    private JdbcDao jdbcDao;

    @Override
    public Result<PageData<TableInfo>> queryTable(PageParam pageParam) {

        String sql = getTableSql();
        List<Object> args = new ArrayList<>();
        if(StrUtil.isNotBlank(pageParam.getStr("tableName"))){
            sql += " and table_name like ? ";
            args.add(StrUtil.format("%{}%",pageParam.getStr("tableName")));
        }

        if(StrUtil.isNotBlank(pageParam.getStr("tableComment"))){
            sql += " and table_comment like ? ";
            args.add(StrUtil.format("%{}%",pageParam.getStr("tableComment")));
        }
        Object[] params = args.toArray();

        Result<PageData<TableInfo>> result = jdbcDao.query(pageParam, TableInfo.class, sql, params);
        List<TableInfo> tableInfos = result.getData().getItems();
        tableInfos.forEach(tableInfo -> {
            tableInfo.setId(tableInfo.getTableName());
            tableInfo.setOldTableName(tableInfo.getTableName());
        });
        return result;
    }

    private String getTableSql(){
        return "select t.TABLE_NAME,t.TABLE_COMMENT,t.TABLE_ROWS from "+dbConfig.getManageSchema()+".`TABLES` t where t.TABLE_SCHEMA = '"+dbConfig.getSchema()+"'";
    }

    @Override
    public Result<TableInfo> tableInfo(String tableName) {
        if(StrUtil.isBlank(tableName)){
            return Result.success(new TableInfo());
        }

        String tableSql = getTableSql() + " and table_name='"+tableName+"'";
        List<TableInfo> list = jdbcDao.find(tableSql,TableInfo.class);
        if(list.isEmpty()){
            return Result.error("表不存在");
        }
        TableInfo tableInfo = list.get(0);
        tableInfo.setOldTableName(tableInfo.getTableName());
        tableInfo.setId(tableInfo.getTableName());

        String columnSql = "select c.COLUMN_NAME,c.COLUMN_COMMENT,c.COLUMN_TYPE,c.IS_NULLABLE from "+dbConfig.getManageSchema()+".`COLUMNS` c where c.TABLE_SCHEMA = '"+dbConfig.getSchema()+"' and c.TABLE_NAME = '"+tableName+"' and c.column_name <> 'id' ";
        List<ColumnInfo> columnInfos = jdbcDao.find(columnSql, ColumnInfo.class);
        for(ColumnInfo c:columnInfos){
            c.setOldColumnName(c.getColumnName());
        }
        tableInfo.setColumnInfos(columnInfos);

        String indexSql = "show keys from "+tableName;
        List<Map<String, Object>> indexList = jdbcDao.find(indexSql);

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
            Long nonUnique = (Long)index.get("nonUnique");
            //排除唯一索引和主键
            if(Long.valueOf(0).equals(nonUnique)){
                continue;
            }
            IndexInfo indexInfo = indexInfoMap.get(keyName);
            if(indexInfo == null){
                indexInfo = new IndexInfo();
                indexInfo.setKeyName(keyName);
                indexInfo.setIndexComment(indexComment);
                indexInfo.setColumnName(columnName);
                indexInfo.setOldKeyName(keyName);

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

        boolean isCreate = StrUtil.isBlank(tableInfo.getOldTableName());
        if(isCreate){
            //建表
            String sql = "create table "+tableInfo.getTableName()+" (\n";
            sql += "\tid BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键' ,\n";
            for (int i = 0; i < tableInfo.getColumnInfos().size(); i++) {
                ColumnInfo columnInfo = tableInfo.getColumnInfos().get(i);
                sql += "\t";
                if(columnInfo.getColumnName().equalsIgnoreCase("id")){
                    continue;
                }else{
                    sql += columnInfo.getColumnName()+" "+columnInfo.getColumnType() + " comment '"+columnInfo.getColumnComment()+"'";
                }
                sql += i == tableInfo.getColumnInfos().size() -1 ? "\n" : ",\n";
            }
            sql += ") comment = '"+tableInfo.getTableComment()+"'";
            jdbcDao.update("建表",sql);

            tableInfo.getIndexInfos().forEach(indexInfo -> {
                String indexSql = StrUtil.format(" alter table {} add index {} ({}) comment '{}' ",
                        tableInfo.getTableName(),
                        indexInfo.getKeyName(),
                        indexInfo.getColumnName(),
                        indexInfo.getIndexComment()
                );
                jdbcDao.update("添加索引",indexSql);
            });
            return Result.success();
        }

        //更新表
        Result<TableInfo> oldTable = this.tableInfo(tableInfo.getOldTableName());
        if(!oldTable.isSuccess()){
            return Result.error("表不存在");
        }
        TableInfo oldTableInfo = oldTable.getData();
        if(!oldTableInfo.getOldTableName().equals(tableInfo.getTableName())){
            String sql = StrUtil.format("alter table {} rename to {} ",oldTableInfo.getTableName(),tableInfo.getTableName());
            jdbcDao.update("修改表名",sql);
        }
        if(!oldTableInfo.getTableComment().equals(tableInfo.getTableComment())){
            String sql = StrUtil.format("alter table {} comment ? ",tableInfo.getTableName());
            jdbcDao.update("修改表注释",sql,tableInfo.getTableComment());
        }
        Map<String, ColumnInfo> oldColumnMap = oldTableInfo.getColumnInfos().stream().collect(Collectors.toMap(ColumnInfo::getColumnName, c -> c));
        Set<String> names = new HashSet<>();
        //新增/修改字段
        for(ColumnInfo columnInfo:tableInfo.getColumnInfos()){
            if(StrUtil.isBlank(columnInfo.getOldColumnName())){
                //新增字段
                String sql = StrUtil.format("alter table {} add {} {} {} comment '{}' ",
                        tableInfo.getTableName(),
                        columnInfo.getColumnName(),
                        columnInfo.getColumnType(),
                        (!"YES".equalsIgnoreCase(columnInfo.getIsNullable()) ? " NOT NULL" : ""),
                        columnInfo.getColumnComment()
                );
                jdbcDao.update("新增字段",sql);
            }else{
                names.add(columnInfo.getOldColumnName());
                ColumnInfo oldColumnInfo = oldColumnMap.get(columnInfo.getOldColumnName());
                if(!oldColumnInfo.equals(columnInfo)){
                    if(!columnInfo.getOldColumnName().equalsIgnoreCase(columnInfo.getColumnName())){
                        //修改字段名称
                        String sql = StrUtil.format(" alter table {} change {} {} {} comment '{}'",
                                tableInfo.getTableName(),
                                columnInfo.getOldColumnName(),
                                columnInfo.getColumnName(),
                                columnInfo.getColumnType(),
                                columnInfo.getColumnComment()
                        );
                        jdbcDao.update("修改字段名称",sql);
                    }else{
                        //修改字段
                        String sql = StrUtil.format("alter table {} modify {} {} {} comment '{}' ",
                                tableInfo.getTableName(),
                                columnInfo.getColumnName(),
                                columnInfo.getColumnType(),
                                (!"YES".equalsIgnoreCase(columnInfo.getIsNullable()) ? " NOT NULL" : ""),
                                columnInfo.getColumnComment()
                        );
                        jdbcDao.update("修改字段",sql);
                    }
                }
            }
        }
        //删除字段
        for(ColumnInfo oldColumnInfo:oldTableInfo.getColumnInfos()){
            if(!names.contains(oldColumnInfo.getColumnName())){
                String sql = StrUtil.format("alter table {} drop column {} ",tableInfo.getTableName(),oldColumnInfo.getColumnName());
                jdbcDao.update("删除字段",sql);
            }
        }
        Map<String, IndexInfo> oldIndexMap = oldTableInfo.getIndexInfos().stream().collect(Collectors.toMap(IndexInfo::getKeyName, c -> c));

        Set<String> indexNames = new HashSet<>();
        //新增/修改索引
        for(IndexInfo indexInfo:tableInfo.getIndexInfos()){

            boolean newIndex = false;
            if(StrUtil.isBlank(indexInfo.getOldKeyName())){
                newIndex = true;
            }else{
                indexNames.add(indexInfo.getOldKeyName());
                IndexInfo oldIndexInfo = oldIndexMap.get(indexInfo.getOldKeyName());
                if(!oldIndexInfo.equals(indexInfo)){
                    //修改索引,先删除,后添加
                    String sql = StrUtil.format(" drop index {} on {} ",oldIndexInfo.getKeyName(),tableInfo.getTableName());
                    jdbcDao.update("删除索引",sql);
                    newIndex = true;
                }
            }
            if(newIndex){
                //新增索引
                String indexSql = StrUtil.format(" alter table {} add index {} ({}) comment '{}' ",
                        tableInfo.getTableName(),
                        indexInfo.getKeyName(),
                        indexInfo.getColumnName(),
                        indexInfo.getIndexComment()
                );
                jdbcDao.update("添加索引",indexSql);
            }
        }
        //删除索引
        for(IndexInfo oldIndexInfo:oldTableInfo.getIndexInfos()){
            if(!indexNames.contains(oldIndexInfo.getKeyName())){
                String sql = StrUtil.format(" drop index {} on {} ",oldIndexInfo.getKeyName(),tableInfo.getTableName());
                jdbcDao.update("删除索引",sql);
            }
        }
        return Result.success();
    }

    @Override
    public Result dropTable(String tableName) {
        String sql = StrUtil.format(" drop table {}", tableName);
        jdbcDao.update("删除表",sql);
        return Result.success();
    }
}
