package com.jqp.admin.page.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Opt;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.data.PageQueryField;
import com.jqp.admin.page.service.InputFieldService;
import com.jqp.admin.page.service.PageButtonService;
import com.jqp.admin.page.service.PageDao;
import com.jqp.admin.page.service.PageConfigService;
import com.jqp.admin.util.StringUtil;
import com.jqp.admin.util.TemplateUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("pageConfigService")
public class PageConfigServiceImpl implements PageConfigService {
    @Resource
    PageDao pageDao;
    @Resource
    InputFieldService inputFieldService;
    @Override
    public Map<String, Object> getSelectorConfig(String code,String field) {
        Page page = pageDao.get(code);

        URL url = getClass().getClassLoader().getResource("ui-json-template/selector.json.vm");
        String template = FileUtil.readUtf8String(url.getFile());
        Map<String,Object> params = new HashMap<>();
        params.put("page",page);
        params.put("formField",field);
        params.put("queryConfigs",JSONUtil.toJsonPrettyStr(queryConfigs(page,true)));
        String jsonConfig = TemplateUtil.getValue(template,params);
        JSONObject json = JSONUtil.parseObj(jsonConfig);
        return json;
    }
    @Override
    public List<Map<String, Object>> queryConfigs(Page page) {
        return this.queryConfigs(page,false);
    }


    @Override
    public List<Map<String, Object>> queryConfigs(Page page,boolean selector) {

        List<Map<String,Object>> queryConfigs = new ArrayList<>();
        for(PageQueryField field:page.getQueryFields()){
            Map<String,Object> queryConfig = inputFieldService.buildInputField(field,false);
            queryConfigs.add(queryConfig);
        }
        return queryConfigs;
    }

    @Override
    public Map<String, Object> getCurdJson(String code) {
        Page page = pageDao.get(code);
        Map<String,Object> params = new HashMap<>();
        params.put("pageTitle",page.getName());
        params.put("pageCode",code);

        StringBuffer downloadParam = new StringBuffer("?1=1");
        page.getQueryFields().forEach(f->{
            String fieldName = StringUtil.toFieldColumn(f.getField());
            downloadParam.append("&")
                    .append(fieldName)
                    .append("=${")
                    .append(fieldName)
                    .append("}");
        });

        List<Map<String,Object>> queryConfigs = this.queryConfigs(page);

        params.put("pageName",page.getName());
        params.put("queryConfigs", JSONUtil.toJsonPrettyStr(queryConfigs));
        params.put("downloadParam",downloadParam);

        List<Object> topButtons = new ArrayList<>();
        topButtons.add("filter-toggler");

        PageButtonService pageButtonService = SpringUtil.getBean(PageButtonService.class);
        List<PageButton> pageButtons = page.getPageButtons();
        for(PageButton pageButton:pageButtons){
            if("top".equals(pageButton.getButtonLocation())){
                topButtons.add(pageButtonService.getButton(pageButton));
            }else if("row".equals(pageButton.getButtonLocation())){

            }
        }
        params.put("topButtons",JSONUtil.toJsonPrettyStr(topButtons));

        String ui = TemplateUtil.getUi("crud.json.vm", params);
        JSONObject json = JSONUtil.parseObj(ui);
        return json;
    }
}
