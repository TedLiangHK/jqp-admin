package com.jqp.admin.page.inputRender;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.InputField;
import com.jqp.admin.page.service.DicService;
import com.jqp.admin.page.service.PageConfigService;
import com.jqp.admin.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputDefaultRender implements InputRender{
    @Override
    public Map<String, Object> render(InputField field) {
        PageConfigService pageConfigService = SpringUtil.getBean(PageConfigService.class);

        Map<String,Object> config = new HashMap<>();
        if(field.getField().contains("_")){
            config.put("name", StringUtil.toFieldColumn(field.getField()));
        }else{
            config.put("name", field.getField());
        }

        config.put("label",field.getLabel());
        config.put("xs",12);
        config.put("sm",6);
        config.put("md",4);
        config.put("lg",3);
        config.put("columnClassName","mb-1");
        config.put("clearable",true);
        config.put("value","");

        if(field.getWidth() != null){
            config.put("xs",field.getWidth());
            config.put("sm",field.getWidth());
            config.put("md",field.getWidth());
            config.put("lg",field.getWidth());
        }

        boolean isMulti = Whether.YES.equals(field.getMulti());
//        if(StrUtil.isNotBlank(field.getValue())){
//            config.put("value", SessionContext.getTemplateValue(field.getValue()));
//        }
        if(Whether.YES.equals(field.getMust())){
            config.put("required",true);
        }
        if(isMulti){
            config.put("multiple",true);
        }

        if(Whether.YES.equals(field.getHidden())){
            config.put("xs",12);
            config.put("sm",12);
            config.put("md",12);
            config.put("lg",12);
            config.put("label","");
            config.put("type","hidden");
        }else if(DataType.isDate(field.getType())){
            config.put("format",field.getFormat().replace("yyyy-MM-dd","YYYY-MM-DD"));
            if("yyyy-MM-dd".equals(field.getFormat())){
                config.put("type","input-date");
            }else if("yyyy-MM-dd HH:mm:ss".equals(field.getFormat())){
                config.put("type","input-datetime");
            }
        }else if(DataType.DIC.equals(field.getType())){
            config.put("type","select");
            DicService dicService = SpringUtil.getBean(DicService.class);
            List<Map<String, Object>> options = dicService.options(field.getFormat());
            if(options.size()>10){
                config.put("searchable",true);
            }
//            config.put("source",StrUtil.format("/options/{}",field.getFormat()));
            config.put("options",options);
        }else if(DataType.Selector.equals(field.getType()) || DataType.SelectorPop.equals(field.getType())){
            Map<String, Object> selectorConfig = pageConfigService.getSelectorConfig(field.getFormat(),field.getField());
            config.putAll(selectorConfig);
        }else if(DataType.isNumber(field.getType())){
            config.put("type","input-number");
        }else{
            config.put("type","input-text");
        }
        if(StringUtils.isNotBlank(field.getComponentType())){
            config.put("type",field.getComponentType());
        }

        if(StringUtils.isNotBlank(field.getOptionSql())){
            JdbcService jdbcService = SpringUtil.getBean(JdbcService.class);
            List<Map<String, Object>> options = jdbcService.find(field.getOptionSql());
            config.put("options",options);
            config.remove("source");
        }
        this.extra(config,field);
        return config;
    }

    protected void extra(Map<String,Object> config,InputField field){}
}
