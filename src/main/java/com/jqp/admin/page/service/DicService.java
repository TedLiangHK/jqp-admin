package com.jqp.admin.page.service;

import java.util.List;
import java.util.Map;

public interface DicService {
    List<Map<String,Object>> options(String code);
    String getLabel(String code,String value);
}
