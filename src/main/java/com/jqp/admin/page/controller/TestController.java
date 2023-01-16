package com.jqp.admin.page.controller;

import com.jqp.admin.common.Result;
import com.jqp.admin.common.service.CacheService;
import com.jqp.admin.common.service.impl.AbstractCacheService;
import com.jqp.admin.page.service.DicCacheService;
import com.jqp.admin.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@Slf4j
@RequestMapping("/test")
public class TestController {


    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private DicCacheService dicCacheService;

    @RequestMapping("/test")
    public Result test(){
        return Result.success("操作成功");
    }
}
