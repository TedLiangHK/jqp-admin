package com.jqp.admin.db.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcDao;
import com.jqp.admin.util.RowMapperUtil;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service("jdbcDao")
@ConditionalOnProperty(value="db.type",havingValue = "mysql")
@Slf4j
public class MysqlJdbcDaoImpl implements JdbcDao {
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

    @Override
    public Long insert(String msg,String sql, Object... args) {
        log.info("{},{},{}",msg,sql,args);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i=1;
            for(Object arg:args){
                ps.setObject(i++,arg);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Long insert(String sql, Object... args) {
        return this.insert("执行insert",sql,args);
    }

    @Override
    public <T> Result<PageData<T>> query(PageParam pageParam, Class<T> clz,String sql,Object... args) {
        String countSql = StrUtil.format("select count(*) from ({}) t",sql);
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, args);
        int start = (pageParam.getPage() - 1) * pageParam.getPerPage();
        if(start >= count || start < 0){
            start = 0;
        }
        String pageSql = StrUtil.format("{} limit {},{}",sql,start,pageParam.getPerPage());
        List<T> data = jdbcTemplate.query(pageSql, RowMapperUtil.newRowMapper(clz),args);
        PageData<T> pageData = new PageData();
        pageData.setItems(data);
        pageData.setTotal(count);
        return new Result<>(pageData);
    }
    @Override
    public Result<PageData<Map<String,Object>>> query(PageParam pageParam,String sql,Object... args) {
        String countSql = StrUtil.format("select count(*) from ({}) t",sql);
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, args);
        int start = (pageParam.getPage() - 1) * pageParam.getPerPage();
        if(start >= count || start < 0){
            start = 0;
        }
        String pageSql = StrUtil.format("{} limit {},{}",sql,start,pageParam.getPerPage());
        List<Map<String,Object>> data = jdbcTemplate.query(pageSql, RowMapperUtil.newMapMapper(),args);
        PageData<Map<String,Object>> pageData = new PageData();
        pageData.setItems(data);
        pageData.setTotal(count);
        return new Result<>(pageData);
    }

    @Override
    public <T> List<T> find(String sql, Class<T> clz, Object... args) {
        return jdbcTemplate.query(sql,RowMapperUtil.newRowMapper(clz),args);
    }

    @Override
    public List<Map<String, Object>> find(String sql, Object... args) {
        return jdbcTemplate.query(sql,RowMapperUtil.newMapMapper(),args);
    }

    @Override
    public Map<String, Object> getById(String tableName, Long id) {
        String sql = StrUtil.format("select * from {} where id = ? ",tableName);
        List<Map<String,Object>> list = this.find(sql, id);
        return list.isEmpty() ? null :list.get(0);
    }

    @Override
    public <T> T getById(Class<T> clz, Long id) {
        String sql = StrUtil.format("select * from {} where id = ? ",getTableName(clz));
        List<T> list = this.find(sql, clz, id);
        return list.isEmpty() ? null :list.get(0);
    }

    private String getTableName(Class clz){
        return StringUtil.toSqlColumn(clz.getSimpleName());
    }
}
