package com.jqp.admin.page.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.constants.Constants;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Opt;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.data.PageButtonData;
import com.jqp.admin.page.data.PageQueryField;
import com.jqp.admin.page.service.InputFieldService;
import com.jqp.admin.page.service.PageButtonService;
import com.jqp.admin.page.service.PageDao;
import com.jqp.admin.page.service.PageConfigService;
import com.jqp.admin.util.StringUtil;
import com.jqp.admin.util.TemplateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
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

        InputStream in = PageConfigServiceImpl.class.getClassLoader().getResourceAsStream("ui-json-template/selector.json.vm");
        String template = IoUtil.readUtf8(in);
        IoUtil.close(in);
        Map<String,Object> params = new HashMap<>();
        params.put("page",page);
        params.put("formField",field);
        params.put("queryConfigs",JSONUtil.toJsonPrettyStr(queryConfigs(page,true)));

        String jsonConfig = TemplateUtil.getValue(template,params);
        JSONObject json = JSONUtil.parseObj(jsonConfig);
        JSONObject body = json.getJSONObject("pickerSchema");
        if("tree".equals(page.getPageType())){
            body.set("perPage",100000);
        }
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
            Map<String,Object> queryConfig = inputFieldService.buildInputField(field,selector);
            if(!field.getField().toLowerCase().contains("id") && !selector && !Whether.YES.equals(field.getRef())){
                queryConfig.put("name", Constants.QUERY_KEY_START+queryConfig.get("name"));
            }
            if(StringUtils.isNotBlank(field.getValue())){
                queryConfig.put("value", SessionContext.getTemplateValue(field.getValue()));
            }
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
            if (!fieldName.toLowerCase().contains("id") && !Whether.YES.equals(f.getRef())){
                fieldName = Constants.QUERY_KEY_START+fieldName;
            }
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


        PageButtonService pageButtonService = SpringUtil.getBean(PageButtonService.class);
        PageButtonData pageButtonData = pageButtonService.dealPageButton(page.getPageButtons(), false);
        params.put("topButtons",JSONUtil.toJsonPrettyStr(pageButtonData.getTopButtons()));
        params.put("bulkButtons",JSONUtil.toJsonPrettyStr(pageButtonData.getBulkButtons()));

        int perPage = 10;
        List<Integer> perPageAvailable = new ArrayList<>();
        perPageAvailable.add(10);
        perPageAvailable.add(20);
        perPageAvailable.add(50);
        perPageAvailable.add(100);
        perPageAvailable.add(300);
        perPageAvailable.add(500);
        if("tree".equals(page.getPageType())){
            perPage = 10000;
            perPageAvailable.clear();
            perPageAvailable.add(10000);
        }
        params.put("perPage",perPage);
        params.put("perPageAvailable",JSONUtil.toJsonPrettyStr(perPageAvailable));
        params.put("openPage",!Whether.NO.equals(page.getOpenPage()));
        String ui = TemplateUtil.getUi("crud.json.vm", params);
        JSONObject json = JSONUtil.parseObj(ui);
        return json;
    }
}
