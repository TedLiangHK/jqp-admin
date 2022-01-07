package com.jqp.admin.page.controller;

import com.jqp.admin.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @RequestMapping("/test")
    public Result test(){
        return Result.success("操作成功");
    }
}
