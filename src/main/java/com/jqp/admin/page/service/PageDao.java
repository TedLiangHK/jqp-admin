package com.jqp.admin.page.service;

import com.jqp.admin.page.data.Page;

import java.util.List;
import java.util.Map;

public interface PageDao {
    void save(Page page);
    Page get(Long id);
    Page get(String pageCode);
}
