package com.jqp.admin.db.service;

import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;

import java.util.List;
import java.util.Map;

public interface JdbcDao {
    int update(String msg,String sql,Object ...args);
    int update(String sql,Object ...args);
    Long insert(String msg,String sql,Object ...args);
    Long insert(String sql,Object ...args);

    <T> Result<PageData<T>> query(PageParam pageParam, Class<T> clz, String sql,Object... values);
    Result<PageData<Map<String,Object>>> query(PageParam pageParam, String sql,Object... values);
    <T> List<T> find(String sql,Class<T> clz,Object ...args);
    List<Map<String,Object>> find(String sql, Object ...args);
    <T> T getById(Class<T> clz,Long id);
    Map<String,Object> getById(String tableName,Long id);
}
