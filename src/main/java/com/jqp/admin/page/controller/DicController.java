package com.jqp.admin.page.controller;

import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DicController {

    @Resource
    private JdbcService jdbcService;
    @RequestMapping("/options/{dicCode}")
    public Result options(@PathVariable String dicCode){
        List<Map<String, Object>> options = jdbcService.find("select label,value from dic_item where parent_id in(" +
                "select id from dic where dic_code = ? " +
                ") order by value asc ", dicCode);
        Map<String,Object> data = new HashMap<>();
        data.put("options",options);
        return Result.success(data);
    }
}
