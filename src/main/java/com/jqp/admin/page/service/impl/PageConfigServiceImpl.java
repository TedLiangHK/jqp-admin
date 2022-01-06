package com.jqp.admin.page.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Opt;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageQueryField;
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
            Map<String,Object> queryConfig = new HashMap<>();
            queryConfig.put("name", StringUtil.toFieldColumn(field.getField()));
            if(selector){
                queryConfig.put("name","selector_"+StringUtil.toFieldColumn(field.getField()));
            }
            queryConfig.put("label",field.getLabel());
            queryConfig.put("xs",12);
            queryConfig.put("sm",6);
            queryConfig.put("md",4);
            queryConfig.put("lg",3);
            queryConfig.put("columnClassName","mb-1");
            queryConfig.put("clearable",true);
            queryConfig.put("value","");

            if(field.getWidth() != null){
                queryConfig.put("xs",field.getWidth());
                queryConfig.put("sm",field.getWidth());
                queryConfig.put("md",field.getWidth());
                queryConfig.put("lg",field.getWidth());
            }

            boolean isMulti = !Opt.isSingleValue(field.getOpt());

            if(StrUtil.isNotBlank(field.getValue())){
                queryConfig.put("value",field.getValue());
            }

            if(Whether.YES.equals(field.getHidden())){
                queryConfig.put("xs",0.0001);
                queryConfig.put("sm",0.0001);
                queryConfig.put("md",0.0001);
                queryConfig.put("lg",0.0001);
                queryConfig.put("label","");
                queryConfig.put("type","hidden");
            }else if(DataType.isDate(field.getType())){
                queryConfig.put("format",field.getFormat().replace("yyyy-MM-dd","YYYY-MM-DD"));
                if("yyyy-MM-dd".equals(field.getFormat())){
                    queryConfig.put("type","input-date");
                }else if("yyyy-MM-dd HH:mm:ss".equals(field.getFormat())){
                    queryConfig.put("type","input-datetime");
                }
            }else if(DataType.DIC.equals(field.getType())){
                queryConfig.put("type","select");

                queryConfig.put("source",StrUtil.format("/options/{}",field.getFormat()));
                if(isMulti){
                    queryConfig.put("multiple",true);
                }
            }else if(DataType.Selector.equals(field.getType())){
                Map<String, Object> selectorConfig = this.getSelectorConfig(field.getFormat(),StringUtil.toFieldColumn(field.getField()));
                queryConfig.putAll(selectorConfig);
            }else if(DataType.isNumber(field.getType())){
                queryConfig.put("type","input-number");
            }else{
                queryConfig.put("type","input-text");
            }
            if(Opt.betweenAnd.equals(field.getOpt())){
                if(DataType.isDate(field.getType())){
                    if("yyyy-MM-dd".equals(field.getFormat())){
                        queryConfig.put("type","input-date-range");
                    }else if("yyyy-MM-dd HH:mm:ss".equals(field.getFormat())){
                        queryConfig.put("type","input-datetime-range");
                    }
                }
            }
            queryConfigs.add(queryConfig);
        }
        return queryConfigs;
    }

}
