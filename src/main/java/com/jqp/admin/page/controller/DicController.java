package com.jqp.admin.page.controller;

import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.service.DicService;
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
    private DicService dicService;
    @RequestMapping("/options/{dicCode}")
    public Result options(@PathVariable String dicCode){
        Map<String,Object> data = new HashMap<>();
        data.put("options",dicService.options(dicCode));
        return Result.success(data);
    }
}
