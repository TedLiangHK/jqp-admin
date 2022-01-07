package com.jqp.admin.page.service;


import com.jqp.admin.page.data.BaseButton;
import com.jqp.admin.page.data.PageButton;

import java.util.Map;

public interface PageButtonService {
    Map<String,Object> getButton(BaseButton baseButton);
}
