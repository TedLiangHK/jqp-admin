package com.jqp.admin.page.service;


import com.jqp.admin.page.data.BaseButton;
import com.jqp.admin.page.data.Form;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageButton;

import java.util.List;
import java.util.Map;

public interface PageButtonService {
    Map<String, Object> getButton(BaseButton baseButton);

    List<PageButton> byPageCode(String pageCode);

    void save(PageButton pageButton);

    List<PageButton> byPageId(Long id);

    List<PageButton> byPage(Page page);

    List<PageButton> getByForm(Form form);

    Page getPage(PageButton pageButton);
}
