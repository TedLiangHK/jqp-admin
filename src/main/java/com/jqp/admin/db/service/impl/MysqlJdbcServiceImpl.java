package com.jqp.admin.db.service.impl;

import com.jqp.admin.db.service.JdbcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@ConditionalOnProperty(value="db.type",havingValue = "mysql")
@Slf4j
public class MysqlJdbcServiceImpl implements JdbcService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Override
    public int update(String msg,String sql, Object... args) {
        log.info("{},{},{}",msg,sql,args);
        int update = jdbcTemplate.update(sql, args);
        return update;
    }

    @Override
    public int update(String sql, Object... args) {
        return this.update("执行sql",sql,args);
    }
}
