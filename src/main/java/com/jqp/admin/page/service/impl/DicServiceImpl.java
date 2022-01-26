package com.jqp.admin.page.service.impl;

import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.service.DicService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("dicService")
public class DicServiceImpl implements DicService {
    @Resource
    private JdbcService jdbcService;
    @Override
    public List<Map<String, Object>> options(String code) {
        List<Map<String, Object>> options = jdbcService.find("select label,value from dic_item where parent_id in(" +
                "select id from dic where dic_code = ? " +
                ") order by seq asc,id asc ", code);
        return options;
    }
}
