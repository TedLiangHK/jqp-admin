package com.jqp.admin.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.service.TemplateService;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("templateService")
public class TemplateServiceImpl implements TemplateService {
    @Resource
    private JdbcService jdbcService;
    @Resource
    private PageService pageService;
    @Override
    public String findAllParent(String childSql, String tableName) {
        childSql = pageService.getQuerySql(childSql);

        String nextChildSql = childSql;
        Set<Long> parentIds = new HashSet<>();
        List<Long> childIds = jdbcService.findForObject(childSql, Long.class);
        parentIds.addAll(childIds);
        if(childIds.isEmpty()){
            return "-1";
        }
        String parentSql = "select parent_id from "+tableName+" where id in ({}) and parent_id is not null ";
        while (true){
            String sql = StrUtil.format(parentSql,nextChildSql);
            List<Long> ids = jdbcService.findForObject(sql, Long.class);
            parentIds.addAll(ids);
            if(ids.isEmpty()){
                break;
            }
            nextChildSql = StringUtil.concatStr(ids,",");
        }
        return StringUtil.concatStr(parentIds,",");
    }
}
