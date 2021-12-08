package com.jqp.admin.page.controller;

import com.jqp.admin.common.PageData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageResultField;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/page")
public class PageController {
    @Resource
    private JdbcService jdbcService;

    @RequestMapping("/query")
    public Result<PageData<Page>> query(@RequestBody PageParam pageParam){
        String sql = "select * from page where 1=1 ";
        List<Object> values = new ArrayList<>();
        return jdbcService.query(pageParam,Page.class,sql,values.toArray());
    }

    @RequestMapping("/get")
    public Result<Page> get(Long id){
        if(id == null){
            return Result.success(new Page());
        }
        return Result.success(jdbcService.getById(Page.class,id));
    }

    @RequestMapping("/save")
    public Result<String> save(@RequestBody Page page){
        jdbcService.saveOrUpdate(page);
        return Result.success();
    }
    @RequestMapping("/resultFields")
    public Result resultFields(@RequestBody Page page){
        page.getResultFields().clear();
        return Result.success(page,"已刷新");
    }


}
