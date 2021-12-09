package com.jqp.admin.page.service;

import com.jqp.admin.common.CrudData;
import com.jqp.admin.common.PageParam;
import com.jqp.admin.common.Result;
import com.jqp.admin.page.data.Page;

import java.util.Map;

public interface PageService {
    void save(Page page);
    Page get(Long id);

    Result<CrudData<Map<String,Object>>> query(Long pageId,PageParam pageParam);
}
