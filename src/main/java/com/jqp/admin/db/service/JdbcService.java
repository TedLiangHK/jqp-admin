package com.jqp.admin.db.service;

import com.jqp.admin.common.BaseData;

import java.util.List;
import java.util.Map;

public interface JdbcService extends JdbcDao{
    void insert(BaseData obj);
    void update(BaseData obj);
    void insert(Map<String,Object> obj,String tableName);
    void update(Map<String,Object> obj,String tableName);
    void saveOrUpdate(BaseData obj);
    void saveOrUpdate(Map<String,Object> obj,String tableName);
    void bathSaveOrUpdate(List<BaseData> objs);
    void bathSaveOrUpdate(List<Map<String,Object>> objs,String tableName);
    void delete(BaseData obj);
    void delete(Long id,String tableName);
    void delete(Long id,Class<? extends BaseData> clz);
    void transactionOption(TransactionOption transactionOption);
}
