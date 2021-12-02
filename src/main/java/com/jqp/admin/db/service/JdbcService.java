package com.jqp.admin.db.service;

public interface JdbcService {
    int update(String msg,String sql,Object ...args);
    int update(String sql,Object ...args);

}
